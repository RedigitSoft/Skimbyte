package io.redigit.skimbyte.data.local

import io.redigit.skimbyte.testing.FakeSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class AppConfigStoreTest {

    @Test
    fun cycleDigestTime_advancesThroughPresets_andPersists() {
        val settings = FakeSettings()
        val store = SettingsAppConfigStore(settings)

        assertEquals("08:30", store.digestTime.value)
        store.cycleDigestTime()
        assertEquals("12:00", store.digestTime.value)

        // A new store reading the same settings sees the persisted value.
        assertEquals("12:00", SettingsAppConfigStore(settings).digestTime.value)
    }

    @Test
    fun cycleSummaryLength_wrapsAround() {
        val store = SettingsAppConfigStore(FakeSettings())

        assertEquals(SummaryLength.Medium, store.summaryLength.value)
        store.cycleSummaryLength()
        assertEquals(SummaryLength.Long, store.summaryLength.value)
        store.cycleSummaryLength()
        assertEquals(SummaryLength.Short, store.summaryLength.value)
    }

    @Test
    fun setNotifications_persists() {
        val settings = FakeSettings()
        SettingsAppConfigStore(settings).setNotificationsEnabled(false)

        assertEquals(false, SettingsAppConfigStore(settings).notificationsEnabled.value)
    }
}
