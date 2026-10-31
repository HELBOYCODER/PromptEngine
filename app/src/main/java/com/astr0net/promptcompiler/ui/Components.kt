package com.astr0net.promptcompiler.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.RowScope

val RSm = RoundedCornerShape(12.dp)
val RMd = RoundedCornerShape(16.dp)
val RLg = RoundedCornerShape(22.dp)

/** Near-black canvas with a single soft cyan/violet light from the top corner. */
@Composable
fun InkBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier.fillMaxSize().background(Ink.Bg).drawBehind {
            val c = Offset(size.width * 0.95f, -size.height * 0.02f)
            drawCircle(Brush.radialGradient(listOf(Ink.Violet.copy(alpha = 0.22f), Color.Transparent), center = c, radius = size.width * 0.95f), radius = size.width * 0.95f, center = c)
            val c2 = Offset(-size.width * 0.1f, size.height * 0.3f)
            drawCircle(Brush.radialGradient(listOf(Ink.Cyan.copy(alpha = 0.07f), Color.Transparent), center = c2, radius = size.width * 0.7f), radius = size.width * 0.7f, center = c2)
        },
    ) { content() }
}

/** Press feedback: a quick spring scale. */
fun Modifier.pressable(enabled: Boolean = true, role: Role = Role.Button, onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "press")
    Modifier.scale(scale).clickable(interactionSource = source, indication = null, enabled = enabled, role = role, onClick = onClick)
}

@Composable
fun InkCard(modifier: Modifier = Modifier, padding: Dp = 16.dp, content: @Composable () -> Unit) {
    Column(modifier.fillMaxWidth().clip(RLg).background(Ink.S1).border(1.dp, Ink.Line, RLg).padding(padding)) { content() }
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = Ink.Dim) {
    Text(text, modifier, style = MaterialTheme.typography.labelMedium, color = color)
}

@Composable
fun IconAction(icon: ImageVector, description: String, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Ink.Dim, enabled: Boolean = true) {
    Box(
        modifier.size(40.dp).alpha(if (enabled) 1f else 0.35f).clip(CircleShape).pressable(enabled, Role.Button, onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, description, tint = tint, modifier = Modifier.size(20.dp)) }
}

/** Main call to action. While [loading] it morphs into a progress strip showing [loadingText]. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false, loadingText: String = "", icon: ImageVector? = null) {
    Box(
        modifier.fillMaxWidth().height(56.dp).clip(RMd)
            .background(if (loading) Brush.horizontalGradient(listOf(Ink.S3, Ink.S3)) else Ink.Cta)
            .alpha(if (enabled || loading) 1f else 0.5f)
            .pressable(enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Ink.Cyan, strokeWidth = 2.5.dp)
                Text(loadingText, style = MaterialTheme.typography.labelLarge, color = Ink.Text)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) Icon(icon, null, tint = Ink.OnCyan, modifier = Modifier.size(20.dp))
                Text(text, style = MaterialTheme.typography.titleSmall, color = Ink.OnCyan)
            }
        }
    }
}

@Composable
fun TonalButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, color: Color = Ink.Text, enabled: Boolean = true) {
    Row(
        modifier.alpha(if (enabled) 1f else 0.4f).height(44.dp).clip(RSm).background(Ink.S2).border(1.dp, Ink.Line, RSm)
            .pressable(enabled, onClick = onClick).padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}

@Composable
fun InkChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean = false, icon: ImageVector? = null, trailing: ImageVector? = null) {
    val bg by animateColorAsState(if (selected) Ink.Cyan.copy(alpha = 0.14f) else Ink.S2, label = "chipBg")
    val fg = if (selected) Ink.Cyan else Ink.Text
    Row(
        modifier.height(38.dp).clip(CircleShape).background(bg).border(1.dp, if (selected) Ink.Cyan.copy(alpha = 0.5f) else Ink.Line, CircleShape)
            .pressable(onClick = onClick).padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = fg, maxLines = 1)
        if (trailing != null) Icon(trailing, null, tint = Ink.Dim, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun Tag(text: String, color: Color, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier.clip(CircleShape).background(color.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
    }
}

@Composable
fun StatusDot(color: Color, pulse: Boolean = false, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "dot")
    val a by t.animateFloat(0.35f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "a")
    Box(modifier.size(8.dp).alpha(if (pulse) a else 1f).clip(CircleShape).background(color))
}

/** Two-or-more option switch with a sliding selected pill. */
@Composable
fun SegmentedControl(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().clip(RMd).background(Ink.S2).border(1.dp, Ink.Line, RMd).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            val bg by animateColorAsState(if (on) Ink.S3 else Color.Transparent, label = "segBg")
            Box(
                Modifier.weight(1f).height(38.dp).clip(RSm).background(bg).then(if (on) Modifier.border(1.dp, Ink.Line, RSm) else Modifier)
                    .pressable(role = Role.Tab) { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) { Text(label, style = MaterialTheme.typography.labelLarge, color = if (on) Ink.Cyan else Ink.Dim) }
        }
    }
}

