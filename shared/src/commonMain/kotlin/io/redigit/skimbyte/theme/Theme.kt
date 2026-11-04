package io.redigit.skimbyte.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Both M3 color schemes, specified in full, per the design spec on RED-2 (`report` §2.3).
 *
 * Previously dark set nine roles and light was `expressiveLightColorScheme().copy(primary,
 * onPrimary)`. Every role neither of them named kept Material's baseline, which is seeded from a
 * violet palette - so the feed card drew its own container at `#36343B` on a warm `#181715` floor,
 * and light mode rendered lavender cards. A `.copy()` on a stock scheme is what produced that, so
 * both schemes now name every role and neither inherits a baseline value.
 *
 * `primary`, `onPrimary` and dark `error` are carried over from the shipped theme unchanged - they
 * are RED-12's subject and the accent hue is pending a board decision. See [SkimbyteAccent].
 */
// M3's "fixed" accent roles hold the same value in both schemes by definition, so they are
// declared once. The spec's role table omits them because nothing in the app references them yet -
// but leaving them unset is what lets a violet value hide, which is the whole failure mode here
// (and it would show through the designsheet catalog as soon as a component did reach for one).
// These are read straight off the ramps the spec already fixed: `*Fixed` takes the light container
// tone, `*FixedDim` the step below it, `on*Fixed` the light on-container, `on*FixedVariant` the
// mid-ramp stop. Flagged for the Designer to confirm rather than quietly treat as spec.
private val PrimaryFixed = Clay80
private val PrimaryFixedDim = Clay70
private val OnPrimaryFixed = Color(0xFF521E22)
private val OnPrimaryFixedVariant = Clay10
private val SecondaryFixed = Color(0xFFCDE8EC)
private val SecondaryFixedDim = Color(0xFFA9DAE2)
private val OnSecondaryFixed = Color(0xFF123C44)
private val OnSecondaryFixedVariant = Color(0xFF1C5763)
private val TertiaryFixed = Color(0xFFF6E2C2)
private val TertiaryFixedDim = Color(0xFFF0D3A4)
private val OnTertiaryFixed = Color(0xFF4A2E09)
private val OnTertiaryFixedVariant = Color(0xFF7B4A13)

private val SkimbyteDarkColors = darkColorScheme(
    primary = SkimbyteAccent,
    onPrimary = SkimbyteOnPrimary,
    primaryContainer = Color(0xFF4F2224),
    onPrimaryContainer = Clay70,
    inversePrimary = Clay10,
    secondary = TealDark,
    onSecondary = Graphite10,
    secondaryContainer = Color(0xFF1C3B40),
    onSecondaryContainer = Color(0xFFA9DAE2),
    tertiary = OchreDark,
    onTertiary = Graphite10,
    tertiaryContainer = Color(0xFF4A3415),
    onTertiaryContainer = Color(0xFFF0D3A4),
    background = Graphite10,
    onBackground = Cream5,
    surface = Graphite10,
    onSurface = Cream5,
    surfaceDim = Graphite5,
    surfaceBright = Graphite50,
    surfaceContainerLowest = Graphite0,
    surfaceContainerLow = Graphite15,
    surfaceContainer = Graphite20,
    surfaceContainerHigh = Graphite30,
    surfaceContainerHighest = Graphite40,
    surfaceVariant = Graphite15,
    onSurfaceVariant = BoneMid,
    surfaceTint = SkimbyteAccent,
    inverseSurface = Cream5,
    inverseOnSurface = Graphite10,
    outline = Graphite60,
    outlineVariant = Graphite50,
    error = SkimbyteError,
    // Cream0, not Graphite10 as the spec's table has it: the spec assumed the lighter oxblood
    // `error` that RED-12 will introduce. Against the carried-over `#C64545` an ink tint reaches
    // only 3.70:1, where white reaches 4.84:1. Both `onError` call sites are icon tints
    // (AvatarWithEditBadge.kt, BannerImage.kt), so 3:1 would pass - but the two are one AA step
    // apart for free. RED-12 must move this back to Graphite10 when it swaps in CrimsonDark.
    onError = Cream0,
    errorContainer = Color(0xFF59222D),
    onErrorContainer = Color(0xFFF0A8B7),
    scrim = Color.Black,
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant,
)

private val SkimbyteLightColors = lightColorScheme(
    primary = SkimbyteAccent,
    onPrimary = SkimbyteOnPrimary,
    primaryContainer = Clay80,
    onPrimaryContainer = Color(0xFF521E22),
    inversePrimary = Clay60,
    secondary = TealLight,
    onSecondary = Cream0,
    secondaryContainer = Color(0xFFCDE8EC),
    onSecondaryContainer = Color(0xFF123C44),
    tertiary = OchreLight,
    onTertiary = Cream0,
    tertiaryContainer = Color(0xFFF6E2C2),
    onTertiaryContainer = Color(0xFF4A2E09),
    background = Cream5,
    onBackground = Graphite10,
    surface = Cream5,
    onSurface = Graphite10,
    surfaceDim = Cream40,
    surfaceBright = Cream0,
    surfaceContainerLowest = Cream0,
    surfaceContainerLow = Cream10,
    surfaceContainer = Cream20,
    surfaceContainerHigh = Cream30,
    surfaceContainerHighest = Cream40,
    surfaceVariant = Cream30,
    onSurfaceVariant = InkMid,
    surfaceTint = SkimbyteAccent,
    inverseSurface = Graphite20,
    inverseOnSurface = Cream5,
    outline = Cream60,
    outlineVariant = Cream50,
    // Light `error` had no shipped value to carry over - it was Material's baseline, which is the
    // very thing this change removes - so it takes the spec's derived stop. Dark `error` is held.
    error = CrimsonLight,
    onError = Cream0,
    errorContainer = Color(0xFFF9DCE2),
    onErrorContainer = Color(0xFF6A1B2A),
    scrim = Color.Black,
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant,
)

/**
 * M3 Expressive theme: [MaterialExpressiveTheme] with [MotionScheme.expressive] gives every
 * Material component spring-based motion physics (per the Compose-first Expressive rollout); both
 * schemes are Skimbyte's own, specified role-by-role in [SkimbyteDarkColors] and
 * [SkimbyteLightColors].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SkimbyteTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        colorScheme = if (darkTheme) SkimbyteDarkColors else SkimbyteLightColors,
        shapes = SkimbyteShapes,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}

@Composable
fun SkimbyteTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    SkimbyteTheme(darkTheme = dark, content = content)
}
