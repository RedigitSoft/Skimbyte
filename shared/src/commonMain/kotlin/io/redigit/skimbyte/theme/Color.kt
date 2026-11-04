package io.redigit.skimbyte.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette primitives, per the design spec on RED-2 (`report` §2.1-2.2).
 *
 * Three anchors, all of them Skimbyte's own work because all three already ship in
 * `docs/branding/`: the brand red `#762830` from the `S` glyph, the cream `#FAF9F5` light field,
 * and the `#181715` dark field. Every other value is generated from an anchor by a stated rule
 * rather than sampled, so the ramps stay re-derivable:
 *
 * - **Clay** - lightness ladder from the brand red, hue drifting 354 -> 6 as it lightens
 *   (lightening a 354 oxblood without the drift goes pink), saturation eased at the mid-steps.
 * - **Graphite** - hold the dark anchor's hue 40 / saturation 7%, step lightness up.
 * - **Cream** - hold the cream anchor's hue 48, step lightness down, taper saturation 33% -> 20%
 *   (holding saturation turns the darker steps khaki).
 */

// Brand ramp - anchored on the docs/branding app-icon "S" glyph.
val Clay10 = Color(0xFF762830) // brand mark; light primary target
val Clay20 = Color(0xFF8E333B)
val Clay30 = Color(0xFFA94247)
val Clay40 = Color(0xFFC15354)
val Clay50 = Color(0xFFD46B68) // dark primary target
val Clay60 = Color(0xFFE38D87)
val Clay70 = Color(0xFFEFB5AF)
val Clay80 = Color(0xFFF7D7D4)

// Warm dark neutrals - hue 40, from the icon's dark field.
val Graphite0 = Color(0xFF0E0D0C)
val Graphite5 = Color(0xFF131211)
val Graphite10 = Color(0xFF181715) // icon dark field; also the ink on cream
val Graphite15 = Color(0xFF1E1D1B)
val Graphite20 = Color(0xFF23221F)
val Graphite30 = Color(0xFF2A2825)
val Graphite40 = Color(0xFF32302C)
val Graphite50 = Color(0xFF3C3A34)
val Graphite60 = Color(0xFF948F84)

// Warm light neutrals - hue 48, from the icon's light field.
val Cream0 = Color(0xFFFFFFFF)
val Cream5 = Color(0xFFFAF9F5) // icon light field
val Cream10 = Color(0xFFF8F6F1)
val Cream20 = Color(0xFFF3F1EA)
val Cream30 = Color(0xFFEDEBE2)
val Cream40 = Color(0xFFE6E3D8)
val Cream50 = Color(0xFFDEDBCE)
val Cream60 = Color(0xFF807960)

val InkMid = Color(0xFF67645B) // light onSurfaceVariant
// Two steps lighter than the spec's #A6A196. That value clears AA on every container level the
// spec checked, but reaches only 4.42:1 on `surfaceBright` - a role the spec's role table defines
// and its contrast pass did not cover. Same ramp rule (hue 40, saturation 8%), 4.64:1 there.
val BoneMid = Color(0xFFAAA59B) // dark onSurfaceVariant

// Semantic counterpoints - teal cool, ochre warm, crimson held apart from Clay by chroma.
val TealDark = Color(0xFF75BCC7)
val TealLight = Color(0xFF226977)
val OchreDark = Color(0xFFDCAF6A)
val OchreLight = Color(0xFF8F5819)
val CrimsonDark = Color(0xFFE8738A)
val CrimsonLight = Color(0xFFA31431)

/**
 * The three accent stops [RED-12] owns, carried over from the shipped theme unchanged.
 *
 * These are deliberately *not* re-derived here. RED-12 tracks the two shipped WCAG AA failures
 * that both live on this family - `onPrimary` on `primary` (3.28:1) and dark `error` on
 * `surfaceVariant` (3.44:1) - and the choice of accent hue is still pending a board decision.
 * Completing the role set does not require touching them, so this change leaves them exactly as
 * they shipped and fixes only the roles that were never set at all.
 *
 * When RED-12 lands, `primary` becomes `Clay50`/`Clay10` and `error` becomes
 * `CrimsonDark`/`CrimsonLight`, and dark `onError` must move back to [Graphite10] in the same
 * commit - white is only correct against the darker carried-over red (see Theme.kt).
 */
val SkimbyteAccent = Color(0xFFCC785C)
val SkimbyteOnPrimary = Color(0xFFFFFFFF)
val SkimbyteError = Color(0xFFC64545)
