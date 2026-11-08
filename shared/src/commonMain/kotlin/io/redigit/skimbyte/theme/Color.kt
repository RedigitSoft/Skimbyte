package io.redigit.skimbyte.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette primitives: two neutral ramps and one sharp accent.
 *
 * - **Dark** is a matte-black field ([Black]) with neutral grey containers stepping *up* from it
 *   ([Graphite05]–[Graphite30]).
 * - **Light** is an off-white field ([Offwhite]) with neutral containers a step *darker* than it
 *   ([Paper10]–[Paper40]).
 * - Both schemes share one sharp accent, chosen by the user — see [AccentColor]; `error` is a red
 *   independent of the accent.
 *
 * Nothing outside Theme.kt references these directly — UI reads `MaterialTheme.colorScheme`.
 */

// Dark neutrals — matte-black field, containers stepping up.
//
// IMPORTANT: no two *background* roles may share a value. M3's `contentColorFor()` matches the
// background colour by value to pick the on-colour, so if e.g. `surfaceContainer` and `errorContainer`
// held the same hex, an unstyled `Card` would resolve its content colour to `onErrorContainer` and
// paint every default-coloured text (post titles, section labels) red. Each role below is distinct.
val Black = Color(0xFF0E0E0E) // dark field (surface/background)
val Graphite05 = Color(0xFF151515) // dark surfaceContainerLow / surfaceDim
val Graphite10 = Color(0xFF1C1C1C) // dark card (surfaceContainer)
val GraphiteVariant = Color(0xFF1F1F1F) // dark surfaceVariant (kept apart from the card)
val Graphite20 = Color(0xFF242424) // dark surfaceContainerHigh
val Graphite30 = Color(0xFF2C2C2C) // dark surfaceContainerHighest / surfaceBright
val GraphiteLine = Color(0xFF3A3A3A) // dark outline
val Mist = Color(0xFFF2F1EF) // dark onSurface (near-white)
val MistMuted = Color(0xFFAFADA8) // dark onSurfaceVariant — ~7.5:1 on the card

// Light neutrals — off-white field, containers a step darker.
val Offwhite = Color(0xFFF7F6F3) // light field (surface/background)
val Paper10 = Color(0xFFF1F0EC) // light surfaceContainerLow
val Paper20 = Color(0xFFEBE9E4) // light card (surfaceContainer)
val PaperVariant = Color(0xFFE7E5DF) // light surfaceVariant (kept apart from the card)
val Paper30 = Color(0xFFE4E2DB) // light surfaceContainerHigh
val Paper40 = Color(0xFFDDDBD3) // light surfaceContainerHighest
val PaperLine = Color(0xFFC7C5BD) // light outline
val Ink = Color(0xFF1A1A18) // light onSurface (near-black)
val InkMuted = Color(0xFF5B5A55) // light onSurfaceVariant — ~5.5:1 on the card
val Cloud = Color(0xFFFFFFFF) // light surfaceBright / surfaceContainerLowest

// Error — a red in both schemes, independent of the accent. The container tones are reddish (and
// distinct from every neutral) both because that is what an error surface should look like and so
// `contentColorFor()` never confuses a neutral card for an error container (see the note above).
val DangerDark = Color(0xFFFF6B6B) // dark error (light red on the black floor)
val DangerLight = Color(0xFFC0392C) // light error
val ErrorContainerDark = Color(0xFF3A1E1E) // dark errorContainer (deep red tint)
val OnErrorContainerDark = Color(0xFFFFB4AB) // on dark errorContainer
val ErrorContainerLight = Color(0xFFF6D7D2) // light errorContainer (soft red tint)
val OnErrorContainerLight = Color(0xFF7A271F) // on light errorContainer
