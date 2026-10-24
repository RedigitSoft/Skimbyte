package io.redigit.skimbyte.ui.screens.auth

import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.models.Profile
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.repositories.ProfileRepository
import io.redigit.skimbyte.data.repositories.Session
import io.redigit.skimbyte.util.PickedImage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeAuthRepositoryForAccount(loggedIn: Boolean) : AuthRepository {
    override val session: StateFlow<Session?> = MutableStateFlow(if (loggedIn) Session("token") else null).asStateFlow()
    override suspend fun login(email: String, password: String) = error("unused")
    override suspend fun register(email: String, password: String) = error("unused")
    override suspend fun completeGoogleSignIn(redirectUri: String) = error("unused")
    override fun logout() = error("unused")
}

private class FakeProfileRepository(initial: Profile? = null) : ProfileRepository {
    private val _profile = MutableStateFlow(initial)
    override val profile: StateFlow<Profile?> = _profile.asStateFlow()
    var avatarResult: Result<Unit> = Result.success(Unit)
    var bannerResult: Result<Unit> = Result.success(Unit)
    var avatarUploadGate: CompletableDeferred<Unit>? = null
    var uploadAvatarCallCount = 0
    var uploadBannerCallCount = 0
    var removeAvatarCallCount = 0
    var removeBannerCallCount = 0
    override suspend fun uploadAvatar(image: PickedImage): Result<Unit> {
        uploadAvatarCallCount++
        avatarUploadGate?.await()
        return avatarResult
    }
    override suspend fun uploadBanner(image: PickedImage): Result<Unit> {
        uploadBannerCallCount++
        return bannerResult
    }
    override suspend fun removeAvatar(): Result<Unit> {
        removeAvatarCallCount++
        _profile.value = _profile.value?.copy(avatarUrl = null)
        return Result.success(Unit)
    }
    override suspend fun removeBanner(): Result<Unit> {
        removeBannerCallCount++
        _profile.value = _profile.value?.copy(bannerUrl = null)
        return Result.success(Unit)
    }
}

class AccountViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setupMainDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDownMainDispatcher() = Dispatchers.resetMain()

    private fun accountVm(
        auth: AuthRepository,
        profile: ProfileRepository,
        followedTopics: Set<Topic> = emptySet(),
        bookmarkIds: Set<String> = emptySet(),
    ) = AccountViewModel(
        auth,
        profile,
        MutableStateFlow(followedTopics),
        MutableStateFlow(bookmarkIds),
    )

    @Test
    fun initialState_reflectsSessionAndProfile() = runTest {
        val repo = FakeProfileRepository(Profile("u1", avatarUrl = "https://cdn/a.jpg", bannerUrl = null))
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        val state = vm.state.value
        assertEquals(true, state.isLoggedIn)
        assertEquals("https://cdn/a.jpg", state.avatarUrl)
        assertEquals("@u1", state.handle) // derived from userId when the server doesn't supply one
    }

    @Test
    fun loggedInState_derivesSavedCountAndFollowedTopicsFromRealData() = runTest {
        val repo = FakeProfileRepository(Profile("user1234567", avatarUrl = null, bannerUrl = null))
        val vm = accountVm(
            FakeAuthRepositoryForAccount(loggedIn = true),
            repo,
            followedTopics = setOf(Topic.Kotlin, Topic.Android),
            bookmarkIds = setOf("a", "b", "c"),
        )

        val state = vm.state.value
        assertEquals(3, state.savedCount)
        assertEquals(setOf(Topic.Kotlin, Topic.Android), state.followedTopics.toSet())
        assertEquals("@user1234", state.handle) // first 8 chars of the userId
    }

    @Test
    fun loggedOutState_clearsDerivedProfileFields() = runTest {
        val repo = FakeProfileRepository()
        val vm = accountVm(
            FakeAuthRepositoryForAccount(loggedIn = false),
            repo,
            followedTopics = setOf(Topic.Kotlin),
            bookmarkIds = setOf("a"),
        )

        val state = vm.state.value
        assertEquals(false, state.isLoggedIn)
        assertNull(state.displayName)
        assertNull(state.savedCount)
        assertEquals(emptyList(), state.followedTopics)
    }

    @Test
    fun onAvatarPicked_success_clearsInProgressAndError() = runTest {
        val repo = FakeProfileRepository()
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png")))

        assertEquals(false, vm.state.value.avatarUploadInProgress)
        assertNull(vm.state.value.avatarError)
        assertEquals(1, repo.uploadAvatarCallCount)
    }

    @Test
    fun onAvatarPicked_failure_setsErrorAndClearsInProgress() = runTest {
        val repo = FakeProfileRepository()
        repo.avatarResult = Result.failure(RuntimeException("boom"))
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png")))

        assertEquals(false, vm.state.value.avatarUploadInProgress)
        assertEquals("Could not update avatar", vm.state.value.avatarError)
    }

    @Test
    fun onAvatarPicked_whileAlreadyUploading_isIgnored() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeProfileRepository()
        repo.avatarUploadGate = gate
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png"))) // suspends on the gate
        vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png"))) // should be ignored: still in progress
        gate.complete(Unit)

        assertEquals(1, repo.uploadAvatarCallCount)
    }

    @Test
    fun onAvatarRemoved_clearsAvatarUrl() = runTest {
        val repo = FakeProfileRepository(Profile("u1", avatarUrl = "https://cdn/a.jpg", bannerUrl = null))
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.AvatarRemoved)

        assertEquals(1, repo.removeAvatarCallCount)
        assertNull(vm.state.value.avatarUrl)
    }

    @Test
    fun onBannerPicked_success_clearsInProgressAndError() = runTest {
        val repo = FakeProfileRepository()
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.BannerPicked(PickedImage(ByteArray(10), "image/png")))

        assertEquals(false, vm.state.value.bannerUploadInProgress)
        assertNull(vm.state.value.bannerError)
        assertEquals(1, repo.uploadBannerCallCount)
    }
}
