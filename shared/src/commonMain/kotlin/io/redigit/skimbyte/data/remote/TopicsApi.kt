package io.redigit.skimbyte.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.redigit.skimbyte.data.models.dto.TopicDto

// PLACEHOLDER-PENDING-BACKEND-CONFIRMATION: GET /v1/topics is not live yet. `TopicsRepository` seeds
// with Topic.selectable and tolerates a failed fetch, so until the endpoint exists this is a no-op
// and the app shows the built-in default topics.
interface TopicsApi {
    suspend fun fetchTopics(): List<TopicDto>
}

class KtorTopicsApi(private val client: HttpClient) : TopicsApi {
    override suspend fun fetchTopics(): List<TopicDto> =
        client.get("${ApiConfig.BASE_URL}/v1/topics").body()
}
