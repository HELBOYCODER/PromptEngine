package com.astr0net.promptcompiler.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.astr0net.promptcompiler.MainViewModel
import com.astr0net.promptcompiler.Msg
import com.astr0net.promptcompiler.R
import com.astr0net.promptcompiler.Screen
import com.astr0net.promptcompiler.UiState
import com.astr0net.promptcompiler.data.ParsedResult
import com.astr0net.promptcompiler.data.findModel
import androidx.compose.foundation.Image

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(state: UiState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var pickerOpen by remember { mutableStateOf(false) }

    fun copy(text: String, message: Int) {
        clipboard.setText(AnnotatedString(text))
        vm.toast(Msg.Res(message))
    }

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TopBar(state, onSettings = { vm.open(Screen.Settings) }, onUsage = { vm.setUsageOpen(true) }, onModel = { pickerOpen = true })

        AnimatedVisibility(state.config.geminiApiKey.isBlank(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            NoKeyBanner(onOpen = { vm.open(Screen.Settings) })
        }

        InputCard(state, vm, onPickModel = { pickerOpen = true }, onPaste = {
            clipboard.getText()?.text?.takeIf { it.isNotBlank() }?.let { vm.setInput(it) }
        })

        AnimatedVisibility(state.loading, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { CompilingBar() }

        AnimatedVisibility(state.result != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            state.result?.let { r ->
                ResultCard(
                    r,
                    onCopyEn = { copy(r.englishPrompt, R.string.toast_copied_en) },
                    onCopyFa = { copy(r.persianPrompt, R.string.toast_copied_fa) },
                    onAgent = { name ->
                        clipboard.setText(AnnotatedString(r.englishPrompt))
                        vm.toast(Msg.Res(R.string.toast_agent_copied, listOf(name)))
                    },
                    onShare = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, r.englishPrompt)
                        }
                        context.startActivity(Intent.createChooser(send, null))
                    },
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (pickerOpen) {
        ModelPickerSheet(selectedId = state.modelId, onSelect = { vm.selectModel(it); pickerOpen = false }, onDismiss = { pickerOpen = false })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopBar(state: UiState, onSettings: () -> Unit, onUsage: () -> Unit, onModel: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Neon.BorderStrong, RoundedCornerShape(14.dp)),
            ) {
                Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.matchParentSize())
                Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.matchParentSize().padding(0.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, color = Neon.Text)
                Text(stringResource(R.string.brand_sub), style = MaterialTheme.typography.labelSmall, color = Neon.TextDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            GhostButton(stringResource(R.string.settings), onSettings)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.clip(CircleShape).background(Neon.Success.copy(alpha = 0.1f)).border(1.dp, Neon.Success.copy(alpha = 0.35f), CircleShape).padding(horizontal = 11.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                LiveDot()
                Text(stringResource(R.string.google_ai_studio), style = MaterialTheme.typography.labelSmall, color = Neon.Success)
            }
            Pill(findModel(state.modelId)?.name ?: state.modelId, Modifier.clickable(onClick = onModel), Neon.Accent)
            val u = state.usage?.today
            Pill(
                stringResource(R.string.usage_title) + " · " + stringResource(R.string.unit_tokens, formatTokens(u?.total ?: 0)) + " · " + stringResource(R.string.unit_requests, fmtInt(u?.requests ?: 0)),
                Modifier.clickable(onClick = onUsage), Neon.Accent2,
            )
        }
    }
}

@Composable
private fun NoKeyBanner(onOpen: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(Neon.Warn.copy(alpha = 0.14f), Neon.Accent3.copy(alpha = 0.10f))))
            .border(1.dp, Neon.Warn.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("🔑  " + stringResource(R.string.no_key_title), style = MaterialTheme.typography.titleSmall, color = Neon.Warn)
        Text(stringResource(R.string.no_key_body), style = MaterialTheme.typography.bodySmall, color = Neon.Text)
        GhostButton(stringResource(R.string.no_key_action), onOpen, accent = Neon.Warn)
    }
}

