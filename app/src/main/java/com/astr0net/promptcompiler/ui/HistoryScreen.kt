package com.astr0net.promptcompiler.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.astr0net.promptcompiler.MainViewModel
import com.astr0net.promptcompiler.UiState
import com.astr0net.promptcompiler.R
import com.astr0net.promptcompiler.data.HistoryItem
import com.astr0net.promptcompiler.data.findModel

@Composable
fun HistoryScreen(state: UiState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var confirm by remember { mutableStateOf(false) }
    val now = System.currentTimeMillis()
    Column(modifier.padding(horizontal = 16.dp).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle(stringResource(R.string.history_title)) {
            if (state.history.isNotEmpty()) TonalButton(stringResource(R.string.history_clear), { confirm = true }, icon = Icons.Rounded.DeleteOutline, color = Ink.Coral)
        }
        if (state.history.isEmpty()) {
            EmptyState(Icons.Rounded.History, stringResource(R.string.history_empty_title), stringResource(R.string.history_empty_body))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)) {
                items(state.history, key = { it.id }) { item -> HistoryRow(item, now, onOpen = { vm.openHistoryItem(item) }, onDelete = { vm.deleteHistory(item.id) }) }
            }
        }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        containerColor = Ink.S1,
        title = { Text(stringResource(R.string.history_clear), color = Ink.Text) },
        text = { Text(stringResource(R.string.history_clear_confirm), color = Ink.Dim) },
        confirmButton = { TextButton({ vm.clearHistory(); confirm = false }) { Text(stringResource(R.string.history_delete), color = Ink.Coral) } },
        dismissButton = { TextButton({ confirm = false }) { Text(stringResource(R.string.cancel), color = Ink.Dim) } },
    )
}

@Composable
private fun HistoryRow(item: HistoryItem, now: Long, onOpen: () -> Unit, onDelete: () -> Unit) {
    val style = categoryStyle(item.result.detectedCategory)
    val (timeRes, n) = relativeTime(item.timeMs, now)
    Column(
        Modifier.fillMaxWidth().clip(RLg).background(Ink.S1).border(1.dp, Ink.Line, RLg).pressable(onClick = onOpen).padding(start = 16.dp, top = 14.dp, end = 6.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tag(stringResource(resultCategoryRes(item.result.detectedCategory)).dropWhile { !it.isLetter() }, style.color, icon = style.icon)
            Text(if (n == null) stringResource(timeRes) else stringResource(timeRes, n), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Ink.Faint)
            IconAction(Icons.Rounded.DeleteOutline, stringResource(R.string.history_delete), onDelete)
        }
        Text(item.input.lineSequence().first(), Modifier.padding(end = 10.dp), style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Content), color = Ink.Text, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(item.result.englishPrompt.lineSequence().first(), Modifier.padding(end = 10.dp), style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Ltr), color = Ink.Dim, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(findModel(item.modelId)?.name ?: item.modelId, style = MaterialTheme.typography.labelSmall, color = Ink.Faint)
    }
}
