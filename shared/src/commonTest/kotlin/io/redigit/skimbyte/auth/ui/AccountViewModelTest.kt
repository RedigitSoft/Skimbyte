package io.redigit.skimbyte.auth.ui

import io.redigit.skimbyte.auth.AuthRepository
import io.redigit.skimbyte.data.models.Profile
import io.redigit.skimbyte.auth.ProfileRepository
import io.redigit.skimbyte.auth.Session
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
    override suspend fun uploadAvatar(image: PickedImage): Result<Unit> {
        uploadAvatarCallCount++
        avatarUploadGate?.await()
        return avatarResult
    }
    override suspend fun uploadBanner(image: PickedImage): Result<Unit> {
        uploadBannerCallCount++
        return bannerResult
    }
}

class AccountViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setupMainDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDownMainDispatcher() = Dispatchers.resetMain()

    @Test
    fun initialState_reflectsSessionAndProfile() = runTest {
        val repo = FakeProfileRepository(Profile("u1", avatarUrl = "https://cdn/a.jpg", bannerUrl = null))
        val vm = AccountViewModel(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        assertEquals(AccountUiState(isLoggedIn = true, avatarUrl = "https://cdn/a.jpg", bannerUrl = null), vm.uiState.value)
    }

    @Test
    fun onAvatarPicked_success_clearsInProgressAndError() = runTest {
        val repo = FakeProfileRepository()
        val vm = AccountViewModel(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onAvatarPicked(PickedImage(ByteArray(10), "image/png"))

        assertEquals(false, vm.uiState.value.avatarUploadInProgress)
        assertNull(vm.uiState.value.avatarError)
        assertEquals(1, repo.uploadAvatarCallCount)
    }

    @Test
    fun onAvatarPicked_failure_setsErrorAndClearsInProgress() = runTest {
        val repo = FakeProfileRepository()
        repo.avatarResult = Result.failure(RuntimeException("boom"))
        val vm = AccountViewModel(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onAvatarPicked(PickedImage(ByteArray(10), "image/png"))

        assertEquals(false, vm.uiState.value.avatarUploadInProgress)
        assertEquals("Could not update avatar", vm.uiState.value.avatarError)
    }

    @Test
    fun onAvatarPicked_whileAlreadyUploading_isIgnored() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeProfileRepository()
        repo.avatarUploadGate = gate
        val vm = AccountViewModel(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onAvatarPicked(PickedImage(ByteArray(10), "image/png")) // suspends on the gate
        vm.onAvatarPicked(PickedImage(ByteArray(10), "image/png")) // should be ignored: still in progress
        gate.complete(Unit)

        assertEquals(1, repo.uploadAvatarCallCount)
    }

    @Test
    fun onBannerPicked_success_clearsInProgressAndError() = runTest {
        val repo = FakeProfileRepository()
        val vm = AccountViewModel(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onBannerPicked(PickedImage(ByteArray(10), "image/png"))

        assertEquals(false, vm.uiState.value.bannerUploadInProgress)
        assertNull(vm.uiState.value.bannerError)
        assertEquals(1, repo.uploadBannerCallCount)
    }
}
