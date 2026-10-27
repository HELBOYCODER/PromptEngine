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

/** Neon "deep space" palette taken from the reference web UI. */
object Neon {
    val Bg = Color(0xFF05060F)
    val Bg2 = Color(0xFF0A0C1C)
    val Surface = Color(0xB80F1226)
    val Surface2 = Color(0xD9080A18)
    val Border = Color(0x2978A0FF)
    val BorderStrong = Color(0x6600F0FF)
    val Text = Color(0xFFEAF0FF)
    val TextDim = Color(0xFF8A93B8)
    val Accent = Color(0xFF00F0FF)
    val Accent2 = Color(0xFF7C5CFF)
    val Accent3 = Color(0xFFFF2FD6)
    val Success = Color(0xFF2DF6A5)
    val Warn = Color(0xFFFFCE5C)
    val Danger = Color(0xFFFF4D6D)

    val Primary = Brush.horizontalGradient(listOf(Accent, Accent2))
    val Hot = Brush.horizontalGradient(listOf(Accent2, Accent3))
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
    primary = Neon.Accent,
    onPrimary = Color(0xFF021014),
    secondary = Neon.Accent2,
    tertiary = Neon.Accent3,
    background = Neon.Bg,
    onBackground = Neon.Text,
    surface = Neon.Bg2,
    onSurface = Neon.Text,
    surfaceVariant = Color(0xFF12152B),
    onSurfaceVariant = Neon.TextDim,
    outline = Neon.Border,
    error = Neon.Danger,
)

@Composable
fun PromptEngineTheme(content: @Composable () -> Unit) {
    // The brand look is always dark neon, regardless of the system theme.
    MaterialTheme(colorScheme = Scheme, typography = AppTypography, content = content)
}
