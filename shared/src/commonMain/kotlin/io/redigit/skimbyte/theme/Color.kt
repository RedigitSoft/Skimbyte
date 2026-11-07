package io.redigit.skimbyte.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette primitives: the five-stop Coolors palette
 * (https://coolors.co/fffcf2-ccc5b9-403d39-252422-eb5e28), adopted as the entire color vocabulary
 * by explicit decision — no derived ramps, no extra stops. Both schemes in [SkimbyteDarkColors]
 * and [SkimbyteLightColors] map every M3 role onto these five values, which has two deliberate
 * consequences:
 *
 * - Roles that would normally sit on different ramp steps share a value (all `surfaceContainer*`
 *   levels collapse onto one neutral per scheme; `error` shares [Flame] with `primary`).
 * - Contrast: [RaisinBlack] on [Flame] clears AA at 4.57:1, which is why it is the on-color for
 *   every Flame fill in both schemes. [Flame] used as *text* clears AA on [RaisinBlack] (4.57:1)
 *   but reaches only 3.31:1 on [FloralWhite] — large-text/UI-component territory, same caveat the
 *   previous accent carried on light surfaces.
 */
val FloralWhite = Color(0xFFFFFCF2) // light field; dark scheme's on-colors
val PaleSilver = Color(0xFFCCC5B9) // light containers / dark scheme's muted content
val BlackOlive = Color(0xFF403D39) // dark containers / light scheme's muted content
val RaisinBlack = Color(0xFF252422) // dark field; ink on cream and on Flame
val Flame = Color(0xFFEB5E28) // the single accent: primary, surfaceTint, and error
