package io.redigit.skimbyte.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// The shipped app is always dark per the product spec; darkTheme exists so individual components
// can still have a light-mode @Preview without contradicting that.
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
