package io.redigit.skimbyte.settings

import androidx.lifecycle.ViewModel
import io.redigit.skimbyte.theme.ThemeMode
import io.redigit.skimbyte.theme.ThemeModeStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(private val themeModeStore: ThemeModeStore) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(themeMode = themeModeStore.get()))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onThemeModeSelected(mode: ThemeMode) {
        themeModeStore.set(mode)
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }
}
