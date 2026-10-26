package io.redigit.skimbyte.data.local

import com.russhwolf.settings.Settings
import io.redigit.skimbyte.data.models.SelectableTopic
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.models.dto.TopicDto
import io.redigit.skimbyte.data.models.dto.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface TopicPreferencesStore {
    /**
     * The full known topic catalogue, each entry flagged with whether the user follows it. The
     * single source of truth the rest of the app reacts to: updated by [syncKnownTopics] (server
     * fetch) and [setSelectedTopics] (user editing interests), and persisted across launches.
     */
    val knownTopicsFlow: StateFlow<List<SelectableTopic>>

    /** The followed subset of [knownTopicsFlow] — what the feed header shows after "All". */
    val selectedTopicsFlow: StateFlow<Set<Topic>>

    fun knownTopics(): List<SelectableTopic>
    fun selectedTopics(): Set<Topic>
    fun setSelectedTopics(topics: Set<Topic>)

    /**
     * Reconcile a catalogue fetched from `GET /v1/topics` with the known one: the server list wins
     * (its order follows `sort_order`; topics it no longer serves drop out), selection flags are
     * preserved by id, and topics never seen before come back as the return value so the UI can
     * announce them. Newly discovered topics start unselected.
     */
    fun syncKnownTopics(fetched: List<Topic>): List<Topic>

    fun hasCompletedOnboarding(): Boolean
    fun setOnboardingCompleted()
}

/** The persisted shape of one catalogue entry. */
@Serializable
private data class StoredTopic(val id: String, val label: String, val apiValue: String?, val selected: Boolean)

/**
 * Backed by multiplatform-settings rather than androidx.datastore: DataStore's non-Android/web
 * targets are not yet shipped, while this library has full wasmJs support (Web Storage-backed),
 * which this app's target list requires.
 */
class SettingsTopicPreferencesStore(private val settings: Settings) : TopicPreferencesStore {

    private val _knownTopicsFlow = MutableStateFlow(load())
    override val knownTopicsFlow: StateFlow<List<SelectableTopic>> = _knownTopicsFlow.asStateFlow()

    private val _selectedTopicsFlow = MutableStateFlow(_knownTopicsFlow.value.selectedSet())
    override val selectedTopicsFlow: StateFlow<Set<Topic>> = _selectedTopicsFlow.asStateFlow()

    override fun knownTopics(): List<SelectableTopic> = _knownTopicsFlow.value

    override fun selectedTopics(): Set<Topic> = _selectedTopicsFlow.value

    override fun setSelectedTopics(topics: Set<Topic>) {
        val selectedIds = topics.map { it.id }.toSet()
        // A selected topic missing from the catalogue (picked from a fetch newer than the stored
        // one) is appended rather than dropped.
        val known = _knownTopicsFlow.value.map { it.topic }
        val catalogue = known + topics.filter { picked -> known.none { it.id == picked.id } }
        persist(catalogue.map { SelectableTopic(it, it.id in selectedIds) })
    }

    override fun syncKnownTopics(fetched: List<Topic>): List<Topic> {
        val current = _knownTopicsFlow.value
        val knownIds = current.map { it.topic.id }.toSet()
        val selectedIds = current.filter { it.isSelected }.map { it.topic.id }.toSet()
        val discovered = fetched.filter { it.id !in knownIds }
        persist(fetched.map { SelectableTopic(it, it.id in selectedIds) })
        return discovered
    }

    override fun hasCompletedOnboarding(): Boolean {
        return settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override fun setOnboardingCompleted() {
        settings.putBoolean(KEY_ONBOARDING_COMPLETED, true)
    }

    private fun persist(catalogue: List<SelectableTopic>) {
        val encoded = json.encodeToString(
            catalogue.map { StoredTopic(it.topic.id, it.topic.displayLabel, it.topic.apiValue, it.isSelected) },
        )
        settings.putString(KEY_KNOWN_TOPICS, encoded)
        _knownTopicsFlow.value = catalogue
        _selectedTopicsFlow.value = catalogue.selectedSet()
    }

    private fun load(): List<SelectableTopic> {
        settings.getStringOrNull(KEY_KNOWN_TOPICS)?.let { stored ->
            runCatching { json.decodeFromString<List<StoredTopic>>(stored) }.getOrNull()?.let { entries ->
                return entries.map { SelectableTopic(Topic(it.id, it.label, it.apiValue), it.selected) }
            }
        }
        // Migration: older installs stored only the selected set under KEY_LEGACY_SELECTED (a JSON
        // TopicDto list, or before that bare comma-separated ids). Rebuild the catalogue as the
        // built-in defaults plus whatever was selected.
        val selected = loadLegacySelected()
        val catalogue = Topic.selectable + selected.filter { s -> Topic.selectable.none { it.id == s.id } }
        return catalogue.map { topic -> SelectableTopic(topic, selected.any { it.id == topic.id }) }
    }

    private fun loadLegacySelected(): Set<Topic> {
        val stored = settings.getStringOrNull(KEY_LEGACY_SELECTED) ?: return emptySet()
        runCatching { json.decodeFromString<List<TopicDto>>(stored) }.getOrNull()?.let { dtos ->
            return dtos.map { it.toDomain() }.toSet()
        }
        // Oldest format: bare ids; "kmp"/"tooling" are those ids' spellings from before they were
        // aligned with the server's slugs.
        val topicsById = Topic.defaults.associateBy { it.id } +
            mapOf("kmp" to Topic.Kmp, "tooling" to Topic.Tooling)
        return stored.split(",").mapNotNull { topicsById[it] }.toSet()
    }

    private fun List<SelectableTopic>.selectedSet(): Set<Topic> =
        filter { it.isSelected }.map { it.topic }.toSet()

    private companion object {
        const val KEY_KNOWN_TOPICS = "known_topics"
        const val KEY_LEGACY_SELECTED = "selected_topics"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        val json = Json { ignoreUnknownKeys = true }
    }
}
