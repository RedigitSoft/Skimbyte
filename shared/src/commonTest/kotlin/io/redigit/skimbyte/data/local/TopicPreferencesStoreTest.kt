package io.redigit.skimbyte.data.local

import io.redigit.skimbyte.data.models.SelectableTopic
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

    @Test
    fun `legacy JSON selected-list format migrates into the catalogue`() {
        // The interim format stored only the selected topics as a JSON TopicDto list.
        val settings = FakeSettings(
            mapOf("selected_topics" to """[{"id":"kotlin","label":"Kotlin","apiValue":"Kotlin"},{"id":"ai","label":"AI","apiValue":"AI"}]"""),
        )
        val store = SettingsTopicPreferencesStore(settings)
        val ai = Topic(id = "ai", displayLabel = "AI", apiValue = "AI")

        assertEquals(setOf(Topic.Kotlin, ai), store.selectedTopics())
        // The rebuilt catalogue is the built-in defaults plus the selected server-only topic.
        assertEquals(Topic.selectable + ai, store.knownTopics().map { it.topic })
    }

    @Test
    fun `catalogue defaults to the built-in selectable topics, unselected`() {
        val store = SettingsTopicPreferencesStore(FakeSettings())
        assertEquals(Topic.selectable.map { SelectableTopic(it, isSelected = false) }, store.knownTopics())
    }

    @Test
    fun `syncKnownTopics returns the newly discovered topics, unselected`() {
        val store = SettingsTopicPreferencesStore(FakeSettings())
        store.setSelectedTopics(setOf(Topic.Kotlin))
        val ai = Topic(id = "ai", displayLabel = "AI", apiValue = "AI")

        val discovered = store.syncKnownTopics(Topic.selectable + ai)

        assertEquals(listOf(ai), discovered)
        assertEquals(setOf(Topic.Kotlin), store.selectedTopics()) // discovery never auto-follows
        assertEquals(
            Topic.selectable.map { SelectableTopic(it, it == Topic.Kotlin) } + SelectableTopic(ai, false),
            store.knownTopics(),
        )
    }

    @Test
    fun `syncKnownTopics adopts server order, drops unserved topics, and is quiet when nothing is new`() {
        val store = SettingsTopicPreferencesStore(FakeSettings())
        store.setSelectedTopics(setOf(Topic.Android))

        val discovered = store.syncKnownTopics(listOf(Topic.Android, Topic.Kotlin)) // reordered, two topics dropped

        assertEquals(emptyList<Topic>(), discovered)
        assertEquals(
            listOf(SelectableTopic(Topic.Android, true), SelectableTopic(Topic.Kotlin, false)),
            store.knownTopics(),
        )
    }

    @Test
    fun `flows emit reactively on sync and selection changes`() {
        val store = SettingsTopicPreferencesStore(FakeSettings())
        val ai = Topic(id = "ai", displayLabel = "AI", apiValue = "AI")

        store.syncKnownTopics(Topic.selectable + ai)
        assertEquals(Topic.selectable + ai, store.knownTopicsFlow.value.map { it.topic })

        store.setSelectedTopics(setOf(ai))
        assertEquals(setOf(ai), store.selectedTopicsFlow.value)
        assertTrue(store.knownTopicsFlow.value.single { it.topic == ai }.isSelected)
    }

    @Test
    fun `a synced catalogue persists across store instances`() {
        val settings = FakeSettings()
        val ai = Topic(id = "ai", displayLabel = "AI", apiValue = "AI")
        SettingsTopicPreferencesStore(settings).syncKnownTopics(Topic.selectable + ai)

        val reopened = SettingsTopicPreferencesStore(settings)
        assertEquals(Topic.selectable + ai, reopened.knownTopics().map { it.topic })
    }
}