/** Collapsible section with an item-count badge. */
@Composable
fun Accordion(title: String, count: Int, accent: Color, initiallyOpen: Boolean = false, content: @Composable () -> Unit) {
    var open by rememberSaveable(title) { mutableStateOf(initiallyOpen) }
    val rot by animateFloatAsState(if (open) 180f else 0f, label = "chev")
    Column(Modifier.fillMaxWidth().clip(RMd).background(Ink.S2).border(1.dp, Ink.Line, RMd).animateContentSize()) {
        Row(Modifier.fillMaxWidth().pressable(role = Role.Button) { open = !open }.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(10.dp))
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = Ink.Text)
            Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = if (count > 0) accent else Ink.Faint)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Rounded.ExpandMore, null, tint = Ink.Dim, modifier = Modifier.size(20.dp).rotate(rot))
        }
        if (open) Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RMd).background(Ink.S1).border(1.dp, Ink.Line, RMd).padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Ink.Text, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Ink.Dim, maxLines = 1)
    }
}

@Composable
fun MeterBar(fraction: Float, modifier: Modifier = Modifier) {
    val f by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(600), label = "meter")
    val fill = when {
        fraction >= 0.9f -> Brush.horizontalGradient(listOf(Ink.Coral, Ink.Magenta))
        fraction >= 0.7f -> Brush.horizontalGradient(listOf(Ink.Amber, Ink.Coral))
        else -> Ink.Cta
    }
    Box(modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Ink.S3)) {
        if (f > 0f) Box(Modifier.fillMaxWidth(f).height(6.dp).clip(CircleShape).background(fill))
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(Ink.S2).border(1.dp, Ink.Line, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Ink.Cyan, modifier = Modifier.size(30.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, color = Ink.Text)
        Text(body, style = MaterialTheme.typography.bodySmall, color = Ink.Dim, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

data class NavItem(val label: String, val icon: ImageVector, val iconOn: ImageVector)

@Composable
fun BottomNav(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Ink.S1).drawBehind { drawRect(Ink.Line, size = androidx.compose.ui.geometry.Size(size.width, 1f)) }.navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
            items.forEachIndexed { i, item ->
                val on = i == selected
                val pill by animateColorAsState(if (on) Ink.Cyan.copy(alpha = 0.16f) else Color.Transparent, label = "pill")
                Column(
                    Modifier.weight(1f).pressable(role = Role.Tab) { onSelect(i) }.padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(Modifier.width(60.dp).height(30.dp).clip(CircleShape).background(pill), contentAlignment = Alignment.Center) {
                        Icon(if (on) item.iconOn else item.icon, item.label, tint = if (on) Ink.Cyan else Ink.Dim, modifier = Modifier.size(22.dp))
                    }
                    Text(item.label, style = MaterialTheme.typography.labelSmall, color = if (on) Ink.Cyan else Ink.Dim, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun ToastBubble(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.fillMaxWidth().clip(RMd).background(Ink.S3).border(1.dp, Ink.Line, RMd).padding(horizontal = 16.dp, vertical = 14.dp),
        style = MaterialTheme.typography.bodyMedium, color = Ink.Text,
    )
}

@Composable fun ScreenTitle(text: String, modifier: Modifier = Modifier, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, color = Ink.Text)
        trailing()
    }
}
