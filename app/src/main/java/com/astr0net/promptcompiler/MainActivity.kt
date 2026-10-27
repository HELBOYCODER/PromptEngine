package com.astr0net.promptcompiler

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.astr0net.promptcompiler.data.AppConfig
import com.astr0net.promptcompiler.ui.MainScreen
import com.astr0net.promptcompiler.ui.Neon
import com.astr0net.promptcompiler.ui.NeonBackground
import com.astr0net.promptcompiler.ui.PromptEngineTheme
import com.astr0net.promptcompiler.ui.SettingsScreen
import com.astr0net.promptcompiler.ui.UsageSheet
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    private val vm: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleShare(intent)

        setContent {
            PromptEngineTheme {
                val state by vm.state.collectAsStateWithLifecycle()
                val context = LocalContext.current
                var toast by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(state.config.language) { applyLanguage(state.config.language) }
                LaunchedEffect(Unit) {
                    vm.messages.collect { msg ->
                        toast = when (msg) {
                            is Msg.Res -> context.getString(msg.id, *msg.args.toTypedArray())
                            is Msg.Raw -> msg.text
                        }
                    }
                }
                LaunchedEffect(toast) {
                    if (toast != null) { delay(3200); toast = null }
                }

                NeonBackground {
                    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars).imePadding()) {
                        AnimatedContent(
                            targetState = state.screen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen",
                        ) { screen ->
                            when (screen) {
                                Screen.Main -> MainScreen(state, vm, Modifier.fillMaxSize())
                                Screen.Settings -> SettingsScreen(
                                    config = state.config,
                                    defaultSystemPrompt = { assets.open("system_prompt.txt").bufferedReader().use { it.readText() } },
                                    onSave = vm::saveConfig,
                                    onBack = { vm.open(Screen.Main) },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        AnimatedVisibility(
                            toast != null,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).navigationBarsPadding(),
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                        ) {
                            Text(
                                toast.orEmpty(),
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Neon.Bg2)
                                    .border(1.dp, Neon.BorderStrong, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 14.dp),
                                style = MaterialTheme.typography.bodyMedium, color = Neon.Text,
                            )
                        }
                    }
                    if (state.usageOpen) {
                        UsageSheet(state.usage, state.modelId, onReset = vm::resetUsage, onDismiss = { vm.setUsageOpen(false) })
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShare(intent)
    }

    /** Text shared from other apps becomes the Persian prompt. */
    private fun handleShare(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }?.let { vm.setInput(it) }
        }
    }

    private fun applyLanguage(language: String) {
        val wanted = if (language == AppConfig.LANG_SYSTEM) "" else language
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != wanted) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(wanted))
        }
    }
}
