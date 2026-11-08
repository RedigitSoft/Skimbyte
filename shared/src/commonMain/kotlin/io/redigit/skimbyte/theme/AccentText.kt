package io.redigit.skimbyte.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.pow

/**
 * The accent, tuned to stay legible as *text* on the card surface.
 *
 * The vivid [AccentColor.accent] is sized for fills (chips, the bookmark button, nav indicators)
 * where a paired on-color sits on top. As small text on a card it can fall under WCAG AA for some
 * hue/scheme combinations — a lime on the off-white card, the oxblood on the matte-black one.
 * [accentTextOn] nudges the accent toward the surface's opposite end only as far as needed to clear
 * AA, so source lines, the read-source link, and meta keep the accent's identity without becoming
 * unreadable. Provided by [SkimbyteTheme]; read via this local.
 */
val LocalAccentText = staticCompositionLocalOf { Color.Unspecified }

private const val MIN_CONTRAST = 4.5

private fun Color.relativeLuminance(): Double {
    fun channel(c: Float): Double {
        val v = c.toDouble()
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(red) + 0.7152 * channel(green) + 0.0722 * channel(blue)
}

private fun contrastRatio(a: Color, b: Color): Double {
    val la = a.relativeLuminance()
    val lb = b.relativeLuminance()
    return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
}

/**
 * [accent] lightened (on a dark [surface]) or darkened (on a light one) just enough to clear WCAG AA
 * against [surface]. Returned unchanged when it already passes; blends toward white/black in small
 * steps otherwise, which preserves the hue for the small shifts most accents need.
 */
fun accentTextOn(accent: Color, surface: Color): Color {
    if (contrastRatio(accent, surface) >= MIN_CONTRAST) return accent
    val target = if (surface.relativeLuminance() < 0.5) Color.White else Color.Black
    var fraction = 0.1f
    while (fraction < 1f) {
        val candidate = lerp(accent, target, fraction)
        if (contrastRatio(candidate, surface) >= MIN_CONTRAST) return candidate
        fraction += 0.1f
    }
    return target
}
