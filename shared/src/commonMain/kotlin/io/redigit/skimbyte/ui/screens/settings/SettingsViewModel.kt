package io.redigit.skimbyte.ui.screens.settings

import androidx.lifecycle.ViewModel
import io.redigit.skimbyte.theme.AccentColor
import io.redigit.skimbyte.theme.AccentColorStore
import io.redigit.skimbyte.theme.ThemeMode
import io.redigit.skimbyte.theme.ThemeModeStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val themeModeStore: ThemeModeStore,
    private val accentColorStore: AccentColorStore,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(themeMode = themeModeStore.get(), accentColor = accentColorStore.get()),
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeModeSelected -> onThemeModeSelected(intent.mode)
            is SettingsIntent.AccentColorSelected -> onAccentColorSelected(intent.accent)
        }
    }

    private fun onThemeModeSelected(mode: ThemeMode) {
        themeModeStore.set(mode)
        _state.value = _state.value.copy(themeMode = mode)
    }

    private fun onAccentColorSelected(accent: AccentColor) {
        accentColorStore.set(accent)
        _state.value = _state.value.copy(accentColor = accent)
    }
}
