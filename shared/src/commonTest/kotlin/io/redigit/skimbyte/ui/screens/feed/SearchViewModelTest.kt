package io.redigit.skimbyte.ui.screens.feed

import io.redigit.skimbyte.data.remote.Page
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.data.repositories.PostsRepository
import io.redigit.skimbyte.data.models.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

private fun postNamed(title: String, id: String = title) = Post(
    id = id,
    topic = Topic.Kotlin.displayLabel,
    title = title,
    summaryMarkdown = "Summary for $title",
    sourceName = "Source",
    sourceUrl = "https://example.com/$id",
    imageUrl = null,
    digestDate = null,
    publishedAt = Instant.parse("2026-10-13T00:00:00Z"),
    isBookmarked = false,
)

private class SearchFakePostsRepository(private val posts: List<Post>) : PostsRepository {
    var setBookmarkedCalls = mutableListOf<Pair<String, Boolean>>()
    var setBookmarkedResult: Result<Unit> = Result.success(Unit)

    override suspend fun fetchPosts(topic: Topic, cursor: String?): Result<Page<Post>> =
        Result.success(Page(posts, null))
    override suspend fun fetchBookmarkedPosts(cursor: String?): Result<Page<Post>> =
        Result.success(Page(emptyList(), null))
    override suspend fun setBookmarked(postId: String, bookmarked: Boolean): Result<Unit> {
        setBookmarkedCalls += postId to bookmarked
        return setBookmarkedResult
    }
}

private fun fakePostsRepositoryWith(posts: List<Post>) = SearchFakePostsRepository(posts)

class SearchViewModelTest {

    // SearchViewModel uses viewModelScope, which defaults to Dispatchers.Main.immediate; redirect
    // Main to the test's deterministic scheduler so launched coroutines complete synchronously
    // within runTest instead of racing on a real background dispatcher.
    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setupMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDownMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty query yields no results even after posts load`() = runTest {
        val vm = SearchViewModel(fakePostsRepositoryWith(listOf(postNamed("Kotlin 2.4"))))
        advanceUntilIdle()
        assertEquals(emptyList(), vm.state.value.results)
    }

    @Test
    fun `query filters loaded posts by title substring`() = runTest {
        val vm = SearchViewModel(fakePostsRepositoryWith(listOf(postNamed("Kotlin 2.4"), postNamed("Android Studio"))))
        advanceUntilIdle()
        vm.onIntent(SearchIntent.QueryChanged("kotlin"))
        assertEquals(listOf("Kotlin 2.4"), vm.state.value.results.map { it.title })
    }

    @Test
    fun `onBookmarkClick toggles optimistically and reverts on failure`() = runTest {
        val repository = fakePostsRepositoryWith(listOf(postNamed("Kotlin 2.4", id = "1")))
        repository.setBookmarkedResult = Result.failure(RuntimeException("network error"))
        val vm = SearchViewModel(repository)
        advanceUntilIdle()
        vm.onIntent(SearchIntent.QueryChanged("kotlin"))

        vm.onIntent(SearchIntent.BookmarkClicked("1"))

        assertEquals(listOf("1" to true), repository.setBookmarkedCalls)
        assertTrue(vm.state.value.pendingBookmarkToggles.isEmpty())
        assertEquals(false, vm.state.value.results.first { it.id == "1" }.isBookmarked)
    }
}
