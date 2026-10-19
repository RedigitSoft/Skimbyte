package io.redigit.skimbyte.theme

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ThemeModeStore {
    val themeModeFlow: StateFlow<ThemeMode>
    fun get(): ThemeMode
    fun set(mode: ThemeMode)
}

/**
 * Backed by multiplatform-settings rather than androidx.datastore: DataStore's non-Android/web
 * targets are not yet shipped, while this library has full wasmJs support (Web Storage-backed),
 * which this app's target list requires.
 */
class SettingsThemeModeStore(private val settings: Settings) : ThemeModeStore {

    private val _themeModeFlow = MutableStateFlow(get())
    override val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

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
        _themeModeFlow.value = mode
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}
