package com.astr0net.promptcompiler.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.astr0net.promptcompiler.BuildConfig
import com.astr0net.promptcompiler.R
import com.astr0net.promptcompiler.data.AppConfig
import com.astr0net.promptcompiler.data.findModel

@Composable
fun SettingsScreen(config: AppConfig, defaultSystemPrompt: () -> String, onSave: (AppConfig) -> Unit, modifier: Modifier = Modifier) {
    var apiKey by rememberSaveable(config.geminiApiKey) { mutableStateOf(config.geminiApiKey) }
    var model by rememberSaveable(config.geminiModel) { mutableStateOf(config.geminiModel) }
    var systemPrompt by rememberSaveable(config.systemPromptOverride) { mutableStateOf(config.systemPromptOverride) }
    var language by rememberSaveable(config.language) { mutableStateOf(config.language) }
    var showKey by rememberSaveable { mutableStateOf(false) }
    var pickerOpen by rememberSaveable { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val draft = AppConfig(apiKey.trim(), model, systemPrompt, language)
    val dirty = draft != config

    Box(modifier) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ScreenTitle(stringResource(R.string.settings))

            Group(stringResource(R.string.settings_group_connection)) {
                Text(stringResource(R.string.settings_google_note), style = MaterialTheme.typography.bodySmall, color = Ink.Dim)
                Row(Modifier.pressable { uriHandler.openUri("https://aistudio.google.com/apikey") }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.settings_get_key), style = MaterialTheme.typography.labelMedium, color = Ink.Violet)
                    Icon(Icons.Rounded.OpenInNew, null, tint = Ink.Violet, modifier = Modifier.size(14.dp))
                }
                Field(stringResource(R.string.settings_api_key)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = apiKey, onValueChange = { apiKey = it.filterNot(Char::isWhitespace) }, singleLine = true, modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink.Text, fontFamily = FontFamily.Monospace, textDirection = TextDirection.Ltr),
                            cursorBrush = SolidColor(Ink.Cyan),
                            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                            decorationBox = { inner -> if (apiKey.isEmpty()) Text("AIza...", color = Ink.Faint, style = MaterialTheme.typography.bodyMedium); inner() },
                        )
                        IconAction(if (showKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, stringResource(if (showKey) R.string.settings_hide_key else R.string.settings_show_key), { showKey = !showKey })
                    }
                }
            }

            Group(stringResource(R.string.settings_group_compiler)) {
                Field(stringResource(R.string.settings_default_model)) {
                    Row(Modifier.fillMaxWidth().pressable { pickerOpen = true }, verticalAlignment = Alignment.CenterVertically) {
                        Text(findModel(model)?.name ?: model, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Ink.Text)
                        Icon(Icons.Rounded.ExpandMore, null, tint = Ink.Cyan)
                    }
                }
                Field(stringResource(R.string.settings_system_prompt)) {
                    BasicTextField(
                        value = systemPrompt, onValueChange = { systemPrompt = it }, modifier = Modifier.fillMaxWidth().height(130.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = Ink.Text, textDirection = TextDirection.Content),
                        cursorBrush = SolidColor(Ink.Cyan),
                        decorationBox = { inner -> if (systemPrompt.isEmpty()) Text(stringResource(R.string.settings_system_prompt_hint), color = Ink.Faint, style = MaterialTheme.typography.bodySmall); inner() },
                    )
                }
                TonalButton(stringResource(R.string.settings_system_prompt_default), { systemPrompt = defaultSystemPrompt() })
            }

            Group(stringResource(R.string.settings_group_app)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Language, null, tint = Ink.Dim, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.labelMedium, color = Ink.Dim)
                }
                val langs = listOf(AppConfig.LANG_SYSTEM to R.string.settings_lang_system, AppConfig.LANG_FA to R.string.settings_lang_fa, AppConfig.LANG_EN to R.string.settings_lang_en)
                SegmentedControl(langs.map { stringResource(it.second) }, langs.indexOfFirst { it.first == language }.coerceAtLeast(0), { language = langs[it].first })
            }

            Group(stringResource(R.string.settings_permissions_title)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Lock, null, tint = Ink.Mint, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.settings_permissions_body), style = MaterialTheme.typography.bodySmall, color = Ink.Dim)
                }
            }

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.labelSmall, color = Ink.Faint)
                Text(stringResource(R.string.settings_source), style = MaterialTheme.typography.labelSmall, color = Ink.Faint)
            }
        }

        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, Ink.Bg.copy(alpha = 0.92f), Ink.Bg)))
                .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp),
        ) { PrimaryButton(stringResource(R.string.settings_save), { onSave(draft) }, enabled = dirty) }
    }

    if (pickerOpen) ModelPickerSheet(selectedId = model, onSelect = { model = it; pickerOpen = false }, onDismiss = { pickerOpen = false })
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Eyebrow(title, Modifier.padding(start = 4.dp), Ink.Cyan)
        InkCard { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { content() } }
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Ink.Dim)
        Box(Modifier.fillMaxWidth().clip(RMd).background(Ink.S2).border(1.dp, Ink.Line, RMd).padding(horizontal = 14.dp, vertical = 12.dp)) { content() }
    }
}
