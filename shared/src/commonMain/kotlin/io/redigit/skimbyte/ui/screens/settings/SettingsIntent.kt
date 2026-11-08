package io.redigit.skimbyte.ui.screens.settings

import io.redigit.skimbyte.theme.AccentColor
import io.redigit.skimbyte.theme.ThemeMode

sealed interface SettingsIntent {
    data class ThemeModeSelected(val mode: ThemeMode) : SettingsIntent
    data class AccentColorSelected(val accent: AccentColor) : SettingsIntent
}
