package io.redigit.skimbyte.data.local

import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.testing.FakeSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TopicPreferencesStoreTest {
    @Test
    fun `no topics selected and onboarding incomplete by default`() {
        val store = SettingsTopicPreferencesStore(FakeSettings())
        assertEquals(emptySet<Topic>(), store.selectedTopics())
        assertFalse(store.hasCompletedOnboarding())
    }

    @Test
    fun `set then get round-trips a topic set`() {
        val store = SettingsTopicPreferencesStore(FakeSettings())
        store.setSelectedTopics(setOf(Topic.Kotlin, Topic.Android))
        assertEquals(setOf(Topic.Kotlin, Topic.Android), store.selectedTopics())
    }

    @Test
    fun `setOnboardingCompleted persists`() {
        val store = SettingsTopicPreferencesStore(FakeSettings())
        store.setOnboardingCompleted()
        assertTrue(store.hasCompletedOnboarding())
    }

    @Test
    fun `a topic that exists only server-side survives the round-trip`() {
        val settings = FakeSettings()
        val ai = Topic(id = "ai", displayLabel = "AI", apiValue = "AI")
        SettingsTopicPreferencesStore(settings).setSelectedTopics(setOf(Topic.Kotlin, ai))

        // A fresh store (new app launch) must retrieve it without a matching built-in constant.
        assertEquals(setOf(Topic.Kotlin, ai), SettingsTopicPreferencesStore(settings).selectedTopics())
    }

    @Test
    fun `legacy comma-separated ids still resolve, unknown ids ignored`() {
        val settings = FakeSettings(mapOf("selected_topics" to "kotlin,not-a-real-topic"))
        val store = SettingsTopicPreferencesStore(settings)
        assertEquals(setOf(Topic.Kotlin), store.selectedTopics())
    }

    @Test
    fun `legacy pre-slug ids map to the renamed built-in topics`() {
        val settings = FakeSettings(mapOf("selected_topics" to "kmp,tooling,android"))
        val store = SettingsTopicPreferencesStore(settings)
        assertEquals(setOf(Topic.Kmp, Topic.Tooling, Topic.Android), store.selectedTopics())
    }
}
