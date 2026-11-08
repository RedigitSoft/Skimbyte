package io.redigit.skimbyte.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect

/**
 * Both M3 color schemes, specified in full so no role inherits Material's violet baseline.
 *
 * The structure is two neutral ramps plus one sharp accent (see Color.kt): dark is a matte-black
 * field with grey containers stepping up from it, light is an off-white field with containers a
 * step darker. `secondary`/`tertiary` are neutral rather than hued — the single accent carries all
 * the colour — and `error` is an independent red. The `surfaceContainer*` ramp is given real steps
 * here (not collapsed onto one neutral) so cards read as raised surfaces rather than flat slabs.
 *
 * The accent is user-selectable ([AccentColor]); these base schemes bake in [AccentColor.Default]
 * and [withAccent] swaps in the chosen hue across the primary family at theme time.
 */
// M3's "fixed" accent roles hold the same value in both schemes by definition, so they are declared
// once. Nothing in the app references them yet, but leaving them unset lets a baseline value hide.
private val PrimaryFixed = AccentColor.Default.accent
private val PrimaryFixedDim = AccentColor.Default.accent
private val OnPrimaryFixed = AccentColor.Default.onAccent
private val OnPrimaryFixedVariant = AccentColor.Default.onAccent
private val SecondaryFixed = Paper30
private val SecondaryFixedDim = Paper40
private val OnSecondaryFixed = Ink
private val OnSecondaryFixedVariant = InkMuted
private val TertiaryFixed = Paper30
private val TertiaryFixedDim = Paper40
private val OnTertiaryFixed = Ink
private val OnTertiaryFixedVariant = InkMuted

internal val SkimbyteDarkColors = darkColorScheme(
    primary = AccentColor.Default.accent,
    onPrimary = AccentColor.Default.onAccent,
    primaryContainer = AccentColor.Default.accent,
    onPrimaryContainer = AccentColor.Default.onAccent,
    inversePrimary = AccentColor.Default.accent,
    // secondary/tertiary are neutral — the accent is the only hue.
    secondary = MistMuted,
    onSecondary = Black,
    secondaryContainer = Graphite20,
    onSecondaryContainer = Mist,
    tertiary = MistMuted,
    onTertiary = Black,
    tertiaryContainer = Graphite20,
    onTertiaryContainer = Mist,
    background = Black,
    onBackground = Mist,
    surface = Black,
    onSurface = Mist,
    surfaceDim = Black,
    surfaceBright = Graphite30,
    surfaceContainerLowest = Black,
    surfaceContainerLow = Graphite05,
    surfaceContainer = Graphite10,
    surfaceContainerHigh = Graphite20,
    surfaceContainerHighest = Graphite30,
    surfaceVariant = GraphiteVariant,
    onSurfaceVariant = MistMuted,
    surfaceTint = AccentColor.Default.accent,
    inverseSurface = Mist,
    inverseOnSurface = Black,
    outline = GraphiteLine,
    outlineVariant = Graphite20,
    error = DangerDark,
    onError = Black,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    scrim = Black,
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

internal val SkimbyteLightColors = lightColorScheme(
    primary = AccentColor.Default.accent,
    onPrimary = AccentColor.Default.onAccent,
    primaryContainer = AccentColor.Default.accent,
    onPrimaryContainer = AccentColor.Default.onAccent,
    inversePrimary = AccentColor.Default.accent,
    secondary = InkMuted,
    onSecondary = Offwhite,
    secondaryContainer = Paper30,
    onSecondaryContainer = Ink,
    tertiary = InkMuted,
    onTertiary = Offwhite,
    tertiaryContainer = Paper30,
    onTertiaryContainer = Ink,
    background = Offwhite,
    onBackground = Ink,
    surface = Offwhite,
    onSurface = Ink,
    surfaceDim = Paper30,
    surfaceBright = Cloud,
    surfaceContainerLowest = Cloud,
    surfaceContainerLow = Paper10,
    surfaceContainer = Paper20,
    surfaceContainerHigh = Paper30,
    surfaceContainerHighest = Paper40,
    surfaceVariant = PaperVariant,
    onSurfaceVariant = InkMuted,
    surfaceTint = AccentColor.Default.accent,
    inverseSurface = Ink,
    inverseOnSurface = Offwhite,
    outline = PaperLine,
    outlineVariant = Paper30,
    error = DangerLight,
    onError = Cloud,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    scrim = Black,
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
 * Swap the chosen accent into every primary-family role. The neutrals, surfaces, and error stay
 * fixed; only the accent-derived roles change, so a different [AccentColor] re-tints the app
 * without touching anything else.
 */
private fun ColorScheme.withAccent(accent: AccentColor): ColorScheme = copy(
    primary = accent.accent,
    onPrimary = accent.onAccent,
    primaryContainer = accent.accent,
    onPrimaryContainer = accent.onAccent,
    inversePrimary = accent.accent,
    surfaceTint = accent.accent,
    primaryFixed = accent.accent,
    primaryFixedDim = accent.accent,
    onPrimaryFixed = accent.onAccent,
    onPrimaryFixedVariant = accent.onAccent,
)

/**
 * M3 Expressive theme: [MaterialExpressiveTheme] with [MotionScheme.expressive] gives every
 * Material component spring-based motion physics (per the Compose-first Expressive rollout); both
 * schemes are Skimbyte's own, specified role-by-role in [SkimbyteDarkColors] and
 * [SkimbyteLightColors], re-tinted with the user's [accent].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SkimbyteTheme(
    darkTheme: Boolean = true,
    accent: AccentColor = AccentColor.Default,
    content: @Composable () -> Unit,
) {
    val colorScheme = (if (darkTheme) SkimbyteDarkColors else SkimbyteLightColors).withAccent(accent)
    val windowChrome = LocalWindowChromeListener.current
    LaunchedEffect(windowChrome, darkTheme) {
        windowChrome.onThemeResolved(darkTheme, colorScheme.surface)
    }
    // Consume the listener so nested themes (light/dark preview sections in the designsheet
    // catalog) don't also report and fight over the host window's chrome. Also publish the
    // contrast-tuned accent-text colour for source lines, links, and meta.
    CompositionLocalProvider(
        LocalWindowChromeListener provides NoOpWindowChromeListener,
        LocalAccentText provides accentTextOn(accent.accent, colorScheme.surfaceContainer),
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            shapes = SkimbyteShapes,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}

@Composable
fun SkimbyteTheme(
    themeMode: ThemeMode,
    accent: AccentColor = AccentColor.Default,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    SkimbyteTheme(darkTheme = dark, accent = accent, content = content)
}
