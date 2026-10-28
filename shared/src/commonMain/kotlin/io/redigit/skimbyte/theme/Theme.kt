package io.redigit.skimbyte.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable

/**
 * M3 Expressive theme: [MaterialExpressiveTheme] with [MotionScheme.expressive] gives every
 * Material component spring-based motion physics (per the Compose-first Expressive rollout);
 * the light scheme starts from [expressiveLightColorScheme]'s tuned role mappings, and both
 * schemes keep Skimbyte's brand colors.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SkimbyteTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = SkimbyteAccent,
            onPrimary = SkimbyteOnPrimary,
            background = SkimbyteBackground,
            onBackground = SkimbyteOnBackground,
            surface = SkimbyteSurface,
            onSurface = SkimbyteOnBackground,
            surfaceVariant = SkimbyteSurfaceVariant,
            onSurfaceVariant = SkimbyteOnSurfaceVariant,
            error = SkimbyteError,
        )
    } else {
        expressiveLightColorScheme().copy(primary = SkimbyteAccent, onPrimary = SkimbyteOnPrimary)
    }
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
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
