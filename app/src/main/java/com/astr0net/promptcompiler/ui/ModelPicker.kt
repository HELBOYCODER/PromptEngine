package com.astr0net.promptcompiler.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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

/** Bottom sheet listing usable models first, then the (disabled) rest, like the web <select>. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelPickerSheet(selectedId: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Neon.Bg2,
        contentColor = Neon.Text,
    ) {
        val (usable, other) = MODELS.partition { it.generate }
        LazyColumn(Modifier.navigationBarsPadding().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { Text(stringResource(R.string.model_pick_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp)) }
            item { SectionLabel(stringResource(R.string.model_group_usable), Modifier.padding(top = 6.dp, bottom = 2.dp)) }
            items(usable, key = { it.id }) { m -> ModelRow(m, selected = m.id == selectedId, onClick = { onSelect(m.id) }) }
            item { SectionLabel(stringResource(R.string.model_group_other), Modifier.padding(top = 14.dp, bottom = 2.dp)) }
            items(other, key = { it.id }) { m -> ModelRow(m, selected = false, onClick = null) }
            item { androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 16.dp)) }
        }
    }
}

@Composable
private fun ModelRow(m: ModelInfo, selected: Boolean, onClick: (() -> Unit)?) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().alpha(if (onClick == null) 0.45f else 1f).clip(shape)
            .background(if (selected) Neon.Accent.copy(alpha = 0.10f) else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.03f))
            .border(1.dp, if (selected) Neon.BorderStrong else Neon.Border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(m.name + if (onClick == null) "  ✕" else "", style = MaterialTheme.typography.bodyMedium, color = Neon.Text, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(m.id, style = MaterialTheme.typography.labelSmall, color = Neon.TextDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (selected) Text("✓", color = Neon.Accent, style = MaterialTheme.typography.titleMedium)
    }
}
