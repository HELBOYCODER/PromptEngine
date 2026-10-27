package com.astr0net.promptcompiler

import com.astr0net.promptcompiler.data.AppConfig
import com.astr0net.promptcompiler.data.CompileError
import com.astr0net.promptcompiler.data.CompileOutcome
import com.astr0net.promptcompiler.data.GENERATE_MODELS
import com.astr0net.promptcompiler.data.GeminiClient
import com.astr0net.promptcompiler.data.MODELS
import com.astr0net.promptcompiler.data.PromptCompiler
import com.astr0net.promptcompiler.data.UsageMetadata
import com.astr0net.promptcompiler.data.UsageStore
import java.io.File
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CompilerAndUsageTest {
    @get:Rule val tmp = TemporaryFolder()
    private lateinit var server: MockWebServer
    private lateinit var usage: UsageStore
    private var now = 1_800_000_000_000L
    private var config = AppConfig(geminiApiKey = "KEY")

    private fun compiler() = PromptCompiler(
        GeminiClient(baseUrl = server.url("/").toString().trimEnd('/')), usage, { config }, { "DEFAULT_SYSTEM" },
    )

    @Before fun setUp() {
        server = MockWebServer().apply { start() }
        usage = UsageStore(File(tmp.root, "usage.json")) { now }
    }

    @After fun tearDown() = server.shutdown()

    private val okBody = """{"candidates":[{"content":{"parts":[{"text":"{\"detected_category\":\"coding\",\"english_prompt\":\"Do X.\"}"}]}}],
        "usageMetadata":{"promptTokenCount":100,"candidatesTokenCount":50,"thoughtsTokenCount":0,"totalTokenCount":150}}"""

    @Test fun catalogMatchesReference() {
        assertEquals(42, MODELS.size)
        assertEquals(13, GENERATE_MODELS.size)
        assertTrue(GENERATE_MODELS.all { it.category == "Text-out models" })
    }

    @Test fun successRecordsUsageAndParses() = runBlocking {
        server.enqueue(MockResponse().setBody(okBody))
        val out = compiler().compile("سلام", "gemini-3.5-flash") as CompileOutcome.Success
        assertEquals("Do X.", out.parsed.englishPrompt)
        val snap = usage.compute()
        assertEquals(1, snap.today.requests)
        assertEquals(150, snap.today.total)
        val m = snap.models.first { it.info.id == "gemini-3.5-flash" }
        assertEquals(1, m.todayRequests); assertEquals(1, m.minuteRequests); assertEquals(150, m.minuteTokens)
    }

    @Test fun usesDefaultSystemPromptUnlessOverridden() = runBlocking {
        server.enqueue(MockResponse().setBody(okBody)); server.enqueue(MockResponse().setBody(okBody))
        fun sys() = JSONObject(server.takeRequest().body.readUtf8()).getJSONObject("system_instruction").getJSONArray("parts").getJSONObject(0).getString("text")
        compiler().compile("a", "gemini-3.5-flash"); assertEquals("DEFAULT_SYSTEM", sys())
        config = config.copy(systemPromptOverride = "MINE")
        compiler().compile("a", "gemini-3.5-flash"); assertEquals("MINE", sys())
    }

    @Test fun blankModelFallsBackToConfigDefault() = runBlocking {
        server.enqueue(MockResponse().setBody(okBody))
        config = config.copy(geminiModel = "gemini-2.5-pro")
        val out = compiler().compile("a", "  ") as CompileOutcome.Success
        assertEquals("gemini-2.5-pro", out.modelId)
    }

    @Test fun rejectsEmptyPromptAndNonGenerateModel() = runBlocking {
        assertEquals(CompileError.EmptyPrompt, (compiler().compile("   ", "gemini-3.5-flash") as CompileOutcome.Failure).error)
        val e = (compiler().compile("x", "veo-3-generate") as CompileOutcome.Failure).error as CompileError.NotGenerateModel
        assertEquals("Veo 3 Generate", e.name)
        assertEquals(0, server.requestCount)
    }

    @Test fun apiErrorIsSurfacedAndNothingRecorded() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(429).setBody("""{"error":{"code":429,"message":"Quota exceeded"}}"""))
        val e = (compiler().compile("x", "gemini-3.5-flash") as CompileOutcome.Failure).error
        assertEquals(CompileError.Api("[429] Quota exceeded"), e)
        assertEquals(0, usage.compute().today.requests)
    }

    @Test fun networkFailureIsClassified() = runBlocking {
        server.shutdown()
        val e = (compiler().compile("x", "gemini-3.5-flash") as CompileOutcome.Failure).error
        assertTrue(e is CompileError.Network)
    }

    @Test fun usageWindowsAndRetention() {
        val day = 24L * 3600 * 1000
        usage.record("gemini-3.5-flash", UsageMetadata(1, 2, 3, 6))
        now += 30_000
        usage.record("gemini-3.5-flash", UsageMetadata(10, 20, 30, 60))
        assertEquals(2, usage.compute().models.first { it.info.id == "gemini-3.5-flash" }.minuteRequests)
        now += 61_000
        assertEquals(0, usage.compute().models.first { it.info.id == "gemini-3.5-flash" }.minuteRequests)
        // an event older than 7 days is dropped at the next write
        now += 8 * day
        usage.record("gemini-2.5-pro", UsageMetadata(1, 1, 0, 2))
        assertEquals(1, usage.load().size)
        assertEquals(1, usage.compute().today.requests)
    }

    @Test fun nullUsageIsIgnored() {
        usage.record("gemini-3.5-flash", null)
        assertEquals(0, usage.load().size)
    }
}
