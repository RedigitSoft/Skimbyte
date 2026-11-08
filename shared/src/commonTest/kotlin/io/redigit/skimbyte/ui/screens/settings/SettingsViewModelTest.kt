package io.redigit.skimbyte.ui.screens.settings

import io.redigit.skimbyte.theme.AccentColor
import io.redigit.skimbyte.theme.AccentColorStore
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

private class FakeAccentColorStore(initial: AccentColor) : AccentColorStore {
    private var accent: AccentColor = initial
    private val _accentColorFlow = MutableStateFlow(initial)
    override val accentColorFlow: StateFlow<AccentColor> = _accentColorFlow

    override fun get(): AccentColor = accent

    override fun set(accent: AccentColor) {
        this.accent = accent
        _accentColorFlow.value = accent
    }
}

class SettingsViewModelTest {
    @Test
    fun `reads initial theme mode from store`() {
        val vm = SettingsViewModel(FakeThemeModeStore(ThemeMode.Dark), FakeAccentColorStore(AccentColor.Default))
        assertEquals(ThemeMode.Dark, vm.state.value.themeMode)
    }

    @Test
    fun `onThemeModeSelected persists and updates state`() {
        val store = FakeThemeModeStore(ThemeMode.System)
        val vm = SettingsViewModel(store, FakeAccentColorStore(AccentColor.Default))
        vm.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.Light))
        assertEquals(ThemeMode.Light, vm.state.value.themeMode)
        assertEquals(ThemeMode.Light, store.get())
    }

    @Test
    fun `reads initial accent from store`() {
        val vm = SettingsViewModel(FakeThemeModeStore(ThemeMode.System), FakeAccentColorStore(AccentColor.Violet))
        assertEquals(AccentColor.Violet, vm.state.value.accentColor)
    }

    @Test
    fun `onAccentColorSelected persists and updates state`() {
        val accentStore = FakeAccentColorStore(AccentColor.Flame)
        val vm = SettingsViewModel(FakeThemeModeStore(ThemeMode.System), accentStore)
        vm.onIntent(SettingsIntent.AccentColorSelected(AccentColor.Emerald))
        assertEquals(AccentColor.Emerald, vm.state.value.accentColor)
        assertEquals(AccentColor.Emerald, accentStore.get())
    }
}
