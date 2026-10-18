package io.redigit.skimbyte.theme

import com.russhwolf.settings.Settings

interface ThemeModeStore {
    fun get(): ThemeMode
    fun set(mode: ThemeMode)
}

/**
 * Backed by multiplatform-settings rather than androidx.datastore: DataStore's non-Android/web
 * targets are not yet shipped, while this library has full wasmJs support (Web Storage-backed),
 * which this app's target list requires.
 */
class SettingsThemeModeStore(private val settings: Settings) : ThemeModeStore {

    override fun get(): ThemeMode {
        val stored = settings.getStringOrNull(KEY_THEME_MODE)
        return if (stored != null) {
            try {
                ThemeMode.valueOf(stored)
            } catch (e: IllegalArgumentException) {
                // Best-guess defensive default at the storage boundary: unknown/corrupted values
                // resolve to System, the safest cross-platform choice.
                ThemeMode.System
            }
        } else {
            ThemeMode.System
        }
    }

    override fun set(mode: ThemeMode) {
        settings.putString(KEY_THEME_MODE, mode.name)
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}
