package io.redigit.skimbyte.feed

import io.redigit.skimbyte.auth.AuthRepository
import io.redigit.skimbyte.auth.Session
import io.redigit.skimbyte.network.Page
import io.redigit.skimbyte.network.dto.PageDto
import io.redigit.skimbyte.onboarding.TopicPreferencesStore
import io.redigit.skimbyte.posts.BookmarkIdsStore
import io.redigit.skimbyte.posts.Post
import io.redigit.skimbyte.posts.PostsApi
import io.redigit.skimbyte.posts.PostsRepository
import io.redigit.skimbyte.posts.Topic
import io.redigit.skimbyte.posts.dto.PostDto
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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

private fun fakePost(id: String, topic: Topic = Topic.Kotlin, isBookmarked: Boolean = false) = Post(
    id = id,
    topic = topic.displayLabel,
    title = "Title $id",
    summaryMarkdown = "Summary $id",
    sourceName = "Source",
    sourceUrl = "https://example.com/$id",
    imageUrl = null,
    digestDate = null,
    publishedAt = Instant.parse("2026-10-13T00:00:00Z"),
    isBookmarked = isBookmarked,
)

private class FakeAuthRepository(initialSession: Session? = null) : AuthRepository {
    private val _session = MutableStateFlow(initialSession)
    override val session: StateFlow<Session?> = _session
    override suspend fun login(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun register(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun completeGoogleSignIn(redirectUri: String): Result<Unit> = Result.success(Unit)
    override fun logout() {
        _session.value = null
    }
    fun setLoggedIn(accessToken: String = "token") {
        _session.value = Session(accessToken)
    }
}

private class FakePostsRepository(
    private val fetchResult: (Topic, String?) -> Result<Page<Post>>,
) : PostsRepository {
    var setBookmarkedCalls = mutableListOf<Pair<String, Boolean>>()
    var setBookmarkedResult: Result<Unit> = Result.success(Unit)

    override suspend fun fetchPosts(topic: Topic, cursor: String?): Result<Page<Post>> = fetchResult(topic, cursor)
    override suspend fun fetchBookmarkedPosts(cursor: String?): Result<Page<Post>> = Result.success(Page(emptyList(), null))
    override suspend fun setBookmarked(postId: String, bookmarked: Boolean): Result<Unit> {
        setBookmarkedCalls += postId to bookmarked
        return setBookmarkedResult
    }
}

private fun fakeBookmarkIdsStore() = BookmarkIdsStore(object : PostsApi {
    override suspend fun fetchPosts(topic: Topic, cursor: String?) = error("unused in this test")
    override suspend fun fetchBookmarkedPosts(cursor: String?) = PageDto<PostDto>(emptyList(), null)
    override suspend fun bookmark(postId: String) = Unit
    override suspend fun unbookmark(postId: String) = Unit
})

private class FakeTopicPreferencesStore : TopicPreferencesStore {
    private var topics: Set<Topic> = emptySet()
    private var onboardingCompleted: Boolean = false

    override fun selectedTopics(): Set<Topic> = topics

    override fun setSelectedTopics(topics: Set<Topic>) {
        this.topics = topics
    }

    override fun hasCompletedOnboarding(): Boolean = onboardingCompleted

    override fun setOnboardingCompleted() {
        onboardingCompleted = true
    }
}

class FeedViewModelTest {

    // FeedViewModel uses viewModelScope, which defaults to Dispatchers.Main.immediate; redirect
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
    fun initialLoad_populatesPosts_andNextCursor() = runTest {
        val posts = listOf(fakePost("1"), fakePost("2"))
        val repository = FakePostsRepository { _, _ -> Result.success(Page(posts, "cursor-2")) }
        val viewModel = FeedViewModel(repository, FakeAuthRepository(), fakeBookmarkIdsStore(), FakeTopicPreferencesStore())

        assertEquals(posts, viewModel.uiState.value.posts)
        assertEquals("cursor-2", viewModel.uiState.value.nextCursor)
        assertFalse(viewModel.uiState.value.isLoadingInitial)
    }

    @Test
    fun initialLoad_failure_setsNetworkError() = runTest {
        val repository = FakePostsRepository { _, _ -> Result.failure(RuntimeException("boom")) }
        val viewModel = FeedViewModel(repository, FakeAuthRepository(), fakeBookmarkIdsStore(), FakeTopicPreferencesStore())

        assertEquals(FeedError.Network, viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.posts.isEmpty())
    }

    @Test
    fun onTopicSelected_resetsPagination_andRefetchesForNewTopic() = runTest {
        var lastRequestedTopic: Topic? = null
        val repository = FakePostsRepository { topic, cursor ->
            lastRequestedTopic = topic
            Result.success(Page(listOf(fakePost("1", topic = topic)), nextCursor = null))
        }
        val viewModel = FeedViewModel(repository, FakeAuthRepository(), fakeBookmarkIdsStore(), FakeTopicPreferencesStore())

        viewModel.onTopicSelected(Topic.Android)

        assertEquals(Topic.Android, viewModel.uiState.value.selectedTopic)
        assertEquals(Topic.Android, lastRequestedTopic)
        assertEquals(0, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun onTopicSelected_sameTopic_isNoOp() = runTest {
        var fetchCount = 0
        val repository = FakePostsRepository { _, _ ->
            fetchCount++
            Result.success(Page(emptyList(), null))
        }
        val viewModel = FeedViewModel(repository, FakeAuthRepository(), fakeBookmarkIdsStore(), FakeTopicPreferencesStore())
        val countAfterInitialLoad = fetchCount

        viewModel.onTopicSelected(Topic.All)

        assertEquals(countAfterInitialLoad, fetchCount)
    }

    @Test
    fun onPageChanged_nearEnd_triggersLoadMore() = runTest {
        var fetchCalls = 0
        val repository = FakePostsRepository { _, cursor ->
            fetchCalls++
            if (cursor == null) {
                Result.success(Page(List(5) { fakePost("$it") }, nextCursor = "more"))
            } else {
                Result.success(Page(List(5) { fakePost("page2-$it") }, nextCursor = null))
            }
        }
        val viewModel = FeedViewModel(repository, FakeAuthRepository(), fakeBookmarkIdsStore(), FakeTopicPreferencesStore())
        val callsAfterInitial = fetchCalls

        viewModel.onPageChanged(3) // posts.size - 3 == index 2, so index 3 should trigger load-more (size=5)

        assertEquals(callsAfterInitial + 1, fetchCalls)
        assertEquals(10, viewModel.uiState.value.posts.size)
        assertNull(viewModel.uiState.value.nextCursor)
    }

    @Test
    fun onBookmarkTapped_loggedOut_setsPendingAction_withoutCallingRepository() = runTest {
        val posts = listOf(fakePost("1"))
        val repository = FakePostsRepository { _, _ -> Result.success(Page(posts, null)) }
        val viewModel = FeedViewModel(repository, FakeAuthRepository(initialSession = null), fakeBookmarkIdsStore(), FakeTopicPreferencesStore())

        viewModel.onBookmarkTapped("1")

        assertEquals(PendingBookmarkAction("1"), viewModel.uiState.value.pendingBookmarkAction)
        assertTrue(repository.setBookmarkedCalls.isEmpty())
    }

    @Test
    fun onLoginSucceeded_replaysPendingBookmarkAction_andClearsIt() = runTest {
        val posts = listOf(fakePost("1", isBookmarked = false))
        val repository = FakePostsRepository { _, _ -> Result.success(Page(posts, null)) }
        val viewModel = FeedViewModel(repository, FakeAuthRepository(initialSession = null), fakeBookmarkIdsStore(), FakeTopicPreferencesStore())
        viewModel.onBookmarkTapped("1")

        viewModel.onLoginSucceeded()

        assertNull(viewModel.uiState.value.pendingBookmarkAction)
        assertEquals(listOf("1" to true), repository.setBookmarkedCalls)
        assertTrue(viewModel.uiState.value.posts.first { it.id == "1" }.isBookmarked)
    }

    @Test
    fun onLoginDismissed_clearsPendingAction_withoutCallingRepository() = runTest {
        val posts = listOf(fakePost("1"))
        val repository = FakePostsRepository { _, _ -> Result.success(Page(posts, null)) }
        val viewModel = FeedViewModel(repository, FakeAuthRepository(initialSession = null), fakeBookmarkIdsStore(), FakeTopicPreferencesStore())
        viewModel.onBookmarkTapped("1")

        viewModel.onLoginDismissed()

        assertNull(viewModel.uiState.value.pendingBookmarkAction)
        assertTrue(repository.setBookmarkedCalls.isEmpty())
    }

    @Test
    fun onBookmarkTapped_loggedIn_togglesOptimistically_andRevertsOnFailure() = runTest {
        val posts = listOf(fakePost("1", isBookmarked = false))
        val repository = FakePostsRepository { _, _ -> Result.success(Page(posts, null)) }
        repository.setBookmarkedResult = Result.failure(RuntimeException("network error"))
        val viewModel = FeedViewModel(
            repository,
            FakeAuthRepository(initialSession = Session("t")),
            fakeBookmarkIdsStore(),
            FakeTopicPreferencesStore(),
        )

        viewModel.onBookmarkTapped("1")

        assertFalse(viewModel.uiState.value.posts.first { it.id == "1" }.isBookmarked)
        assertTrue(viewModel.uiState.value.pendingBookmarkToggles.isEmpty())
    }

    @Test
    fun `initial selected topic comes from stored interests, not Topic All`() = runTest {
        val repository = FakePostsRepository { _, _ -> Result.success(Page(emptyList(), null)) }
        val store = FakeTopicPreferencesStore().apply { setSelectedTopics(setOf(Topic.Kotlin, Topic.Android)) }
        val viewModel = FeedViewModel(repository, FakeAuthRepository(), fakeBookmarkIdsStore(), store)

        assertEquals(Topic.Kotlin, viewModel.uiState.value.selectedTopic)
        assertEquals(listOf(Topic.Kotlin, Topic.Android), viewModel.uiState.value.availableTopics)
    }

    @Test
    fun `editing interests away from the active filter falls back to a remaining topic`() = runTest {
        val repository = FakePostsRepository { _, _ -> Result.success(Page(emptyList(), null)) }
        val store = FakeTopicPreferencesStore().apply { setSelectedTopics(setOf(Topic.Android, Topic.Kotlin)) }
        val viewModel = FeedViewModel(repository, FakeAuthRepository(), fakeBookmarkIdsStore(), store)
        viewModel.onTopicSelected(Topic.Android) // user is filtered on Android
        store.setSelectedTopics(setOf(Topic.Kotlin)) // edits interests, drops Android

        viewModel.onTopicPreferencesChanged()

        assertEquals(Topic.Kotlin, viewModel.uiState.value.selectedTopic)
        assertEquals(listOf(Topic.Kotlin), viewModel.uiState.value.availableTopics)
    }

    @Test
    fun `onViewModeToggled flips between Stack and List`() = runTest {
        val repository = FakePostsRepository { _, _ -> Result.success(Page(emptyList(), null)) }
        val viewModel = FeedViewModel(repository, FakeAuthRepository(), fakeBookmarkIdsStore(), FakeTopicPreferencesStore())

        viewModel.onViewModeToggled()
        assertEquals(FeedViewMode.List, viewModel.uiState.value.viewMode)

        viewModel.onViewModeToggled()
        assertEquals(FeedViewMode.Stack, viewModel.uiState.value.viewMode)
    }
}
