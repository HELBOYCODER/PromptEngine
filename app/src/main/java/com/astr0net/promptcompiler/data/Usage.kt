package com.astr0net.promptcompiler.data

import java.io.File
import java.util.Calendar
import org.json.JSONArray
import org.json.JSONObject

data class UsageEvent(val model: String, val timeMs: Long, val input: Int, val output: Int, val thoughts: Int, val total: Int)

data class UsageToday(val requests: Int, val input: Int, val output: Int, val thoughts: Int, val total: Int)

data class ModelUsage(
    val info: ModelInfo,
    val todayRequests: Int,
    val todayTokens: Int,
    val minuteRequests: Int,
    val minuteTokens: Int,
)

data class UsageSnapshot(val today: UsageToday, val models: List<ModelUsage>)

/** Local token-usage persistence + aggregation (mirrors prompt_compiler/services/usage.py). */
class UsageStore(private val file: File, private val clock: () -> Long = System::currentTimeMillis) {

    @Synchronized
    fun load(): List<UsageEvent> {
        if (!file.exists()) return emptyList()
        return try {
            val events = JSONObject(file.readText()).optJSONArray("events") ?: return emptyList()
            (0 until events.length()).map {
                val e = events.getJSONObject(it)
                UsageEvent(e.getString("m"), e.getLong("t"), e.optInt("in"), e.optInt("out"), e.optInt("th"), e.optInt("tot"))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun save(events: List<UsageEvent>) {
        val arr = JSONArray()
        events.forEach {
            arr.put(JSONObject().put("m", it.model).put("t", it.timeMs).put("in", it.input).put("out", it.output).put("th", it.thoughts).put("tot", it.total))
        }
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(JSONObject().put("events", arr).toString())
        if (!tmp.renameTo(file)) { file.writeText(tmp.readText()); tmp.delete() }
    }

    @Synchronized
    fun record(modelId: String, usage: UsageMetadata?) {
        if (usage == null) return
        val now = clock()
        val cutoff = now - 7L * 24 * 3600 * 1000
        val events = load() + UsageEvent(modelId, now, usage.prompt, usage.candidates, usage.thoughts, usage.total)
        save(events.filter { it.timeMs >= cutoff })
    }

    @Synchronized
    fun clear() { file.delete() }

    fun localMidnightMs(): Long = Calendar.getInstance().apply {
        timeInMillis = clock()
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun compute(): UsageSnapshot {
        val events = load()
        val now = clock()
        val dayStart = localMidnightMs()
        val minuteStart = now - 60_000
        val today = events.filter { it.timeMs >= dayStart }
        val minute = events.filter { it.timeMs >= minuteStart }
        return UsageSnapshot(
            today = UsageToday(today.size, today.sumOf { it.input }, today.sumOf { it.output }, today.sumOf { it.thoughts }, today.sumOf { it.total }),
            models = MODELS.map { m ->
                val t = today.filter { it.model == m.id }
                val mi = minute.filter { it.model == m.id }
                ModelUsage(m, t.size, t.sumOf { it.total }, mi.size, mi.sumOf { it.total })
            },
        )
    }
}
