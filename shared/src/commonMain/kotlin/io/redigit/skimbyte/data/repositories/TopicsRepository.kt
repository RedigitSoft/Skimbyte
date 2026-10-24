package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.models.dto.toDomain
import io.redigit.skimbyte.data.remote.TopicsApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The catalogue of selectable topics (the [Topic.All] pseudo-topic is excluded - callers add it
 * where a "show everything" option is wanted). Seeded with [Topic.selectable] so the UI always has
 * something to show, and refreshed from the backend once `GET /topics` is live (today the fetch is a
 * tolerated no-op, see [TopicsApi]).
 */
interface TopicsRepository {
    val topics: StateFlow<List<Topic>>
    suspend fun refresh()
}

class TopicsRepositoryImpl(
    private val api: TopicsApi,
    scope: CoroutineScope,
) : TopicsRepository {
    private val _topics = MutableStateFlow(Topic.selectable)
    override val topics: StateFlow<List<Topic>> = _topics.asStateFlow()

    init {
        scope.launch { refresh() }
    }

    override suspend fun refresh() {
        // Best-effort: a failed fetch (endpoint not live, network blip) just keeps the seeded
        // defaults rather than emptying the topic list.
        runCatching { api.fetchTopics().map { it.toDomain() } }
            .onSuccess { fetched -> if (fetched.isNotEmpty()) _topics.value = fetched }
    }
}
