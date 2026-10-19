package io.redigit.skimbyte.settings

import io.redigit.skimbyte.theme.ThemeMode
import io.redigit.skimbyte.theme.ThemeModeStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeThemeModeStore(initial: ThemeMode) : ThemeModeStore {
    private var mode: ThemeMode = initial
    private val _themeModeFlow = MutableStateFlow(initial)
    override val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow

    override fun get(): ThemeMode = mode

    override fun set(mode: ThemeMode) {
        this.mode = mode
        _themeModeFlow.value = mode
    }
}

class SettingsViewModelTest {
    @Test
    fun `reads initial theme mode from store`() {
        val store = FakeThemeModeStore(ThemeMode.Dark)
        val vm = SettingsViewModel(store)
        assertEquals(ThemeMode.Dark, vm.uiState.value.themeMode)
    }

    @Test
    fun `onThemeModeSelected persists and updates state`() {
        val store = FakeThemeModeStore(ThemeMode.System)
        val vm = SettingsViewModel(store)
        vm.onThemeModeSelected(ThemeMode.Light)
        assertEquals(ThemeMode.Light, vm.uiState.value.themeMode)
        assertEquals(ThemeMode.Light, store.get())
    }
}
