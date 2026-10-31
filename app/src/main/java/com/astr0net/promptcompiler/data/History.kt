package com.astr0net.promptcompiler.data

import java.io.File
import org.json.JSONArray
import org.json.JSONObject

/** One compiled prompt kept on the device. */
data class HistoryItem(val id: Long, val timeMs: Long, val input: String, val modelId: String, val result: ParsedResult)

/** Last [MAX_ITEMS] compiled prompts, newest first, stored as a private JSON file. */
class HistoryStore(private val file: File, private val clock: () -> Long = System::currentTimeMillis) {

    @Synchronized
    fun load(): List<HistoryItem> {
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONObject(file.readText()).getJSONArray("items")
            (0 until arr.length()).map { toItem(arr.getJSONObject(it)) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun add(input: String, modelId: String, result: ParsedResult): List<HistoryItem> {
        val now = clock()
        val existing = load()
        val id = maxOf(now, (existing.firstOrNull()?.id ?: 0L) + 1)
        val items = (listOf(HistoryItem(id, now, input, modelId, result)) + existing).take(MAX_ITEMS)
        save(items)
        return items
    }

    @Synchronized
    fun delete(id: Long): List<HistoryItem> = load().filterNot { it.id == id }.also(::save)

    @Synchronized
    fun clear() { file.delete() }

    private fun save(items: List<HistoryItem>) {
        val arr = JSONArray()
        items.forEach { arr.put(toJson(it)) }
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(JSONObject().put("items", arr).toString())
        if (!tmp.renameTo(file)) { file.writeText(tmp.readText()); tmp.delete() }
    }

    private fun toJson(i: HistoryItem) = JSONObject()
        .put("id", i.id).put("t", i.timeMs).put("in", i.input).put("m", i.modelId)
        .put("cat", i.result.detectedCategory).put("sub", i.result.detectedSubcategory).put("purpose", i.result.detectedPurpose)
        .put("en", i.result.englishPrompt).put("fa", i.result.persianPrompt)
        .put("req", JSONArray(i.result.preservedRequirements)).put("asm", JSONArray(i.result.assumptions))
        .put("miss", JSONArray(i.result.missingInformation)).put("sug", JSONArray(i.result.suggestions))

    private fun strings(o: JSONObject, k: String) = o.optJSONArray(k)?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()

    private fun toItem(o: JSONObject) = HistoryItem(
        o.getLong("id"), o.getLong("t"), o.getString("in"), o.getString("m"),
        ParsedResult(
            o.optString("cat", "general"), o.optString("sub"), o.optString("purpose"), o.optString("en"), o.optString("fa"),
            strings(o, "req"), strings(o, "asm"), strings(o, "miss"), strings(o, "sug"),
        ),
    )

    companion object { const val MAX_ITEMS = 100 }
}
