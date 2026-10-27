package com.astr0net.promptcompiler.data

import org.json.JSONObject

/** Normalized compiler output (mirrors the `parsed` payload of the web app). */
data class ParsedResult(
    val detectedCategory: String = "general",
    val detectedSubcategory: String = "",
    val detectedPurpose: String = "",
    val englishPrompt: String = "",
    val persianPrompt: String = "",
    val preservedRequirements: List<String> = emptyList(),
    val assumptions: List<String> = emptyList(),
    val missingInformation: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
)

object PromptEngine {
    const val NO_PROMPT = "No prompt was generated."

    private val fenceStart = Regex("^```(?:json)?\\s*", RegexOption.IGNORE_CASE)
    private val fenceEnd = Regex("\\s*```$")

    private fun asStr(value: Any?): String = when (value) {
        is String -> value.trim()
        is Boolean -> if (value) "True" else "False"
        is Int, is Long -> value.toString()
        is Double -> if (value % 1.0 == 0.0 && !value.isInfinite()) "%.1f".format(java.util.Locale.ROOT, value) else value.toString()
        else -> ""
    }

    private fun asList(obj: JSONObject, key: String): List<String> {
        val value = if (obj.has(key) && !obj.isNull(key)) obj.get(key) else return emptyList()
        return when (value) {
            is org.json.JSONArray -> (0 until value.length()).map { asStr(value.get(it).takeUnless { v -> v == JSONObject.NULL }) }.filter { it.isNotEmpty() }
            is String -> value.trim().let { if (it.isNotEmpty()) listOf(it) else emptyList() }
            else -> emptyList()
        }
    }

    private fun str(obj: JSONObject, key: String): String =
        if (obj.has(key) && !obj.isNull(key)) asStr(obj.get(key)) else ""

    /** Parse and normalize model JSON into the stable result schema (never throws). */
    fun parseModelOutput(rawText: String?): ParsedResult {
        var cleaned = (rawText ?: "").trim()
        if (cleaned.isNotEmpty()) {
            cleaned = fenceStart.replace(cleaned, "")
            cleaned = fenceEnd.replace(cleaned, "").trim()
        }

        val data: JSONObject? = try {
            if (cleaned.startsWith("{")) JSONObject(cleaned) else null
        } catch (_: Exception) {
            null
        }

        val result = if (data != null) {
            var category = str(data, "detected_category").ifEmpty { "general" }
            if (category !in KNOWN_CATEGORIES) category = "general"
            ParsedResult(
                detectedCategory = category,
                detectedSubcategory = str(data, "detected_subcategory"),
                detectedPurpose = str(data, "detected_purpose"),
                englishPrompt = str(data, "english_prompt").ifEmpty { str(data, "englishPrompt") },
                persianPrompt = str(data, "persian_prompt").ifEmpty { str(data, "persianPrompt") },
                preservedRequirements = asList(data, "preserved_requirements"),
                assumptions = asList(data, "assumptions"),
                missingInformation = asList(data, "missing_information"),
                suggestions = asList(data, "suggestions"),
            )
        } else {
            ParsedResult(englishPrompt = cleaned)
        }
        return if (result.englishPrompt.isEmpty()) result.copy(englishPrompt = NO_PROMPT) else result
    }
}
