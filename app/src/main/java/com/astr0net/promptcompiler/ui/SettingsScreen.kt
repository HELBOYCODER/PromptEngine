package com.astr0net.promptcompiler.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.astr0net.promptcompiler.BuildConfig
import com.astr0net.promptcompiler.R
import com.astr0net.promptcompiler.data.AppConfig
import com.astr0net.promptcompiler.data.findModel
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    config: AppConfig,
    defaultSystemPrompt: () -> String,
    onSave: (AppConfig) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var apiKey by rememberSaveable { mutableStateOf(config.geminiApiKey) }
    var model by rememberSaveable { mutableStateOf(config.geminiModel) }
    var systemPrompt by rememberSaveable { mutableStateOf(config.systemPromptOverride) }
    var language by rememberSaveable { mutableStateOf(config.language) }
    var showKey by rememberSaveable { mutableStateOf(false) }
    var pickerOpen by rememberSaveable { mutableStateOf(false) }
    var saved by rememberSaveable { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    BackHandler(onBack = onBack)
    LaunchedEffect(saved) {
        if (saved) { delay(1100); onBack() }
    }

    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostButton("‹  " + stringResource(R.string.back), onBack)
            Text(stringResource(R.string.settings), style = MaterialTheme.typography.titleLarge, color = Neon.Text, modifier = Modifier.weight(1f))
        }

        GlassCard {
            Text(stringResource(R.string.settings_google_title), style = MaterialTheme.typography.titleSmall, color = Neon.Accent)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.settings_google_note), style = MaterialTheme.typography.bodySmall, color = Neon.TextDim)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.settings_get_key),
                Modifier.clickable { uriHandler.openUri("https://aistudio.google.com/apikey") }.padding(vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall, color = Neon.Accent2,
            )
            Spacer(Modifier.height(6.dp))
            FieldLabel(stringResource(R.string.settings_api_key))
            FieldBox {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = apiKey, onValueChange = { apiKey = it.filterNot(Char::isWhitespace) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Neon.Text, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, textDirection = TextDirection.Ltr),
                        cursorBrush = SolidColor(Neon.Accent),
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        decorationBox = { inner -> if (apiKey.isEmpty()) Text("AIza...", color = Neon.TextDim, style = MaterialTheme.typography.bodyMedium); inner() },
                    )
                    Text(
                        if (showKey) stringResource(R.string.settings_hide_key) else stringResource(R.string.settings_show_key),
                        Modifier.clickable { showKey = !showKey }.padding(start = 10.dp),
                        style = MaterialTheme.typography.labelSmall, color = Neon.Accent,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            FieldLabel(stringResource(R.string.settings_default_model))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Neon.Surface2).border(1.dp, Neon.Border, RoundedCornerShape(14.dp))
                    .clickable { pickerOpen = true }.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(findModel(model)?.name ?: model, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Neon.Text)
                Text("⌄", color = Neon.Accent)
            }
        }

        GlassCard {
            FieldLabel(stringResource(R.string.settings_system_prompt))
            FieldBox {
                BasicTextField(
                    value = systemPrompt, onValueChange = { systemPrompt = it },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Neon.Text, textDirection = TextDirection.Content),
                    cursorBrush = SolidColor(Neon.Accent),
                    decorationBox = { inner -> if (systemPrompt.isEmpty()) Text(stringResource(R.string.settings_system_prompt_hint), color = Neon.TextDim, style = MaterialTheme.typography.bodySmall); inner() },
                )
            }
            Spacer(Modifier.height(10.dp))
            GhostButton(stringResource(R.string.settings_system_prompt_default), { systemPrompt = defaultSystemPrompt() })
        }

        GlassCard {
            FieldLabel(stringResource(R.string.settings_language))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LangChip(stringResource(R.string.settings_lang_system), language == AppConfig.LANG_SYSTEM) { language = AppConfig.LANG_SYSTEM }
                LangChip(stringResource(R.string.settings_lang_fa), language == AppConfig.LANG_FA) { language = AppConfig.LANG_FA }
                LangChip(stringResource(R.string.settings_lang_en), language == AppConfig.LANG_EN) { language = AppConfig.LANG_EN }
            }
        }

        GlassCard {
            Text("🔒  " + stringResource(R.string.settings_permissions_title), style = MaterialTheme.typography.titleSmall, color = Neon.Success)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.settings_permissions_body), style = MaterialTheme.typography.bodySmall, color = Neon.TextDim)
        }

        GradientButton(
            stringResource(if (saved) R.string.settings_saved else R.string.settings_save),
            onClick = {
                onSave(AppConfig(apiKey.trim(), model, systemPrompt, language))
                saved = true
            },
            enabled = !saved,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.labelSmall, color = Neon.TextDim)
            Text(stringResource(R.string.settings_source), style = MaterialTheme.typography.labelSmall, color = Neon.TextDim)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (pickerOpen) ModelPickerSheet(selectedId = model, onSelect = { model = it; pickerOpen = false }, onDismiss = { pickerOpen = false })
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, Modifier.padding(bottom = 6.dp), style = MaterialTheme.typography.labelMedium, color = Neon.TextDim)
}

@Composable
private fun FieldBox(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Neon.Surface2).border(1.dp, Neon.Border, RoundedCornerShape(14.dp)).padding(14.dp),
    ) { content() }
}

@Composable
private fun LangChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Text(
        label,
        Modifier.clip(shape)
            .background(if (selected) Neon.Accent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.04f))
            .border(1.dp, if (selected) Neon.BorderStrong else Neon.Border, shape)
            .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        style = MaterialTheme.typography.labelMedium, color = if (selected) Neon.Accent else Neon.Text,
    )
}
