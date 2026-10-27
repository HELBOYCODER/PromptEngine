package com.astr0net.promptcompiler.ui

import androidx.annotation.StringRes
import com.astr0net.promptcompiler.R
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
