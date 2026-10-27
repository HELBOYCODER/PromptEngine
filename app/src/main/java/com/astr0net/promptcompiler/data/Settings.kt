package com.astr0net.promptcompiler.data

import android.content.Context
import android.content.SharedPreferences

/** Persisted user settings (mirrors config.json: key, default model, system-prompt override). */
data class AppConfig(
    val geminiApiKey: String = "",
    val geminiModel: String = DEFAULT_MODEL,
    val systemPromptOverride: String = "",
    val language: String = LANG_SYSTEM,
) {
    companion object {
        const val LANG_FA = "fa"
        const val LANG_EN = "en"
        const val LANG_SYSTEM = "system"
    }
}

interface ConfigStore {
    fun load(): AppConfig
    fun save(config: AppConfig)
}

class PrefsConfigStore(context: Context) : ConfigStore {
    private val prefs: SharedPreferences = context.getSharedPreferences("prompt_engine_config", Context.MODE_PRIVATE)

    override fun load() = AppConfig(
        geminiApiKey = prefs.getString("gemini_api_key", "").orEmpty(),
        geminiModel = prefs.getString("gemini_model", DEFAULT_MODEL).orEmpty().ifEmpty { DEFAULT_MODEL },
        systemPromptOverride = prefs.getString("system_prompt_override", "").orEmpty(),
        language = prefs.getString("language", AppConfig.LANG_SYSTEM).orEmpty(),
    )

    override fun save(config: AppConfig) {
        prefs.edit()
            .putString("gemini_api_key", config.geminiApiKey.trim())
            .putString("gemini_model", config.geminiModel.trim().ifEmpty { DEFAULT_MODEL })
            .putString("system_prompt_override", config.systemPromptOverride)
            .putString("language", config.language)
            .apply()
    }
}
