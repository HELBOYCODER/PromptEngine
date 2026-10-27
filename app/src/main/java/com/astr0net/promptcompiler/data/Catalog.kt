package com.astr0net.promptcompiler.data

/** Supported Google AI Studio model catalog (mirrors prompt_compiler/catalog.py). */
data class ModelInfo(
    val id: String,
    val name: String,
    val category: String,
    val rpm: Int?,
    val tpm: Int?,
    val rpd: Int?,
    val generate: Boolean,
)

const val DEFAULT_MODEL = "gemini-3.5-flash"

val KNOWN_CATEGORIES = setOf(
    "coding", "debugging", "research", "writing", "image_generation",
    "translation", "data_analysis", "planning", "automation", "general",
)

val MODELS: List<ModelInfo> = listOf(
    ModelInfo("gemini-3.5-flash", "Gemini 3.5 Flash", "Text-out models", 5, 250000, 20, true),
    ModelInfo("gemini-3.8-flash", "Gemini 3.8 Flash", "Text-out models", 5, 250000, 20, true),
    ModelInfo("gemini-3.7-flash", "Gemini 3.7 Flash", "Text-out models", 5, 250000, 20, true),
    ModelInfo("gemini-3.6-flash", "Gemini 3.6 Flash", "Text-out models", 5, 250000, 20, true),
    ModelInfo("gemini-3.5-flash-lite", "Gemini 3.5 Flash Lite", "Text-out models", 15, 250000, 500, true),
    ModelInfo("gemini-3.1-pro", "Gemini 3.1 Pro", "Text-out models", null, null, null, true),
    ModelInfo("gemini-3.1-flash-lite", "Gemini 3.1 Flash Lite", "Text-out models", 15, 250000, 500, true),
    ModelInfo("gemini-3-flash", "Gemini 3 Flash", "Text-out models", 5, 250000, 20, true),
    ModelInfo("gemini-2.5-pro", "Gemini 2.5 Pro", "Text-out models", null, null, null, true),
    ModelInfo("gemini-2.5-flash-lite", "Gemini 2.5 Flash Lite", "Text-out models", 10, 250000, 20, true),
    ModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash", "Text-out models", 5, 250000, 20, true),
    ModelInfo("gemini-2.0-flash-lite", "Gemini 2 Flash Lite", "Text-out models", null, null, null, true),
    ModelInfo("gemini-2.0-flash", "Gemini 2 Flash", "Text-out models", null, null, null, true),
    ModelInfo("antigravity", "Antigravity", "Agents", 60, 100000, 100, false),
    ModelInfo("deep-research-pro", "Deep Research Pro Preview", "Agents", null, null, null, false),
    ModelInfo("computer-use-preview", "Computer Use Preview", "Other models", null, null, null, false),
    ModelInfo("gemini-2.5-flash-preview-image", "Nano Banana (Gemini 2.5 Flash Preview Image)", "Multi-modal generative models", null, null, null, false),
    ModelInfo("gemini-2.5-flash-tts", "Gemini 2.5 Flash TTS", "Multi-modal generative models", 3, 10000, 10, false),
    ModelInfo("gemini-2.5-pro-tts", "Gemini 2.5 Pro TTS", "Multi-modal generative models", null, null, null, false),
    ModelInfo("gemini-3-pro-image", "Nano Banana Pro (Gemini 3 Pro Image)", "Multi-modal generative models", null, null, null, false),
    ModelInfo("gemini-3.1-flash", "Nano Banana 2 (Gemini 3.1 Flash Image)", "Multi-modal generative models", null, null, null, false),
    ModelInfo("gemini-3.1-flash-lite-image", "Nano Banana 2 Lite (Gemini 3.1 Flash Lite Image)", "Multi-modal generative models", null, null, null, false),
    ModelInfo("gemini-3.1-flash-tts", "Gemini 3.1 Flash TTS", "Multi-modal generative models", 3, 10000, 10, false),
    ModelInfo("gemini-3.5-transcribe", "Gemini 3.5 Transcribe", "Live API", 3, 10000, 25, false),
    ModelInfo("gemini-embedding-001", "Gemini Embedding 1", "Other models", 100, 30000, 1000, false),
    ModelInfo("gemini-embedding-2", "Gemini Embedding 2", "Other models", 100, 30000, 1000, false),
    ModelInfo("gemini-omni-1.1-flash", "Gemini Omni 1.1 Flash", "Multi-modal generative models", null, null, null, false),
    ModelInfo("gemini-omni-flash", "Gemini Omni Flash", "Multi-modal generative models", null, null, null, false),
    ModelInfo("gemini-robotics-er-2-preview", "Gemini Robotics ER 2 Preview", "Other models", 5, 250000, 20, false),
    ModelInfo("gemma-4-26b", "Gemma 4 26B", "Other models", 30, 16000, 14400, false),
    ModelInfo("gemma-4-31b", "Gemma 4 31B", "Other models", 30, 16000, 14400, false),
    ModelInfo("lyria-3-clip", "Lyria 3 Clip", "Multi-modal generative models", null, null, null, false),
    ModelInfo("lyria-3-pro", "Lyria 3 Pro", "Multi-modal generative models", null, null, null, false),
    ModelInfo("veo-3-fast-generate", "Veo 3 Fast Generate", "Multi-modal generative models", null, null, null, false),
    ModelInfo("veo-3-generate", "Veo 3 Generate", "Multi-modal generative models", null, null, null, false),
    ModelInfo("veo-3-lite-generate", "Veo 3 Lite Generate", "Multi-modal generative models", null, null, null, false),
    ModelInfo("gemini-2.5-flash-native-audio-dialog", "Gemini 2.5 Flash Native Audio Dialog", "Live API", null, 1000000, null, false),
    ModelInfo("gemini-3-flash-live", "Gemini 3 Flash Live", "Live API", null, 65000, null, false),
    ModelInfo("gemini-3.5-live-translate", "Gemini 3.5 Live Translate", "Live API", null, 20000, null, false),
    ModelInfo("gemini-3.5-transcribe-live", "Gemini 3.5 Transcribe Live", "Live API", null, 20000, null, false),
    ModelInfo("gemini-3.8-live", "Gemini 3.8 Live", "Live API", null, 65000, null, false),
    ModelInfo("gemini-3.8-live-extended-thinking", "Gemini 3.8 Live Extended Thinking", "Live API", null, 65000, null, false),
)

fun findModel(id: String): ModelInfo? = MODELS.firstOrNull { it.id == id }

/** Models usable with generateContent, in catalog order. */
val GENERATE_MODELS: List<ModelInfo> = MODELS.filter { it.generate }
