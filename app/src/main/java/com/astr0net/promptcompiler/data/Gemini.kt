package com.astr0net.promptcompiler.data

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Token counts returned by Gemini's `usageMetadata`. */
data class UsageMetadata(val prompt: Int = 0, val candidates: Int = 0, val thoughts: Int = 0, val total: Int = 0)

data class GeminiResponse(val text: String, val usage: UsageMetadata?)

/** User-facing failure. [message] carries the same wording as the reference implementation. */
class GeminiException(message: String) : Exception(message)

/** Thrown when the device can't reach Google at all (offline, DNS, TLS, timeout). */
class GeminiNetworkException(val detail: String, cause: Throwable? = null) : IOException(detail, cause)

/** Google AI Studio HTTP client (mirrors prompt_compiler/services/gemini.py). */
class GeminiClient(
    private val baseUrl: String = "https://generativelanguage.googleapis.com",
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .callTimeout(190, TimeUnit.SECONDS)
        .build(),
) {
    suspend fun generate(apiKey: String, systemPrompt: String, userPrompt: String, modelId: String): GeminiResponse =
        withContext(Dispatchers.IO) {
            if (apiKey.isEmpty()) {
                throw GeminiException(
                    "Google AI Studio API key is not configured. Add it from the Settings menu. " +
                        "(https://aistudio.google.com/apikey)"
                )
            }

            val payload = JSONObject()
                .put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
                .put(
                    "contents",
                    JSONArray().put(
                        JSONObject().put("role", "user").put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", wrapUserPrompt(userPrompt))),
                        )
                    ),
                )
                .put("generationConfig", JSONObject().put("temperature", 0.7).put("maxOutputTokens", 8192))

            val url = "$baseUrl/v1beta/models/$modelId:generateContent".toHttpUrlOrThrow().newBuilder()
                .addQueryParameter("key", apiKey)
                .build()
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val (status, contentType, body) = try {
                http.newCall(request).execute().use { r ->
                    Triple(r.code, r.header("content-type").orEmpty().lowercase(), r.body?.string().orEmpty())
                }
            } catch (e: IOException) {
                throw GeminiNetworkException(e.javaClass.simpleName, e)
            }

            if (status != 200) throw GeminiException(errorMessage(status, contentType, body))

            val data = try {
                JSONObject(body)
            } catch (_: JSONException) {
                throw GeminiException(
                    "No valid API response was received (status=$status): " +
                        (body.trim().take(400).ifEmpty { "empty response" })
                )
            }

            data.optJSONObject("error")?.let { error ->
                throw GeminiException("[${error.optInt("code", status)}] ${error.optString("message", error.toString())}")
            }

            val text = try {
                data.getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            } catch (_: JSONException) {
                throw GeminiException("No model response was received: " + data.toString().take(600))
            }

            val usage = data.optJSONObject("usageMetadata")?.let {
                UsageMetadata(
                    prompt = it.optInt("promptTokenCount", 0),
                    candidates = it.optInt("candidatesTokenCount", 0),
                    thoughts = it.optInt("thoughtsTokenCount", 0),
                    total = it.optInt("totalTokenCount", 0),
                )
            }
            GeminiResponse(text, usage)
        }

    companion object {
        fun wrapUserPrompt(userPrompt: String): String =
            "Compile the following Persian request into an optimized English prompt. " +
                "The English prompt is the canonical result. Then translate that exact finalized " +
                "English prompt into a faithful Persian version. Do not generate Persian " +
                "independently from the original input. Follow your instructions:\n\n" +
                userPrompt

        fun errorMessage(status: Int, contentType: String, body: String): String {
            if ("html" in contentType || body.trimStart().lowercase().startsWith("<!doctype")) {
                return "Google API rejected the request (HTTP 403). Check access to " +
                    "generativelanguage.googleapis.com from this network."
            }
            var code = status
            var message: String
            try {
                val error = JSONObject(body).getJSONObject("error")
                code = error.optInt("code", status)
                message = error.optString("message", error.toString())
            } catch (_: JSONException) {
                message = body.trim().take(400).ifEmpty { "No valid JSON response was received from the API." }
            }
            if ("api key" in message.lowercase() && "not valid" in message.lowercase()) {
                message = "The API key is invalid. Create a new key at aistudio.google.com/apikey."
            }
            return "[$code] $message"
        }

        private fun String.toHttpUrlOrThrow() = okhttp3.HttpUrl.Companion.run { this@toHttpUrlOrThrow.toHttpUrl() }
    }
}
