package io.redigit.skimbyte.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeModeStoreTest {
    @Test
    fun `defaults to System when nothing stored`() {
        val store = SettingsThemeModeStore(MapSettings())
        assertEquals(ThemeMode.System, store.get())
    }

    @Test
    fun `set then get round-trips`() {
        val store = SettingsThemeModeStore(MapSettings())
        store.set(ThemeMode.Light)
        assertEquals(ThemeMode.Light, store.get())
    }

    @Test
    fun `unknown stored value resolves to System`() {
        val settings = MapSettings()
        settings.putString("theme_mode", "UnknownValue")
        val store = SettingsThemeModeStore(settings)
        assertEquals(ThemeMode.System, store.get())
    }
}
