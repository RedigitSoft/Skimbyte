package io.redigit.skimbyte.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect

/**
 * Both M3 color schemes, specified in full so no role inherits Material's violet baseline, mapped
 * onto the five-stop palette in Color.kt and nothing else.
 *
 * Five values cannot give every role its own step, so the collapses are deliberate: all
 * `surfaceContainer*` levels share one neutral per scheme ([PaleSilver] on light, [BlackOlive] on
 * dark), `secondary`/`tertiary` are neutral rather than hued, and `error` shares [Flame] with
 * `primary` — the error signal comes from copy and iconography, not a separate hue.
 *
 * Contrast: every on-color pairing here clears WCAG AA for normal text (ink on Flame 4.57:1,
 * muted-on-container 6.28:1, and field/ink pairs 9:1+), with one carried-over caveat — [Flame] as
 * *text* on light surfaces reaches 3.31:1, large-text/UI territory only.
 */
// M3's "fixed" accent roles hold the same value in both schemes by definition, so they are
// declared once. Nothing in the app references them yet, but leaving them unset is what lets a
// baseline value hide (and it would show through the designsheet catalog as soon as a component
// did reach for one).
private val PrimaryFixed = Flame
private val PrimaryFixedDim = Flame
private val OnPrimaryFixed = RaisinBlack
private val OnPrimaryFixedVariant = RaisinBlack
private val SecondaryFixed = PaleSilver
private val SecondaryFixedDim = PaleSilver
private val OnSecondaryFixed = RaisinBlack
private val OnSecondaryFixedVariant = BlackOlive
private val TertiaryFixed = PaleSilver
private val TertiaryFixedDim = PaleSilver
private val OnTertiaryFixed = RaisinBlack
private val OnTertiaryFixedVariant = BlackOlive

internal val SkimbyteDarkColors = darkColorScheme(
    primary = Flame,
    // Ink, not white: FloralWhite on Flame reaches only 3.31:1 at the 12sp TopicPill label;
    // RaisinBlack clears 4.57:1.
    onPrimary = RaisinBlack,
    primaryContainer = Flame,
    onPrimaryContainer = RaisinBlack,
    inversePrimary = Flame,
    secondary = PaleSilver,
    onSecondary = RaisinBlack,
    secondaryContainer = BlackOlive,
    onSecondaryContainer = PaleSilver,
    tertiary = PaleSilver,
    onTertiary = RaisinBlack,
    tertiaryContainer = BlackOlive,
    onTertiaryContainer = PaleSilver,
    background = RaisinBlack,
    onBackground = FloralWhite,
    surface = RaisinBlack,
    onSurface = FloralWhite,
    surfaceDim = RaisinBlack,
    surfaceBright = BlackOlive,
    surfaceContainerLowest = RaisinBlack,
    surfaceContainerLow = BlackOlive,
    surfaceContainer = BlackOlive,
    surfaceContainerHigh = BlackOlive,
    surfaceContainerHighest = BlackOlive,
    surfaceVariant = BlackOlive,
    onSurfaceVariant = PaleSilver,
    surfaceTint = Flame,
    inverseSurface = FloralWhite,
    inverseOnSurface = RaisinBlack,
    outline = PaleSilver,
    outlineVariant = BlackOlive,
    // Flame doubles as the error hue; as text on this floor it clears 4.57:1.
    error = Flame,
    onError = RaisinBlack,
    errorContainer = Flame,
    onErrorContainer = RaisinBlack,
    scrim = RaisinBlack,
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
    primary = Flame,
    // Same 3.31:1 failure as dark - the fill is identical in both schemes.
    onPrimary = RaisinBlack,
    primaryContainer = Flame,
    onPrimaryContainer = RaisinBlack,
    inversePrimary = Flame,
    secondary = BlackOlive,
    onSecondary = FloralWhite,
    secondaryContainer = PaleSilver,
    onSecondaryContainer = RaisinBlack,
    tertiary = BlackOlive,
    onTertiary = FloralWhite,
    tertiaryContainer = PaleSilver,
    onTertiaryContainer = RaisinBlack,
    background = FloralWhite,
    onBackground = RaisinBlack,
    surface = FloralWhite,
    onSurface = RaisinBlack,
    surfaceDim = PaleSilver,
    surfaceBright = FloralWhite,
    surfaceContainerLowest = FloralWhite,
    surfaceContainerLow = PaleSilver,
    surfaceContainer = PaleSilver,
    surfaceContainerHigh = PaleSilver,
    surfaceContainerHighest = PaleSilver,
    surfaceVariant = PaleSilver,
    onSurfaceVariant = BlackOlive,
    surfaceTint = Flame,
    inverseSurface = RaisinBlack,
    inverseOnSurface = FloralWhite,
    outline = BlackOlive,
    outlineVariant = PaleSilver,
    // Flame doubles as the error hue; as a fill its ink clears AA, as text on this floor it is
    // large-text only (3.31:1).
    error = Flame,
    onError = RaisinBlack,
    errorContainer = Flame,
    onErrorContainer = RaisinBlack,
    scrim = RaisinBlack,
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
    val colorScheme = if (darkTheme) SkimbyteDarkColors else SkimbyteLightColors
    val windowChrome = LocalWindowChromeListener.current
    LaunchedEffect(windowChrome, darkTheme) {
        windowChrome.onThemeResolved(darkTheme, colorScheme.surface)
    }
    // Consume the listener so nested themes (light/dark preview sections in the designsheet
    // catalog) don't also report and fight over the host window's chrome.
    CompositionLocalProvider(LocalWindowChromeListener provides NoOpWindowChromeListener) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            shapes = SkimbyteShapes,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
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
