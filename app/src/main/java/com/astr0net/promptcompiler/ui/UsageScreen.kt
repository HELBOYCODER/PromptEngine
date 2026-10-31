package com.astr0net.promptcompiler.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.astr0net.promptcompiler.MainViewModel
import com.astr0net.promptcompiler.R
import com.astr0net.promptcompiler.UiState
import com.astr0net.promptcompiler.data.ModelUsage

@Composable
fun UsageScreen(state: UiState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val usage = state.usage
    Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenTitle(stringResource(R.string.usage_title)) {
            TonalButton(stringResource(R.string.usage_reset), vm::resetUsage, icon = Icons.Rounded.DeleteOutline, color = Ink.Coral)
        }
        if (usage == null) return@Column
        val sel = usage.models.firstOrNull { it.info.id == state.modelId }

        if (sel != null) InkCard {
            Eyebrow(stringResource(R.string.usage_selected_model, sel.info.name), color = Ink.Cyan)
            Box(Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) { Ring(sel) }
            Meter(stringResource(R.string.meter_rpd), sel.todayRequests, sel.info.rpd)
            Meter(stringResource(R.string.meter_tpm), sel.minuteTokens, sel.info.tpm)
            Meter(stringResource(R.string.meter_rpm), sel.minuteRequests, sel.info.rpm)
        }

        val t = usage.today
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(fmtInt(t.requests), stringResource(R.string.usage_requests_today), Modifier.weight(1f))
            StatTile(formatTokens(t.total), stringResource(R.string.usage_total), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(formatTokens(t.input), stringResource(R.string.usage_input), Modifier.weight(1f))
            StatTile(formatTokens(t.output), stringResource(R.string.usage_output), Modifier.weight(1f))
        }

        val used = usage.models.filter { it.todayRequests > 0 }.sortedByDescending { it.todayTokens }
        if (used.isEmpty()) {
            Text(stringResource(R.string.usage_empty), style = MaterialTheme.typography.bodySmall, color = Ink.Dim)
        } else {
            Eyebrow(stringResource(R.string.usage_models_today))
            used.forEach { UsedRow(it) }
        }
    }
}

@Composable
private fun Ring(m: ModelUsage) {
    val limit = m.info.rpd
    val target = if (limit != null && limit > 0) (m.todayRequests.toFloat() / limit).coerceIn(0f, 1f) else 0f
    val sweep by animateFloatAsState(target, tween(800), label = "ring")
    val warn = target >= 0.9f
    Box(Modifier.size(168.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(168.dp)) {
            val stroke = 14.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(Ink.S3, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            if (sweep > 0f) drawArc(
                if (warn) Brush.sweepGradient(listOf(Ink.Amber, Ink.Coral, Ink.Magenta)) else Brush.sweepGradient(listOf(Ink.Violet, Ink.Cyan, Ink.Violet)),
                -90f, 360f * sweep, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(fmtInt(m.todayRequests), style = MaterialTheme.typography.displaySmall, color = Ink.Text)
            Text(
                if (limit != null) "/ ${fmtInt(limit)} " + stringResource(R.string.usage_ring_of) else stringResource(R.string.usage_ring_nocap),
                Modifier.padding(horizontal = 24.dp), style = MaterialTheme.typography.labelSmall, color = Ink.Dim, textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Meter(label: String, used: Int, limit: Int?) {
    val fraction = if (limit != null && limit > 0) used.toFloat() / limit else 0f
    Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Ink.Dim)
            Text(if (limit == null) formatTokens(used) + " · " + stringResource(R.string.meter_unlimited) else fmtInt(used) + " / " + formatTokens(limit), style = MaterialTheme.typography.labelSmall, color = Ink.Text)
        }
        MeterBar(fraction)
        if (limit != null) Text(stringResource(R.string.meter_remaining, fmtInt((limit - used).coerceAtLeast(0))), style = MaterialTheme.typography.labelSmall, color = Ink.Faint)
    }
}

@Composable
private fun UsedRow(m: ModelUsage) {
    Row(
        Modifier.fillMaxWidth().cardRow().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(m.info.name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = Ink.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(stringResource(R.string.unit_requests, fmtInt(m.todayRequests)), style = MaterialTheme.typography.labelSmall, color = Ink.Dim)
        Text(stringResource(R.string.unit_tokens, formatTokens(m.todayTokens)), style = MaterialTheme.typography.labelSmall, color = Ink.Cyan)
    }
}

private fun Modifier.cardRow(): Modifier = this
    .then(Modifier.clip(RMd))
    .then(Modifier.background(Ink.S1))
    .then(Modifier.border(1.dp, Ink.Line, RMd))
