package io.redigit.skimbyte.data.local

import com.russhwolf.settings.Settings
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.models.dto.TopicDto
import io.redigit.skimbyte.data.models.dto.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

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
        // Current format: a JSON list of {id, label, apiValue}, self-contained so a topic served
        // only by the backend survives a restart without needing a matching built-in constant.
        return runCatching { json.decodeFromString<List<TopicDto>>(stored).map { it.toDomain() }.toSet() }
            .getOrElse { decodeLegacyIds(stored) }
    }

    // Pre-JSON installs stored bare comma-separated ids, resolvable only against the built-in
    // defaults; "kmp"/"tooling" are those ids' spellings from before they were aligned with the
    // server's slugs.
    private fun decodeLegacyIds(stored: String): Set<Topic> {
        val topicsById = Topic.defaults.associateBy { it.id } +
            mapOf("kmp" to Topic.Kmp, "tooling" to Topic.Tooling)
        return stored.split(",")
            .mapNotNull { topicsById[it] }
            .toSet()
    }

    override fun setSelectedTopics(topics: Set<Topic>) {
        val encoded = json.encodeToString(
            topics.map { TopicDto(id = it.id, label = it.displayLabel, apiValue = it.apiValue) },
        )
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
        val json = Json { ignoreUnknownKeys = true }
    }
}
