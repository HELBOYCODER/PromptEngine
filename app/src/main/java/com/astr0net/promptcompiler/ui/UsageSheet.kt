package com.astr0net.promptcompiler.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.astr0net.promptcompiler.R
import com.astr0net.promptcompiler.data.ModelUsage
import com.astr0net.promptcompiler.data.UsageSnapshot

/** Token usage + RPD/TPM/RPM meters for the selected model (the web sidebar). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageSheet(usage: UsageSnapshot?, selectedModelId: String, onReset: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Neon.Bg2,
        contentColor = Neon.Text,
    ) {
        Column(Modifier.navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveDot(Modifier.padding(end = 10.dp))
                Text(stringResource(R.string.usage_title), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                GhostButton(stringResource(R.string.usage_reset), onReset, accent = Neon.Danger)
            }
            if (usage == null) return@Column
            val t = usage.today
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCell(fmtInt(t.requests), stringResource(R.string.usage_requests_today), Modifier.weight(1f))
                SummaryCell(formatTokens(t.input), stringResource(R.string.usage_input), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCell(formatTokens(t.output), stringResource(R.string.usage_output), Modifier.weight(1f))
                SummaryCell(formatTokens(t.total), stringResource(R.string.usage_total), Modifier.weight(1f))
            }

            usage.models.firstOrNull { it.info.id == selectedModelId }?.let { sel ->
                GlassCard {
                    Text(stringResource(R.string.usage_selected_model, sel.info.name), style = MaterialTheme.typography.labelLarge, color = Neon.Accent)
                    Spacer(Modifier.height(12.dp))
                    Meter(stringResource(R.string.meter_rpd), sel.todayRequests, sel.info.rpd, tokens = false)
                    Spacer(Modifier.height(12.dp))
                    Meter(stringResource(R.string.meter_tpm), sel.minuteTokens, sel.info.tpm, tokens = true)
                    Spacer(Modifier.height(12.dp))
                    Meter(stringResource(R.string.meter_rpm), sel.minuteRequests, sel.info.rpm, tokens = false)
                }
            }

            val used = usage.models.filter { it.todayRequests > 0 }.sortedByDescending { it.todayTokens }
            if (used.isEmpty()) {
                Text(stringResource(R.string.usage_empty), style = MaterialTheme.typography.bodySmall, color = Neon.TextDim)
            } else {
                SectionLabel(stringResource(R.string.usage_models_today))
                used.forEach { UsedRow(it) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SummaryCell(value: String, label: String, modifier: Modifier) {
    Column(
        modifier.androidxClipCard().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Neon.Text)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Neon.TextDim)
    }
}

private fun Modifier.androidxClipCard(): Modifier = this
    .then(Modifier.fillMaxWidth())
    .then(Modifier.cardBackground())

@Composable
private fun Meter(label: String, used: Int, limit: Int?, tokens: Boolean) {
    val fraction = if (limit != null && limit > 0) used.toFloat() / limit else 0f
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Neon.TextDim, modifier = Modifier.weight(1f))
            val caption = if (limit == null) {
                formatTokens(used) + if (tokens) " " + stringResource(R.string.unit_tokens, "").trim() else ""
            } else {
                fmtInt(used) + " / " + formatTokens(limit)
            }
            Text(caption, style = MaterialTheme.typography.labelSmall, color = Neon.Text)
        }
        MeterBar(fraction)
        Text(
            if (limit == null) stringResource(R.string.meter_unlimited) else stringResource(R.string.meter_remaining, fmtInt((limit - used).coerceAtLeast(0))),
            style = MaterialTheme.typography.labelSmall, color = Neon.TextDim,
        )
    }
}

@Composable
private fun UsedRow(m: ModelUsage) {
    Row(Modifier.fillMaxWidth().cardBackground().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(m.info.name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = Neon.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(stringResource(R.string.unit_requests, fmtInt(m.todayRequests)), style = MaterialTheme.typography.labelSmall, color = Neon.TextDim)
        Text(stringResource(R.string.unit_tokens, formatTokens(m.todayTokens)), style = MaterialTheme.typography.labelSmall, color = Neon.Accent)
    }
}
