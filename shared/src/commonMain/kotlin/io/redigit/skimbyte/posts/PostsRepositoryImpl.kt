package io.redigit.skimbyte.posts

import io.redigit.skimbyte.network.Page
import io.redigit.skimbyte.network.toDomain

class PostsRepositoryImpl(
    private val postsApi: PostsApi,
    private val bookmarkIdsStore: BookmarkIdsStore,
) : PostsRepository {

    override suspend fun fetchPosts(topic: Topic, cursor: String?): Result<Page<Post>> = runCatching {
        postsApi.fetchPosts(topic, cursor).toDomain { dto ->
            dto.toDomain(isBookmarked = dto.id in bookmarkIdsStore.ids.value)
        }
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
}
