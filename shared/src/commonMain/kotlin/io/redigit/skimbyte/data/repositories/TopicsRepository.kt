package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.local.TopicPreferencesStore
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.models.dto.toDomain
import io.redigit.skimbyte.data.remote.TopicsApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The catalogue of selectable topics (the [Topic.All] pseudo-topic is excluded - callers add it
 * where a "show everything" option is wanted). Backed by [TopicPreferencesStore]'s persisted
 * catalogue (seeded with [Topic.selectable] on first launch) and reconciled against
 * `GET /v1/topics` once per app open — a failed fetch is tolerated and just keeps the stored
 * catalogue. Topics the server has that the catalogue doesn't surface in [pendingNewTopics]
 * until [acknowledgeNewTopics] is called, so the UI can announce them once.
 */
interface TopicsRepository {
    val topics: StateFlow<List<Topic>>

    /** Topics the latest [refresh] discovered that the user hasn't been told about yet. */
    val pendingNewTopics: StateFlow<List<Topic>>

    fun acknowledgeNewTopics()
    suspend fun refresh()
}

class TopicsRepositoryImpl(
    private val api: TopicsApi,
    private val store: TopicPreferencesStore,
    scope: CoroutineScope,
) : TopicsRepository {
    override val topics: StateFlow<List<Topic>> = store.knownTopicsFlow
        .map { known -> known.map { it.topic } }
        .stateIn(scope, SharingStarted.Eagerly, store.knownTopics().map { it.topic })

    private val _pendingNewTopics = MutableStateFlow<List<Topic>>(emptyList())
    override val pendingNewTopics: StateFlow<List<Topic>> = _pendingNewTopics.asStateFlow()

    init {
        scope.launch { refresh() }
    }

    override suspend fun refresh() {
        // Best-effort: a failed fetch (endpoint down, network blip) just keeps the stored
        // catalogue rather than emptying the topic list.
        runCatching { api.fetchTopics().map { it.toDomain() } }
            .onSuccess { fetched ->
                if (fetched.isEmpty()) return
                val discovered = store.syncKnownTopics(fetched)
                if (discovered.isNotEmpty()) _pendingNewTopics.value = discovered
            }
    }

    override fun acknowledgeNewTopics() {
        _pendingNewTopics.value = emptyList()
    }
}
