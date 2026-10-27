package com.astr0net.promptcompiler

import com.astr0net.promptcompiler.data.GeminiClient
import com.astr0net.promptcompiler.data.GeminiException
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeminiClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: GeminiClient

    @Before fun setUp() {
        server = MockWebServer().apply { start() }
        client = GeminiClient(baseUrl = server.url("/").toString().trimEnd('/'))
    }

    @After fun tearDown() = server.shutdown()

    private fun fails(block: () -> Unit): String = try { block(); error("expected GeminiException") } catch (e: GeminiException) { e.message.orEmpty() }

    @Test fun successParsesTextAndUsage() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"candidates":[{"content":{"parts":[{"text":"hello"}]}}],
            "usageMetadata":{"promptTokenCount":10,"candidatesTokenCount":20,"thoughtsTokenCount":5,"totalTokenCount":35}}"""))
        val r = client.generate("KEY", "SYS", "سلام", "gemini-3.5-flash")
        assertEquals("hello", r.text)
        assertEquals(35, r.usage?.total)
        assertEquals(5, r.usage?.thoughts)

        val req = server.takeRequest()
        assertEquals("/v1beta/models/gemini-3.5-flash:generateContent?key=KEY", req.path)
        val body = JSONObject(req.body.readUtf8())
        assertEquals("SYS", body.getJSONObject("system_instruction").getJSONArray("parts").getJSONObject(0).getString("text"))
        val userText = body.getJSONArray("contents").getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text")
        assertTrue(userText.startsWith("Compile the following Persian request into an optimized English prompt."))
        assertTrue(userText.endsWith("\n\nسلام"))
        assertEquals(8192, body.getJSONObject("generationConfig").getInt("maxOutputTokens"))
        assertEquals(0.7, body.getJSONObject("generationConfig").getDouble("temperature"), 0.0)
    }

    @Test fun missingKeyDoesNotHitNetwork() {
        val msg = fails { runBlocking { client.generate("", "s", "u", "m") } }
        assertTrue(msg.startsWith("Google AI Studio API key is not configured."))
        assertEquals(0, server.requestCount)
    }

    @Test fun invalidKeyIsFriendly() {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":{"code":400,"message":"API key not valid. Please pass a valid API key."}}"""))
        assertEquals("[400] The API key is invalid. Create a new key at aistudio.google.com/apikey.", fails { runBlocking { client.generate("k", "s", "u", "m") } })
    }

    @Test fun apiErrorKeepsCodeAndMessage() {
        server.enqueue(MockResponse().setResponseCode(429).setBody("""{"error":{"code":429,"message":"Quota exceeded"}}"""))
        assertEquals("[429] Quota exceeded", fails { runBlocking { client.generate("k", "s", "u", "m") } })
    }

    @Test fun htmlResponseIsReportedAsBlocked() {
        server.enqueue(MockResponse().setResponseCode(403).setHeader("Content-Type", "text/html").setBody("<!DOCTYPE html><html></html>"))
        assertTrue(fails { runBlocking { client.generate("k", "s", "u", "m") } }.startsWith("Google API rejected the request (HTTP 403)"))
    }

    @Test fun nonJsonErrorBodyIsTruncated() {
        server.enqueue(MockResponse().setResponseCode(502).setBody("x".repeat(1000)))
        assertEquals("[502] " + "x".repeat(400), fails { runBlocking { client.generate("k", "s", "u", "m") } })
    }

    @Test fun emptyCandidatesIsAnError() {
        server.enqueue(MockResponse().setBody("""{"candidates":[]}"""))
        assertTrue(fails { runBlocking { client.generate("k", "s", "u", "m") } }.startsWith("No model response was received: "))
    }

    @Test fun nonJsonSuccessBodyIsAnError() {
        server.enqueue(MockResponse().setBody("nope"))
        assertEquals("No valid API response was received (status=200): nope", fails { runBlocking { client.generate("k", "s", "u", "m") } })
    }
}
