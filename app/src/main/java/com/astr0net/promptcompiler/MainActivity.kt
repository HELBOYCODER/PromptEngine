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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.astr0net.promptcompiler.ui.BottomNav
import com.astr0net.promptcompiler.ui.EditorScreen
import com.astr0net.promptcompiler.ui.HistoryScreen
import com.astr0net.promptcompiler.ui.InkBackground
import com.astr0net.promptcompiler.ui.NavItem
import com.astr0net.promptcompiler.ui.PromptEngineTheme
import com.astr0net.promptcompiler.ui.SettingsScreen
import com.astr0net.promptcompiler.ui.ToastBubble
import com.astr0net.promptcompiler.ui.UsageScreen
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

                InkBackground {
                    val imeOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
                    val navItems = listOf(
                        NavItem(stringResource(R.string.nav_editor), Icons.Outlined.Edit, Icons.Rounded.Edit),
                        NavItem(stringResource(R.string.nav_history), Icons.Outlined.History, Icons.Rounded.History),
                        NavItem(stringResource(R.string.nav_usage), Icons.Outlined.BarChart, Icons.Rounded.BarChart),
                        NavItem(stringResource(R.string.settings), Icons.Outlined.Settings, Icons.Rounded.Settings),
                    )
                    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
                        Column(Modifier.fillMaxSize()) {
                            AnimatedContent(
                                targetState = state.screen,
                                modifier = Modifier.weight(1f),
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "screen",
                            ) { screen ->
                                val m = Modifier.fillMaxSize()
                                when (screen) {
                                    Screen.Editor -> EditorScreen(state, vm, m)
                                    Screen.History -> HistoryScreen(state, vm, m)
                                    Screen.Usage -> UsageScreen(state, vm, m)
                                    Screen.Settings -> SettingsScreen(
                                        config = state.config,
                                        defaultSystemPrompt = { assets.open("system_prompt.txt").bufferedReader().use { it.readText() } },
                                        onSave = { vm.saveConfig(it); vm.toast(Msg.Res(R.string.settings_saved)) },
                                        modifier = m,
                                    )
                                }
                            }
                            if (!imeOpen) BottomNav(navItems, state.screen.ordinal, { vm.open(Screen.entries[it]) })
                        }
                        AnimatedVisibility(
                            toast != null,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 16.dp, end = 16.dp, bottom = if (imeOpen) 80.dp else 96.dp),
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                        ) { ToastBubble(toast.orEmpty()) }
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
