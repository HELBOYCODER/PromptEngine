package com.astr0net.promptcompiler.ui

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.astr0net.promptcompiler.MainViewModel
import com.astr0net.promptcompiler.Msg
import com.astr0net.promptcompiler.R
import com.astr0net.promptcompiler.Screen
import com.astr0net.promptcompiler.UiState
import com.astr0net.promptcompiler.data.ParsedResult
import com.astr0net.promptcompiler.data.findModel
import kotlinx.coroutines.delay

@Composable
fun BrandMark(size: androidx.compose.ui.unit.Dp = 44.dp) {
    Box(Modifier.size(size).clip(RMd).border(1.dp, Ink.Line, RMd)) {
        Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}

@Composable
fun EditorScreen(state: UiState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val clipboard = LocalClipboardManager.current
    var pickerOpen by rememberSaveable { mutableStateOf(false) }
    val hasKey = state.config.geminiApiKey.isNotBlank()
    val stages = listOf(R.string.stage_1, R.string.stage_2, R.string.stage_3, R.string.stage_4)
    var stage by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.loading) {
        stage = 0
        while (state.loading) { delay(2200); stage = (stage + 1).coerceAtMost(stages.lastIndex) }
    }
    val examples = listOf(R.string.ex1_label to R.string.ex1_text, R.string.ex2_label to R.string.ex2_text, R.string.ex3_label to R.string.ex3_text, R.string.ex4_label to R.string.ex4_text)

    Box(modifier) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BrandMark()
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, color = Ink.Text)
                    Text(stringResource(R.string.brand_sub), style = MaterialTheme.typography.labelSmall, color = Ink.Dim, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(
                    Modifier.clip(CircleShape).background((if (hasKey) Ink.Mint else Ink.Amber).copy(alpha = 0.12f)).pressable { vm.open(Screen.Settings) }.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    if (hasKey) StatusDot(Ink.Mint, pulse = true) else Icon(Icons.Rounded.Key, null, tint = Ink.Amber, modifier = Modifier.size(14.dp))
                    Text(stringResource(if (hasKey) R.string.status_key_ok else R.string.status_key_missing), style = MaterialTheme.typography.labelSmall, color = if (hasKey) Ink.Mint else Ink.Amber)
                }
            }

            AnimatedVisibility(!hasKey, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(
                    Modifier.fillMaxWidth().clip(RLg).background(Ink.Amber.copy(alpha = 0.08f)).border(1.dp, Ink.Amber.copy(alpha = 0.35f), RLg).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(stringResource(R.string.no_key_title), style = MaterialTheme.typography.titleSmall, color = Ink.Amber)
                    Text(stringResource(R.string.no_key_body), style = MaterialTheme.typography.bodySmall, color = Ink.Text)
                    TonalButton(stringResource(R.string.no_key_action), { vm.open(Screen.Settings) }, icon = Icons.Rounded.Key, color = Ink.Amber)
                }
            }

            InkCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(stringResource(R.string.input_label), Modifier.weight(1f), Ink.Cyan)
                    Text(stringResource(R.string.char_count, com.astr0net.promptcompiler.ui.fmtInt(state.input.length)), style = MaterialTheme.typography.labelSmall, color = Ink.Faint)
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().heightIn(min = 150.dp)) {
                    BasicTextField(
                        value = state.input,
                        onValueChange = vm::setInput,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink.Text, textDirection = TextDirection.Content),
                        cursorBrush = SolidColor(Ink.Cyan),
                        decorationBox = { inner ->
                            if (state.input.isEmpty()) Text(stringResource(R.string.input_hint), style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Content), color = Ink.Faint)
                            inner()
                        },
                    )
                }
                AnimatedVisibility(state.input.isEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Column(Modifier.padding(top = 4.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Eyebrow(stringResource(R.string.examples_title))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(examples) { (label, text) ->
                                val t = stringResource(text)
                                InkChip(stringResource(label), { vm.setInput(t) }, icon = Icons.Rounded.AutoAwesome)
                            }
                        }
                    }
                }
                Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconAction(Icons.Rounded.ContentPaste, stringResource(R.string.paste), { clipboard.getText()?.text?.takeIf { it.isNotBlank() }?.let(vm::setInput) })
                    if (state.input.isNotEmpty()) IconAction(Icons.Rounded.Close, stringResource(R.string.clear), { vm.setInput("") })
                    Spacer(Modifier.weight(1f))
                    InkChip(findModel(state.modelId)?.name ?: state.modelId, { pickerOpen = true }, trailing = Icons.Rounded.ExpandMore)
                }
            }

            AnimatedVisibility(state.result != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                state.result?.let { ResultCard(it, vm) }
            }
        }

        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Ink.Bg.copy(alpha = 0.92f), Ink.Bg)))
                .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp),
        ) {
            PrimaryButton(
                text = stringResource(R.string.compile), onClick = vm::compile, loading = state.loading,
                loadingText = stringResource(stages[stage]), icon = Icons.Rounded.Terminal,
            )
        }
    }

    if (pickerOpen) ModelPickerSheet(selectedId = state.modelId, onSelect = { vm.selectModel(it); pickerOpen = false }, onDismiss = { pickerOpen = false })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultCard(r: ParsedResult, vm: MainViewModel) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val persianTab = tab == 1 && r.persianPrompt.isNotEmpty()
    val text = if (persianTab) r.persianPrompt else r.englishPrompt
    val style = categoryStyle(r.detectedCategory)

    InkCard(padding = 0.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(42.dp).clip(RSm).background(style.color.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(style.icon, null, tint = style.color, modifier = Modifier.size(22.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(resultCategoryRes(r.detectedCategory)).trim().dropWhile { !it.isLetter() }, style = MaterialTheme.typography.titleSmall, color = Ink.Text)
                    if (r.detectedSubcategory.isNotEmpty()) Text(r.detectedSubcategory, style = MaterialTheme.typography.labelSmall, color = style.color)
                }
            }
            if (r.detectedPurpose.isNotEmpty()) {
                Text(r.detectedPurpose, style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Content), color = Ink.Dim)
            }
            SegmentedControl(listOf(stringResource(R.string.tab_english), stringResource(R.string.tab_persian)), tab, { tab = it })
            Box(Modifier.fillMaxWidth().clip(RMd).background(Ink.S2).border(1.dp, Ink.Line, RMd)) {
                AnimatedContent(text, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "prompt") { shown ->
                    CompositionLocalProvider(LocalLayoutDirection provides if (persianTab) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                        SelectionContainer {
                            Text(
                                shown, Modifier.fillMaxWidth().padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium, color = Ink.Text,
                                fontFamily = if (persianTab) null else FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TonalButton(
                    stringResource(R.string.copy), {
                        clipboard.setText(AnnotatedString(text))
                        vm.toast(Msg.Res(if (persianTab) R.string.toast_copied_fa else R.string.toast_copied_en))
                    },
                    Modifier.weight(1f), icon = Icons.Rounded.ContentCopy, color = Ink.Cyan,
                )
                TonalButton(stringResource(R.string.share), {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, null))
                }, Modifier.weight(1f), icon = Icons.Rounded.Share)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(R.string.use_in_claude to "Claude Code", R.string.use_in_opencode to "OpenCode").forEach { (label, agent) ->
                    TonalButton(stringResource(label), {
                        clipboard.setText(AnnotatedString(r.englishPrompt))
                        vm.toast(Msg.Res(R.string.toast_agent_copied, listOf(agent)))
                    }, icon = Icons.Rounded.Terminal)
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Analysis(stringResource(R.string.missing_info), r.missingInformation, Ink.Coral, open = r.missingInformation.isNotEmpty())
            Analysis(stringResource(R.string.requirements), r.preservedRequirements, Ink.Mint)
            Analysis(stringResource(R.string.assumptions), r.assumptions, Ink.Amber)
            Analysis(stringResource(R.string.suggestions), r.suggestions, Ink.Violet)
        }
    }
}

@Composable
private fun Analysis(title: String, items: List<String>, accent: Color, open: Boolean = false) {
    Accordion(title, items.size, accent, initiallyOpen = open) {
        if (items.isEmpty()) {
            Text(stringResource(R.string.nothing_recorded), style = MaterialTheme.typography.bodySmall, color = Ink.Faint)
        } else items.forEach {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.padding(top = 8.dp).size(5.dp).clip(CircleShape).background(accent))
                Text(it, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Content), color = Ink.Text)
            }
        }
    }
}
