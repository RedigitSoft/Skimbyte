package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.local.SettingsFeedCache
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.models.dto.PageDto
import io.redigit.skimbyte.data.models.dto.PostDto
import io.redigit.skimbyte.data.remote.PostsApi
import io.redigit.skimbyte.testing.FakeSettings
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakePostsApi : PostsApi {
    var result: Result<PageDto<PostDto>> = Result.success(PageDto(emptyList(), null))
    override suspend fun fetchPosts(topic: Topic, cursor: String?): PageDto<PostDto> = result.getOrThrow()
    override suspend fun fetchBookmarkedPosts(cursor: String?): PageDto<PostDto> = PageDto(emptyList(), null)
    override suspend fun bookmark(postId: String) {}
    override suspend fun unbookmark(postId: String) {}
}

class PostsRepositoryImplTest {

    private fun post(id: String) = PostDto(
        id = id,
        topic = "Kotlin",
        title = "Title $id",
        summaryMarkdown = "Summary",
        sourceName = "Source",
        sourceUrl = "https://example.test/$id",
        publishedAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    private fun repo(api: PostsApi, settings: FakeSettings) =
        PostsRepositoryImpl(api, BookmarkIdsStore(api), SettingsFeedCache(settings))

    @Test
    fun fetchPosts_success_cachesTheFirstPage() = runTest {
        val api = FakePostsApi().apply { result = Result.success(PageDto(listOf(post("a")), null)) }
        val settings = FakeSettings()

        val result = repo(api, settings).fetchPosts(Topic.All, cursor = null)

        assertTrue(result.isSuccess)
        assertEquals(listOf("a"), SettingsFeedCache(settings).load("all")?.items?.map { it.id })
    }

    @Test
    fun fetchPosts_firstPageFailure_servesCachedPage() = runTest {
        val settings = FakeSettings()
        // Seed the cache with a prior successful fetch.
        repo(FakePostsApi().apply { result = Result.success(PageDto(listOf(post("a")), null)) }, settings)
            .fetchPosts(Topic.All, cursor = null)

        val failing = FakePostsApi().apply { result = Result.failure(RuntimeException("offline")) }
        val result = repo(failing, settings).fetchPosts(Topic.All, cursor = null)

        assertTrue(result.isSuccess, "offline first load should serve the cached page")
        assertEquals(listOf("a"), result.getOrThrow().items.map { it.id })
    }

    @Test
    fun fetchPosts_firstPageFailure_withNoCache_returnsFailure() = runTest {
        val failing = FakePostsApi().apply { result = Result.failure(RuntimeException("offline")) }

        val result = repo(failing, FakeSettings()).fetchPosts(Topic.All, cursor = null)

        assertTrue(result.isFailure, "no cache means the failure must surface")
    }

    @Test
    fun fetchPosts_deepPageFailure_doesNotServeCache() = runTest {
        val settings = FakeSettings()
        repo(FakePostsApi().apply { result = Result.success(PageDto(listOf(post("a")), null)) }, settings)
            .fetchPosts(Topic.All, cursor = null)

        val failing = FakePostsApi().apply { result = Result.failure(RuntimeException("offline")) }
        val result = repo(failing, settings).fetchPosts(Topic.All, cursor = "page-2")

        assertTrue(result.isFailure, "a deeper page has no cache fallback")
    }
}
