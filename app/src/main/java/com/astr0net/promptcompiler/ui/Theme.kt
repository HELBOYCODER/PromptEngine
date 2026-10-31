package com.astr0net.promptcompiler.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.astr0net.promptcompiler.R

/** Design tokens: tonal dark layers, hairlines, one electric-cyan accent (gradient only on CTA / brand). */
object Ink {
    val Bg = Color(0xFF07080D)
    val S1 = Color(0xFF0E1018)
    val S2 = Color(0xFF141722)
    val S3 = Color(0xFF1B1F2E)
    val Line = Color(0xFF252A3A)
    val Text = Color(0xFFF1F3FA)
    val Dim = Color(0xFF8E96AE)
    val Faint = Color(0xFF5B6279)
    val Cyan = Color(0xFF3DEBFF)
    val Violet = Color(0xFF8F7CFF)
    val Magenta = Color(0xFFFF4FD8)
    val Mint = Color(0xFF3DF2A8)
    val Amber = Color(0xFFFFC857)
    val Coral = Color(0xFFFF5D73)
    val OnCyan = Color(0xFF04141A)

    val Cta = Brush.horizontalGradient(listOf(Cyan, Violet))
}

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private fun base(size: Int, weight: FontWeight = FontWeight.Normal, line: Int = (size * 1.55).toInt()) =
    TextStyle(fontFamily = Vazirmatn, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp)

private val AppTypography = Typography(
    displaySmall = base(28, FontWeight.Bold),
    headlineSmall = base(22, FontWeight.Bold),
    titleLarge = base(20, FontWeight.SemiBold),
    titleMedium = base(16, FontWeight.SemiBold),
    titleSmall = base(14, FontWeight.SemiBold),
    bodyLarge = base(16),
    bodyMedium = base(14),
    bodySmall = base(12),
    labelLarge = base(14, FontWeight.SemiBold),
    labelMedium = base(12, FontWeight.Medium),
    labelSmall = base(11, FontWeight.Medium),
)

private val Scheme = darkColorScheme(
    primary = Ink.Cyan,
    onPrimary = Color(0xFF021014),
    secondary = Ink.Violet,
    tertiary = Ink.Magenta,
    background = Ink.Bg,
    onBackground = Ink.Text,
    surface = Ink.S1,
    onSurface = Ink.Text,
    surfaceVariant = Ink.S2,
    onSurfaceVariant = Ink.Dim,
    outline = Ink.Line,
    error = Ink.Coral,
)

@Composable
fun PromptEngineTheme(content: @Composable () -> Unit) {
    // The brand look is always dark neon, regardless of the system theme.
    MaterialTheme(colorScheme = Scheme, typography = AppTypography, content = content)
}
