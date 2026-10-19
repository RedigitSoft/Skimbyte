package io.redigit.skimbyte.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
        lightColorScheme(primary = SkimbyteAccent)
    }
    MaterialTheme(colorScheme = colorScheme, shapes = SkimbyteShapes, content = content)
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
