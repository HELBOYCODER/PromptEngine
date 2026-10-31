package com.astr0net.promptcompiler.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.astr0net.promptcompiler.R
import com.astr0net.promptcompiler.data.MODELS
import com.astr0net.promptcompiler.data.ModelInfo

/** Usable models first, then the (disabled) rest — same grouping as the web <select>. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelPickerSheet(selectedId: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Ink.S1,
        contentColor = Ink.Text,
    ) {
        val (usable, other) = MODELS.partition { it.generate }
        LazyColumn(Modifier.navigationBarsPadding().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text(stringResource(R.string.model_pick_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp)) }
            item { Eyebrow(stringResource(R.string.model_group_usable), Modifier.padding(top = 4.dp), Ink.Cyan) }
            items(usable, key = { it.id }) { m -> ModelRow(m, selected = m.id == selectedId, onClick = { onSelect(m.id) }) }
            item { Eyebrow(stringResource(R.string.model_group_other), Modifier.padding(top = 14.dp)) }
            items(other, key = { it.id }) { m -> ModelRow(m, selected = false, onClick = null) }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ModelRow(m: ModelInfo, selected: Boolean, onClick: (() -> Unit)?) {
    Row(
        Modifier.fillMaxWidth().alpha(if (onClick == null) 0.45f else 1f).clip(RMd)
            .background(if (selected) Ink.Cyan.copy(alpha = 0.10f) else Ink.S2)
            .border(1.dp, if (selected) Ink.Cyan.copy(alpha = 0.5f) else Ink.Line, RMd)
            .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(m.name, style = MaterialTheme.typography.bodyMedium, color = Ink.Text, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(m.id, style = MaterialTheme.typography.labelSmall, color = Ink.Dim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (selected) Icon(Icons.Rounded.CheckCircle, null, tint = Ink.Cyan)
        else if (onClick == null) Icon(Icons.Rounded.Block, null, tint = Ink.Faint)
    }
}
