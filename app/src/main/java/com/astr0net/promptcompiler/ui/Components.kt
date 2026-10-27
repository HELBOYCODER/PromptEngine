package com.astr0net.promptcompiler.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/** Full-screen deep-space backdrop with two soft colored glows. */
@Composable
fun NeonBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Neon.Bg, Neon.Bg2, Neon.Bg)))
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(listOf(Neon.Accent2.copy(alpha = 0.28f), Color.Transparent), center = Offset(size.width * 0.9f, size.height * 0.02f), radius = size.width * 0.9f),
                    radius = size.width * 0.9f, center = Offset(size.width * 0.9f, size.height * 0.02f),
                )
                drawCircle(
                    Brush.radialGradient(listOf(Neon.Accent.copy(alpha = 0.14f), Color.Transparent), center = Offset(size.width * 0.05f, size.height * 0.95f), radius = size.width * 0.8f),
                    radius = size.width * 0.8f, center = Offset(size.width * 0.05f, size.height * 0.95f),
                )
            },
    ) { content() }
}

val CardShape = RoundedCornerShape(22.dp)

/** Glassy bordered card. */
@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Neon.Surface)
            .border(1.dp, Brush.linearGradient(listOf(Neon.Border, Neon.Accent2.copy(alpha = 0.22f), Neon.Border)), CardShape)
            .padding(16.dp),
    ) { content() }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = MaterialTheme.typography.labelMedium, color = Neon.Accent)
}

/** Gradient call-to-action button. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    brush: Brush = Neon.Primary,
    leading: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .height(50.dp)
            .alpha(if (enabled) 1f else 0.55f)
            .clip(shape)
            .background(brush)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color(0xFF021014))
    }
}

/** Quiet outlined action. */
@Composable
fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, accent: Color = Neon.Text) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.04f))
            .border(BorderStroke(1.dp, Neon.Border), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = MaterialTheme.typography.labelMedium, color = accent) }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, color: Color = Neon.Accent, style: TextStyle = MaterialTheme.typography.labelSmall) {
    Text(
        text,
        modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = 11.dp, vertical = 4.dp),
        style = style,
        color = color,
        maxLines = 1,
    )
}

@Composable
fun LiveDot(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "live")
    val a by t.animateFloat(0.35f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "alpha")
    Box(modifier.size(8.dp).alpha(a).clip(CircleShape).background(Neon.Success))
}

/** Indeterminate neon bar shown while Gemini is working. */
@Composable
fun CompilingBar(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "compiling")
    val x by t.animateFloat(-0.4f, 1f, infiniteRepeatable(tween(1100)), label = "x")
    Canvas(modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f))) {
        val w = size.width * 0.4f
        val start = size.width * x
        drawRect(Neon.Primary, topLeft = Offset(start, 0f), size = androidx.compose.ui.geometry.Size(w, size.height))
    }
}

/** Thin horizontal usage meter; turns amber/red like the reference UI. */
@Composable
fun MeterBar(fraction: Float, modifier: Modifier = Modifier) {
    val f = fraction.coerceIn(0f, 1f)
    val fill: Brush = when {
        f >= 0.9f -> Brush.horizontalGradient(listOf(Neon.Danger, Neon.Accent3))
        f >= 0.7f -> Brush.horizontalGradient(listOf(Neon.Warn, Neon.Danger))
        else -> Neon.Primary
    }
    Box(modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.07f))) {
        if (f > 0f) Box(Modifier.fillMaxWidth(f).height(7.dp).clip(CircleShape).background(fill))
    }
}

fun Modifier.noRipple(onClick: () -> Unit): Modifier = clickable(interactionSource = MutableInteractionSource(), indication = null, onClick = onClick)


/** Subtle rounded tile used for stat cells and rows. */
fun Modifier.cardBackground(): Modifier = this
    .clip(RoundedCornerShape(16.dp))
    .background(Color.White.copy(alpha = 0.04f))
    .border(1.dp, Neon.Border, RoundedCornerShape(16.dp))
