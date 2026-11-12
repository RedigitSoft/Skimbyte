package io.redigit.skimbyte.ui.screens.bookmarks

import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.repositories.BookmarkIdsStore
import io.redigit.skimbyte.data.repositories.Session
import io.redigit.skimbyte.data.remote.Page
import io.redigit.skimbyte.data.remote.PostsApi
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.data.models.dto.PageDto
import io.redigit.skimbyte.data.models.dto.PostDto
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

// Same pattern as FeedViewModelTest: the real store on a stub PostsApi — its mutation/flow logic
// is pure in-memory, so a fake would just duplicate the class under test elsewhere.
private fun fakeBookmarkIdsStore() = BookmarkIdsStore(object : PostsApi {
    override suspend fun fetchPosts(topic: Topic, cursor: String?) = error("unused in this test")
    override suspend fun fetchBookmarkedPosts(cursor: String?) = PageDto<PostDto>(emptyList(), null)
    override suspend fun bookmark(postId: String) = Unit
    override suspend fun unbookmark(postId: String) = Unit
})

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
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = null), fakeBookmarkIdsStore())

        assertTrue(viewModel.state.value.isLoggedOut)
        assertEquals(0, fetchCount)
    }

    @Test
    fun loggedIn_loadsBookmarkedPosts() = runTest {
        val posts = listOf(fakePost("1"), fakePost("2"))
        val repository = FakePostsRepository { Result.success(Page(posts, null)) }
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = Session("t")), fakeBookmarkIdsStore())

        assertFalse(viewModel.state.value.isLoggedOut)
        assertEquals(posts, viewModel.state.value.posts)
    }

    @Test
    fun onUnbookmark_removesPostOptimistically() = runTest {
        val posts = listOf(fakePost("1"), fakePost("2"))
        val repository = FakePostsRepository { Result.success(Page(posts, null)) }
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = Session("t")), fakeBookmarkIdsStore())

        viewModel.onIntent(BookmarksIntent.Unbookmark("1"))

        assertEquals(listOf("2"), viewModel.state.value.posts.map { it.id })
        assertEquals(listOf("1"), repository.unbookmarkCalls)
    }

    @Test
    fun onUnbookmark_failure_reloadsList() = runTest {
        val posts = listOf(fakePost("1"), fakePost("2"))
        val repository = FakePostsRepository { Result.success(Page(posts, null)) }
        repository.setBookmarkedResult = Result.failure(RuntimeException("boom"))
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = Session("t")), fakeBookmarkIdsStore())

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
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = Session("t")), fakeBookmarkIdsStore())

        viewModel.onIntent(BookmarksIntent.Refresh)

        assertEquals(listOf("fresh"), viewModel.state.value.posts.map { it.id })
        assertFalse(viewModel.state.value.isRefreshing)
        assertEquals(null, lastCursor) // refresh always fetches the first page
    }

    @Test
    fun bookmarkAddedElsewhere_quietlyReloadsList() = runTest {
        var serverPosts = listOf(fakePost("1"), fakePost("2"))
        var fetchCount = 0
        val repository = FakePostsRepository {
            fetchCount++
            Result.success(Page(serverPosts, null))
        }
        val store = fakeBookmarkIdsStore()
        store.markBookmarked("1")
        store.markBookmarked("2")
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = Session("t")), store)
        val fetchesAfterLoad = fetchCount

        // Bookmarking on the feed flips the shared store; the list should re-fetch on its own.
        serverPosts = listOf(fakePost("3"), fakePost("1"), fakePost("2"))
        store.markBookmarked("3")

        assertEquals(listOf("3", "1", "2"), viewModel.state.value.posts.map { it.id })
        assertEquals(fetchesAfterLoad + 1, fetchCount)
    }

    @Test
    fun bookmarkRemovedElsewhere_dropsItLocally_withoutRefetch() = runTest {
        val posts = listOf(fakePost("1"), fakePost("2"))
        var fetchCount = 0
        val repository = FakePostsRepository {
            fetchCount++
            Result.success(Page(posts, null))
        }
        val store = fakeBookmarkIdsStore()
        store.markBookmarked("1")
        store.markBookmarked("2")
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = Session("t")), store)
        val fetchesAfterLoad = fetchCount

        store.markUnbookmarked("1")

        assertEquals(listOf("2"), viewModel.state.value.posts.map { it.id })
        assertEquals(fetchesAfterLoad, fetchCount)
    }

    @Test
    fun unbookmarkElsewhere_dropsOnlyThatPost_evenWhenDeepPagesArentInTheStore() = runTest {
        // The store only holds the newest page of ids ("1"); "2" was paginated in deeper and is
        // legitimately absent from it. Reconciling must react to the "1" removal *transition*,
        // not filter by absolute store membership, or "2" would be dropped too.
        val posts = listOf(fakePost("1"), fakePost("2"))
        val repository = FakePostsRepository { Result.success(Page(posts, null)) }
        val store = fakeBookmarkIdsStore()
        store.markBookmarked("1")
        val viewModel = BookmarksViewModel(repository, FakeAuthRepository(initialSession = Session("t")), store)

        store.markUnbookmarked("1")

        assertEquals(listOf("2"), viewModel.state.value.posts.map { it.id })
    }
}
