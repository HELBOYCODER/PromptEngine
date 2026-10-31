package com.astr0net.promptcompiler

import android.app.Application
import com.astr0net.promptcompiler.data.ConfigStore
import com.astr0net.promptcompiler.data.GeminiClient
import com.astr0net.promptcompiler.data.HistoryStore
import com.astr0net.promptcompiler.data.PrefsConfigStore
import com.astr0net.promptcompiler.data.PromptCompiler
import com.astr0net.promptcompiler.data.UsageStore
import java.io.File

open class PromptEngineApp : Application() {
    lateinit var configStore: ConfigStore
        private set
    lateinit var usageStore: UsageStore
        private set
    lateinit var historyStore: HistoryStore
        private set
    lateinit var compiler: PromptCompiler
        internal set

    override fun onCreate() {
        super.onCreate()
        configStore = PrefsConfigStore(this)
        usageStore = UsageStore(File(filesDir, "usage.json"))
        historyStore = HistoryStore(File(filesDir, "history.json"))
        compiler = PromptCompiler(
            client = GeminiClient(),
            usageStore = usageStore,
            configProvider = { configStore.load() },
            defaultSystemPrompt = { assets.open("system_prompt.txt").bufferedReader(Charsets.UTF_8).use { it.readText() } },
        )
    }
}
