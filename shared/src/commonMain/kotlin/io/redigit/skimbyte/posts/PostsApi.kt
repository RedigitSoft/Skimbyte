package io.redigit.skimbyte.posts

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.redigit.skimbyte.network.ApiConfig
import io.redigit.skimbyte.data.models.dto.PageDto
import io.redigit.skimbyte.posts.dto.PostDto

// PLACEHOLDER-PENDING-BACKEND-CONFIRMATION
interface PostsApi {
    suspend fun fetchPosts(topic: Topic, cursor: String?): PageDto<PostDto>
    suspend fun fetchBookmarkedPosts(cursor: String?): PageDto<PostDto>
    suspend fun bookmark(postId: String)
    suspend fun unbookmark(postId: String)
}

class KtorPostsApi(private val client: HttpClient) : PostsApi {

    override suspend fun fetchPosts(topic: Topic, cursor: String?): PageDto<PostDto> =
        client.get("${ApiConfig.BASE_URL}/v1/posts") {
            topic.apiValue?.let { parameter("topic", it) }
            cursor?.let { parameter("cursor", it) }
            parameter("limit", PAGE_LIMIT)
        }.body()

    override suspend fun fetchBookmarkedPosts(cursor: String?): PageDto<PostDto> =
        client.get("${ApiConfig.BASE_URL}/v1/bookmarks") {
            cursor?.let { parameter("cursor", it) }
            parameter("limit", PAGE_LIMIT)
        }.body()

    override suspend fun bookmark(postId: String) {
        client.post("${ApiConfig.BASE_URL}/v1/posts/$postId/bookmarks")
    }

    override suspend fun unbookmark(postId: String) {
        client.delete("${ApiConfig.BASE_URL}/v1/posts/$postId/bookmarks")
    }

    private companion object {
        const val PAGE_LIMIT = 20
    }
}
