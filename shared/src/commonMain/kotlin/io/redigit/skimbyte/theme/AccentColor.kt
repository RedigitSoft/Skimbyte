package io.redigit.skimbyte.theme

import androidx.compose.ui.graphics.Color

/**
 * The user-selectable accent hue. One sharp colour carries `primary` (and the rest of the primary
 * family) in both the light and dark schemes, paired with the content colour drawn on top of it —
 * [onAccent] is ink for light accents, white for saturated ones, picked to clear WCAG AA on the
 * fill. The neutral ramps in Color.kt are fixed; only this hue changes.
 *
 * Persisted by [AccentColorStore]; [Default] is used until the user picks one.
 */
enum class AccentColor(val accent: Color, val onAccent: Color) {
    Flame(Color(0xFFEB5E28), Color(0xFF1A1208)),
    Amber(Color(0xFFF5A524), Color(0xFF1A1200)),
    Crimson(Color(0xFFF2384A), Color(0xFFFFFFFF)),
    Magenta(Color(0xFFEC4899), Color(0xFF1A0410)),
    Violet(Color(0xFF7C5CFC), Color(0xFFFFFFFF)),
    Blue(Color(0xFF3B6EF6), Color(0xFFFFFFFF)),
    Cyan(Color(0xFF12B5E5), Color(0xFF04141A)),
    Emerald(Color(0xFF15B881), Color(0xFF05130D)),
    Lime(Color(0xFF9BCF35), Color(0xFF131A04)),
    Oxblood(Color(0xFF762830), Color(0xFFFFFFFF)),
    ;

    companion object {
        val Default = Flame
    }
}
