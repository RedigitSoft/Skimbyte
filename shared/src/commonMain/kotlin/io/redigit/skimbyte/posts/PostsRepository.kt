package io.redigit.skimbyte.posts

import io.redigit.skimbyte.network.Page

interface PostsRepository {
    suspend fun fetchPosts(topic: Topic, cursor: String?): Result<Page<Post>>
    suspend fun fetchBookmarkedPosts(cursor: String?): Result<Page<Post>>
    suspend fun setBookmarked(postId: String, bookmarked: Boolean): Result<Unit>
}
