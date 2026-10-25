package io.redigit.skimbyte.ui.screens.bookmarks

import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.repositories.Session
import io.redigit.skimbyte.data.remote.Page
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.data.repositories.PostsRepository
import io.redigit.skimbyte.data.models.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

private fun fakePost(id: String) = Post(
    id = id,
    topic = Topic.Kotlin.displayLabel,
    title = "Title $id",
    summaryMarkdown = "Summary",
    sourceName = "Source",
    sourceUrl = "https://example.com/$id",
    imageUrl = null,
    digestDate = null,
    publishedAt = Instant.parse("2026-10-13T00:00:00Z"),
    isBookmarked = true,
)

private class FakeAuthRepository(initialSession: Session?) : AuthRepository {
    private val _session = MutableStateFlow(initialSession)
    override val session: StateFlow<Session?> = _session
    override suspend fun login(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun register(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun completeGoogleSignIn(redirectUri: String): Result<Unit> = Result.success(Unit)
    override fun logout() {
        _session.value = null
    }
}

private class FakePostsRepository(private val fetchResult: (String?) -> Result<Page<Post>>) : PostsRepository {
    var unbookmarkCalls = mutableListOf<String>()
    var setBookmarkedResult: Result<Unit> = Result.success(Unit)

    override suspend fun fetchPosts(topic: Topic, cursor: String?) = error("unused")
    override suspend fun fetchBookmarkedPosts(cursor: String?): Result<Page<Post>> = fetchResult(cursor)
    override suspend fun setBookmarked(postId: String, bookmarked: Boolean): Result<Unit> {
        unbookmarkCalls += postId
        return setBookmarkedResult
    }
}

class BookmarksViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setupMainDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDownMainDispatcher() = Dispatchers.resetMain()

    @Test
    fun loggedOut_showsIsLoggedOut_withoutFetching() = runTest {
        var fetchCount = 0
        val repository = FakePostsRepository {
            fetchCount++
            Result.success(Page(emptyList(), null))
        }
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = null))

        assertTrue(viewModel.state.value.isLoggedOut)
        assertEquals(0, fetchCount)
    }

    @Test
    fun loggedIn_loadsBookmarkedPosts() = runTest {
        val posts = listOf(fakePost("1"), fakePost("2"))
        val repository = FakePostsRepository { Result.success(Page(posts, null)) }
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = Session("t")))

        assertFalse(viewModel.state.value.isLoggedOut)
        assertEquals(posts, viewModel.state.value.posts)
    }

    @Test
    fun onUnbookmark_removesPostOptimistically() = runTest {
        val posts = listOf(fakePost("1"), fakePost("2"))
        val repository = FakePostsRepository { Result.success(Page(posts, null)) }
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = Session("t")))

        viewModel.onIntent(BookmarksIntent.Unbookmark("1"))

        assertEquals(listOf("2"), viewModel.state.value.posts.map { it.id })
        assertEquals(listOf("1"), repository.unbookmarkCalls)
    }

    @Test
    fun onUnbookmark_failure_reloadsList() = runTest {
        val posts = listOf(fakePost("1"), fakePost("2"))
        val repository = FakePostsRepository { Result.success(Page(posts, null)) }
        repository.setBookmarkedResult = Result.failure(RuntimeException("boom"))
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = Session("t")))

        viewModel.onIntent(BookmarksIntent.Unbookmark("1"))

        // Reload restores the full list since the unbookmark failed server-side.
        assertEquals(posts.map { it.id }, viewModel.state.value.posts.map { it.id })
    }

    @Test
    fun refresh_reloadsFromFirstPage_andClearsRefreshing() = runTest {
        var lastCursor: String? = "unset"
        val repository = FakePostsRepository { cursor ->
            lastCursor = cursor
            Result.success(Page(listOf(fakePost("fresh")), nextCursor = null))
        }
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = Session("t")))

        viewModel.onIntent(BookmarksIntent.Refresh)

        assertEquals(listOf("fresh"), viewModel.state.value.posts.map { it.id })
        assertFalse(viewModel.state.value.isRefreshing)
        assertEquals(null, lastCursor) // refresh always fetches the first page
    }
}
