package io.redigit.skimbyte.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.redigit.skimbyte.testing.FakeSettings

class AccentColorStoreTest {
    @Test
    fun `assigns a random accent on first use and persists it`() {
        val settings = FakeSettings()
        val chosen = SettingsAccentColorStore(settings).get()
        // A real accent was chosen and written to storage, so a later read returns the same one.
        assertTrue(chosen in AccentColor.entries)
        assertEquals(chosen, SettingsAccentColorStore(settings).get())
    }

    @Test
    fun `set then get round-trips`() {
        val store = SettingsAccentColorStore(FakeSettings())
        store.set(AccentColor.Violet)
        assertEquals(AccentColor.Violet, store.get())
    }

    @Test
    fun `unknown stored value resolves to the default accent`() {
        val settings = FakeSettings()
        settings.putString("accent_color", "UnknownValue")
        val store = SettingsAccentColorStore(settings)
        assertEquals(AccentColor.Default, store.get())
    }

    @Test
    fun `accentColorFlow emits the new value after set`() {
        val store = SettingsAccentColorStore(FakeSettings())
        store.set(AccentColor.Emerald)
        assertEquals(AccentColor.Emerald, store.accentColorFlow.value)
    }
}
