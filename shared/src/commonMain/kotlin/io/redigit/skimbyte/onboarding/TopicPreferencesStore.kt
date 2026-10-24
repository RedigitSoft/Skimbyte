package io.redigit.skimbyte.onboarding

import com.russhwolf.settings.Settings
import io.redigit.skimbyte.data.models.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TopicPreferencesStore {
    val selectedTopicsFlow: StateFlow<Set<Topic>>
    fun selectedTopics(): Set<Topic>
    fun setSelectedTopics(topics: Set<Topic>)
    fun hasCompletedOnboarding(): Boolean
    fun setOnboardingCompleted()
}

/**
 * Backed by multiplatform-settings rather than androidx.datastore: DataStore's non-Android/web
 * targets are not yet shipped, while this library has full wasmJs support (Web Storage-backed),
 * which this app's target list requires.
 */
class SettingsTopicPreferencesStore(private val settings: Settings) : TopicPreferencesStore {

    private val _selectedTopicsFlow = MutableStateFlow(selectedTopics())
    override val selectedTopicsFlow: StateFlow<Set<Topic>> = _selectedTopicsFlow.asStateFlow()

    override fun selectedTopics(): Set<Topic> {
        val stored = settings.getStringOrNull(KEY_SELECTED_TOPICS) ?: return emptySet()
        val topicsByName = Topic.entries.associateBy { it.name }
        return stored.split(",")
            .mapNotNull { topicsByName[it] }
            .toSet()
    }

    override fun setSelectedTopics(topics: Set<Topic>) {
        val encoded = topics.map { it.name }.joinToString(",")
        settings.putString(KEY_SELECTED_TOPICS, encoded)
        _selectedTopicsFlow.value = topics
    }

    override fun hasCompletedOnboarding(): Boolean {
        return settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override fun setOnboardingCompleted() {
        settings.putBoolean(KEY_ONBOARDING_COMPLETED, true)
    }

    private companion object {
        const val KEY_SELECTED_TOPICS = "selected_topics"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
