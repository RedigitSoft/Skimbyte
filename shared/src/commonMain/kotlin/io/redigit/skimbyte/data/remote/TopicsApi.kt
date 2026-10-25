package io.redigit.skimbyte.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.redigit.skimbyte.data.models.dto.TopicDto

// GET /v1/topics is live (skimbyte-server returns {id, label, apiValue} per topic, ordered by
// sort_order). `TopicsRepository` still tolerates a failed fetch and seeds with Topic.selectable,
// so a network blip just keeps the built-in defaults rather than failing.
interface TopicsApi {
    suspend fun fetchTopics(): List<TopicDto>
}

class KtorTopicsApi(private val client: HttpClient) : TopicsApi {
    override suspend fun fetchTopics(): List<TopicDto> =
        client.get("${ApiConfig.BASE_URL}/v1/topics").body()
}