@Composable
private fun InputCard(state: UiState, vm: MainViewModel, onPickModel: () -> Unit, onPaste: () -> Unit) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(stringResource(R.string.input_label), Modifier.weight(1f))
            GhostButton(stringResource(R.string.paste), onPaste)
            if (state.input.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                GhostButton(stringResource(R.string.clear), { vm.setInput("") })
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier.fillMaxWidth().heightIn(min = 170.dp).clip(RoundedCornerShape(16.dp))
                .background(Neon.Surface2).border(1.dp, Neon.Border, RoundedCornerShape(16.dp)).padding(14.dp),
        ) {
            BasicTextField(
                value = state.input,
                onValueChange = vm::setInput,
                modifier = Modifier.fillMaxWidth().heightIn(min = 142.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Neon.Text, textDirection = TextDirection.Content),
                cursorBrush = SolidColor(Neon.Accent),
                decorationBox = { inner ->
                    if (state.input.isEmpty()) {
                        Text(stringResource(R.string.input_hint), style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Content), color = Neon.TextDim.copy(alpha = 0.75f))
                    }
                    inner()
                },
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Neon.Border, RoundedCornerShape(16.dp)).clickable(onClick = onPickModel).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(findModel(state.modelId)?.name ?: state.modelId, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = Neon.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("⌄", color = Neon.Accent)
            }
            GradientButton(
                if (state.loading) stringResource(R.string.compiling) else stringResource(R.string.compile),
                onClick = vm::compile,
                enabled = !state.loading,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultCard(r: ParsedResult, onCopyEn: () -> Unit, onCopyFa: () -> Unit, onAgent: (String) -> Unit, onShare: () -> Unit) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(stringResource(R.string.result), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Pill(stringResource(resultCategoryRes(r.detectedCategory)), color = Neon.Accent3)
            if (r.detectedSubcategory.isNotEmpty()) Pill(r.detectedSubcategory, color = Neon.Accent2)
        }
        if (r.detectedPurpose.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(r.detectedPurpose, style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Content), color = Neon.TextDim)
        }
        Spacer(Modifier.height(14.dp))
        PromptBox(stringResource(R.string.persian_prompt), r.persianPrompt.ifEmpty { "—" }, stringResource(R.string.copy_persian), onCopyFa, rtl = true, copyEnabled = r.persianPrompt.isNotEmpty())
        Spacer(Modifier.height(12.dp))
        PromptBox(stringResource(R.string.english_prompt), r.englishPrompt, stringResource(R.string.copy_english), onCopyEn, rtl = false)
        Spacer(Modifier.height(14.dp))
        AnalysisCard(stringResource(R.string.requirements), r.preservedRequirements, Neon.Success)
        Spacer(Modifier.height(10.dp))
        AnalysisCard(stringResource(R.string.assumptions), r.assumptions, Neon.Warn)
        Spacer(Modifier.height(10.dp))
        AnalysisCard(stringResource(R.string.missing_info), r.missingInformation, Neon.Danger)
        Spacer(Modifier.height(10.dp))
        AnalysisCard(stringResource(R.string.suggestions), r.suggestions, Neon.Accent2)
        Spacer(Modifier.height(16.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GhostButton(stringResource(R.string.use_in_claude), { onAgent("Claude Code") })
            GhostButton(stringResource(R.string.use_in_opencode), { onAgent("OpenCode") })
            GhostButton(stringResource(R.string.share), onShare, accent = Neon.Accent)
        }
    }
}

@Composable
private fun PromptBox(label: String, text: String, copyLabel: String, onCopy: () -> Unit, rtl: Boolean, copyEnabled: Boolean = true) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Neon.Surface2).border(1.dp, Neon.Border, RoundedCornerShape(16.dp)).padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = if (rtl) Neon.Accent2 else Neon.Accent)
            GhostButton("⧉  $copyLabel", onCopy, enabled = copyEnabled)
        }
        Spacer(Modifier.height(8.dp))
        CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
            SelectionContainer {
                Text(
                    text,
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neon.Text,
                    fontFamily = if (rtl) null else FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun AnalysisCard(title: String, items: List<String>, accent: Color) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(accent.copy(alpha = 0.05f))
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(16.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = accent)
        if (items.isEmpty()) {
            Text(stringResource(R.string.nothing_recorded), style = MaterialTheme.typography.bodySmall, color = Neon.TextDim)
        } else {
            items.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.padding(top = 8.dp).size(6.dp).clip(CircleShape).background(accent))
                    Text(item, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Content), color = Neon.Text)
                }
            }
        }
    }
}
