package io.redigit.skimbyte.theme

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AccentColorStore {
    val accentColorFlow: StateFlow<AccentColor>
    fun get(): AccentColor
    fun set(accent: AccentColor)
}

/**
 * Backed by multiplatform-settings, mirroring [SettingsThemeModeStore] — the same cross-platform
 * persistence the app's wasmJs target requires.
 *
 * On a fresh install (nothing stored) a *random* accent is chosen and persisted, so every install
 * opens with a varied accent that then stays put across launches; the user can still change it.
 */
class SettingsAccentColorStore(private val settings: Settings) : AccentColorStore {

    init {
        if (settings.getStringOrNull(KEY_ACCENT_COLOR) == null) {
            settings.putString(KEY_ACCENT_COLOR, AccentColor.entries.random().name)
        }
    }

    private val _accentColorFlow = MutableStateFlow(get())
    override val accentColorFlow: StateFlow<AccentColor> = _accentColorFlow.asStateFlow()

    override fun get(): AccentColor {
        val stored = settings.getStringOrNull(KEY_ACCENT_COLOR)
        return if (stored != null) {
            try {
                AccentColor.valueOf(stored)
            } catch (e: IllegalArgumentException) {
                // Defensive default at the storage boundary: unknown/corrupted values (e.g. a colour
                // removed in a later version) resolve to the default accent.
                AccentColor.Default
            }
        } else {
            AccentColor.Default
        }
    }

    override fun set(accent: AccentColor) {
        settings.putString(KEY_ACCENT_COLOR, accent.name)
        _accentColorFlow.value = accent
    }

    private companion object {
        const val KEY_ACCENT_COLOR = "accent_color"
    }
}
