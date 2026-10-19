package io.redigit.skimbyte.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import io.redigit.skimbyte.testing.FakeSettings

class ThemeModeStoreTest {
    @Test
    fun `defaults to System when nothing stored`() {
        val store = SettingsThemeModeStore(FakeSettings())
        assertEquals(ThemeMode.System, store.get())
    }

    @Test
    fun `set then get round-trips`() {
        val store = SettingsThemeModeStore(FakeSettings())
        store.set(ThemeMode.Light)
        assertEquals(ThemeMode.Light, store.get())
    }

    @Test
    fun `unknown stored value resolves to System`() {
        val settings = FakeSettings()
        settings.putString("theme_mode", "UnknownValue")
        val store = SettingsThemeModeStore(settings)
        assertEquals(ThemeMode.System, store.get())
    }

    @Test
    fun `themeModeFlow emits the new value after set`() {
        val store = SettingsThemeModeStore(FakeSettings())
        store.set(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, store.themeModeFlow.value)
    }
}
