package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.local.FeedCache
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.data.models.toDomain
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.remote.PostsApi
import io.redigit.skimbyte.data.models.dto.PageDto
import io.redigit.skimbyte.data.models.dto.PostDto

import io.redigit.skimbyte.data.remote.Page
import io.redigit.skimbyte.data.remote.toDomain

class PostsRepositoryImpl(
    private val postsApi: PostsApi,
    private val bookmarkIdsStore: BookmarkIdsStore,
    private val feedCache: FeedCache,
) : PostsRepository {

    override suspend fun fetchPosts(topic: Topic, cursor: String?): Result<Page<Post>> {
        // null cursor == the first page; that's the one we cache and fall back to offline. Deeper
        // pages always require the network.
        val cacheKey = topic.apiValue ?: ALL_TOPICS_KEY
        return runCatching { postsApi.fetchPosts(topic, cursor) }.fold(
            onSuccess = { page ->
                if (cursor == null) feedCache.save(cacheKey, page)
                Result.success(page.toDomainPage())
            },
            onFailure = { cause ->
                // Offline or a failed first load: serve the last cached page so the brief still
                // shows instead of an error. Nothing cached (or a deep page) surfaces the failure.
                val cached = if (cursor == null) feedCache.load(cacheKey) else null
                cached?.let { Result.success(it.toDomainPage()) } ?: Result.failure(cause)
            },
        )
    }

    private fun PageDto<PostDto>.toDomainPage(): Page<Post> = toDomain { dto ->
        dto.toDomain(isBookmarked = dto.id in bookmarkIdsStore.ids.value)
    }

    override suspend fun fetchBookmarkedPosts(cursor: String?): Result<Page<Post>> = runCatching {
        postsApi.fetchBookmarkedPosts(cursor).toDomain { dto -> dto.toDomain(isBookmarked = true) }
    }

    override suspend fun setBookmarked(postId: String, bookmarked: Boolean): Result<Unit> = runCatching {
        // Optimistic: flip local state first for instant UI feedback, revert only on failure.
        if (bookmarked) {
            bookmarkIdsStore.markBookmarked(postId)
            runCatching { postsApi.bookmark(postId) }
                .onFailure { bookmarkIdsStore.markUnbookmarked(postId) }
                .getOrThrow()
        } else {
            bookmarkIdsStore.markUnbookmarked(postId)
            runCatching { postsApi.unbookmark(postId) }
                .onFailure { bookmarkIdsStore.markBookmarked(postId) }
                .getOrThrow()
        }
    }

    private companion object {
        // Topic.All has no apiValue; give its cache a stable key of its own.
        const val ALL_TOPICS_KEY = "all"
    }
}
