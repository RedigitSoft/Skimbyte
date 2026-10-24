package io.redigit.skimbyte.ui.screens.settings

import io.redigit.skimbyte.theme.ThemeMode

sealed interface SettingsIntent {
    data class ThemeModeSelected(val mode: ThemeMode) : SettingsIntent
}
