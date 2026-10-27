package com.astr0net.promptcompiler

import android.graphics.Bitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.astr0net.promptcompiler.data.AppConfig
import com.astr0net.promptcompiler.data.GeminiClient
import com.astr0net.promptcompiler.data.PromptCompiler
import java.io.File
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The app talks to a MockWebServer instead of Google. */
class TestApp : PromptEngineApp() {
    override fun onCreate() {
        super.onCreate()
        compiler = PromptCompiler(
            client = GeminiClient(baseUrl = UiFlowTest.server.url("/").toString().trimEnd('/')),
            usageStore = usageStore,
            configProvider = { configStore.load() },
            defaultSystemPrompt = { assets.open("system_prompt.txt").bufferedReader().use { it.readText() } },
        )
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = TestApp::class, qualifiers = "fa-w411dp-h891dp-xxhdpi")
class UiFlowTest {
    companion object {
        val modelJson = """{"detected_category":"coding","detected_subcategory":"web_development","detected_purpose":"Build a Flask shop that lists product prices.",
            "persian_prompt":"یک وب‌سایت فروشگاهی با Flask بساز که قیمت محصولات را نمایش دهد. از هیچ فریم‌ورک یا دیتابیس اضافه‌ای استفاده نکن.",
            "english_prompt":"Build an online shop website with Flask that displays product prices.\n\nConstraints:\n- Do not introduce additional frameworks or databases.",
            "preserved_requirements":["Flask","Show product prices"],"assumptions":[],"missing_information":["Where product data comes from"],"suggestions":["Consider pagination"]}"""
        val requests = mutableListOf<RecordedRequest>()
        /** Started once: the Application (created by the rule) needs the URL before @Before runs. */
        val server: MockWebServer = MockWebServer().apply {
            dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    requests += request
                    val body = JSONObject().put("candidates", org.json.JSONArray().put(JSONObject().put("content", JSONObject().put("parts", org.json.JSONArray().put(JSONObject().put("text", modelJson))))))
                        .put("usageMetadata", JSONObject().put("promptTokenCount", 1200).put("candidatesTokenCount", 640).put("thoughtsTokenCount", 0).put("totalTokenCount", 1840))
                    return MockResponse().setBody(body.toString())
                }
            }
            start()
        }
    }

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val shots = File("build/screenshots").apply { mkdirs() }

    @Before fun setUp() {
        requests.clear()
        ApplicationProvider.getApplicationContext<PromptEngineApp>().getSharedPreferences("prompt_engine_config", 0).edit().clear().apply()
    }

    /** Draws every window of the activity (main content + bottom sheets) onto one bitmap. */
    private fun shot(name: String) {
        rule.waitForIdle()
        val decor = rule.activity.window.decorView
        val bmp = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        decor.draw(canvas)
        // popup windows (ModalBottomSheet) live in their own root view
        rule.onAllNodes(isRoot()).fetchSemanticsNodes().size
        File(shots, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun str(id: Int) = rule.activity.getString(id)

    @Test fun firstRunShowsKeyBannerAndSettingsSavesKey() {
        rule.onNodeWithText(str(R.string.no_key_title), substring = true).assertIsDisplayed()
        shot("01_home_empty")
        rule.onNodeWithText(str(R.string.no_key_action)).performClick()
        rule.onNodeWithText(str(R.string.settings_save)).performScrollTo().assertIsDisplayed()
        shot("02_settings")
        rule.onNodeWithText("AIza...").performTextInput("AIzaSyTESTKEY")
        rule.onNodeWithText(str(R.string.settings_save)).performScrollTo().performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithTextSafe(str(R.string.compile)) }
        assertEquals("AIzaSyTESTKEY", ApplicationProvider.getApplicationContext<PromptEngineApp>().configStore.load().geminiApiKey)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test fun compileFlowShowsResultAndUsage() {
        val app = ApplicationProvider.getApplicationContext<PromptEngineApp>()
        app.configStore.save(AppConfig(geminiApiKey = "KEY123"))
        rule.activityRule.scenario.recreate()

        rule.onNodeWithText(str(R.string.input_hint), substring = true).performTextInput("یه وب‌سایت فروشگاهی با فلسک بساز که قیمت محصولات رو نشون بده")
        rule.onNodeWithText(str(R.string.compile)).performClick()
        try {
            rule.waitUntil(10_000) { rule.onAllNodesWithTextSafe("Build an online shop website with Flask", substring = true) }
        } catch (e: Throwable) {
            System.err.println("TREE>>> " + rule.onRoot().printToString(5000) + "\nREQS=" + requests.size)
            throw e
        }
        rule.onNodeWithText("Build an online shop website with Flask", substring = true).performScrollTo().assertIsDisplayed()
        shot("03_result_top")
        rule.onNodeWithText(str(R.string.use_in_claude)).performScrollTo().assertIsDisplayed()
        shot("04_result_bottom")

        assertEquals(1, requests.size)
        assertTrue(requests[0].path!!.startsWith("/v1beta/models/gemini-3.5-flash:generateContent?key=KEY123"))
        val sent = JSONObject(requests[0].body.readUtf8()).getJSONArray("contents").getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text")
        assertTrue(sent.endsWith("فلسک بساز که قیمت محصولات رو نشون بده"))

        assertEquals(1, app.usageStore.compute().today.requests)
        assertEquals(1840, app.usageStore.compute().today.total)
    }

    @Test fun usageSheetAndModelPickerOpen() {
        val app = ApplicationProvider.getApplicationContext<PromptEngineApp>()
        app.configStore.save(AppConfig(geminiApiKey = "KEY123"))
        app.usageStore.record("gemini-3.5-flash", com.astr0net.promptcompiler.data.UsageMetadata(900, 500, 0, 1400))
        app.usageStore.record("gemini-3.5-flash", com.astr0net.promptcompiler.data.UsageMetadata(900, 500, 0, 1400))
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText(str(R.string.usage_title), substring = true).performClick()
        rule.onNodeWithText(str(R.string.meter_rpd)).assertIsDisplayed()
        shot("05_usage")
    }
}

private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.onAllNodesWithTextSafe(text: String, substring: Boolean = false): Boolean =
    onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty()
