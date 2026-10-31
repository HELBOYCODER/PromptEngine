package com.astr0net.promptcompiler.ui

import androidx.annotation.StringRes
import com.astr0net.promptcompiler.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.AutoAwesome
import com.astr0net.promptcompiler.data.ModelInfo
import java.text.NumberFormat
import java.util.Locale

/** Compact token count: 1.5K / 2.3M (digits localized by the caller's locale). */
fun formatTokens(n: Int?, locale: Locale = Locale.getDefault()): String {
    if (n == null) return "—"
    val nf = NumberFormat.getInstance(locale).apply { maximumFractionDigits = 2; minimumFractionDigits = 0 }
    return when {
        n >= 1_000_000 -> nf.format(n / 1_000_000.0).trimEnd() + "M"
        n >= 1_000 -> NumberFormat.getInstance(locale).apply { maximumFractionDigits = 1; minimumFractionDigits = 0 }.format(n / 1_000.0) + "K"
        else -> NumberFormat.getInstance(locale).format(n)
    }
}

fun fmtInt(n: Int?, locale: Locale = Locale.getDefault()): String = NumberFormat.getInstance(locale).format(n ?: 0)

@StringRes
fun resultCategoryRes(category: String): Int = when (category) {
    "image_generation" -> R.string.rc_image_generation
    "research" -> R.string.rc_research
    "coding" -> R.string.rc_coding
    "debugging" -> R.string.rc_debugging
    "writing" -> R.string.rc_writing
    "translation" -> R.string.rc_translation
    "data_analysis" -> R.string.rc_data_analysis
    "planning" -> R.string.rc_planning
    "automation" -> R.string.rc_automation
    else -> R.string.rc_general
}

@StringRes
fun modelCategoryRes(category: ModelInfo): Int = when (category.category) {
    "Text-out models" -> R.string.cat_text_out
    "Multi-modal generative models" -> R.string.cat_multimodal
    "Live API" -> R.string.cat_live
    "Agents" -> R.string.cat_agents
    else -> R.string.cat_other
}

data class CategoryStyle(val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: androidx.compose.ui.graphics.Color)

/** Icon + accent color per detected intent category (no emoji: they render differently on every device). */
fun categoryStyle(category: String): CategoryStyle = when (category) {
    "coding" -> CategoryStyle(Icons.Rounded.Code, Ink.Cyan)
    "debugging" -> CategoryStyle(Icons.Rounded.BugReport, Ink.Coral)
    "research" -> CategoryStyle(Icons.Rounded.MenuBook, Ink.Violet)
    "writing" -> CategoryStyle(Icons.Rounded.EditNote, Ink.Amber)
    "image_generation" -> CategoryStyle(Icons.Rounded.Image, Ink.Magenta)
    "translation" -> CategoryStyle(Icons.Rounded.Translate, Ink.Mint)
    "data_analysis" -> CategoryStyle(Icons.Rounded.Analytics, Ink.Violet)
    "planning" -> CategoryStyle(Icons.Rounded.Map, Ink.Amber)
    "automation" -> CategoryStyle(Icons.Rounded.SmartToy, Ink.Mint)
    else -> CategoryStyle(Icons.Rounded.AutoAwesome, Ink.Dim)
}

/** "just now" / "5 min ago" / "3 h ago" / "2 d ago" as a string-resource key + number. */
fun relativeTime(thenMs: Long, nowMs: Long): Pair<Int, Int?> {
    val min = ((nowMs - thenMs) / 60_000).toInt().coerceAtLeast(0)
    return when {
        min < 1 -> R.string.time_now to null
        min < 60 -> R.string.time_min to min
        min < 24 * 60 -> R.string.time_hour to min / 60
        else -> R.string.time_day to min / (24 * 60)
    }
}
