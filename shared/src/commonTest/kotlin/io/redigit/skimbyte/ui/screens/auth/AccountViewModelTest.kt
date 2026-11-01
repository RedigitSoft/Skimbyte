package io.redigit.skimbyte.ui.screens.auth

import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.models.Profile
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.repositories.ProfileRepository
import io.redigit.skimbyte.data.repositories.Session
import io.redigit.skimbyte.data.repositories.UploadStage
import io.redigit.skimbyte.data.repositories.UserDataRepository
import io.redigit.skimbyte.ui.components.ImageUploadState
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
    private val _session = MutableStateFlow(if (loggedIn) Session("token") else null)
    override val session: StateFlow<Session?> = _session.asStateFlow()
    override suspend fun login(email: String, password: String) = error("unused")
    override suspend fun register(email: String, password: String) = error("unused")
    override suspend fun completeGoogleSignIn(redirectUri: String) = error("unused")
    override fun logout() {
        _session.value = null
    }
}

private class FakeUserDataRepository : UserDataRepository {
    var result: Result<Unit> = Result.success(Unit)
    var gate: CompletableDeferred<Unit>? = null
    var callCount = 0
    override suspend fun deleteAll(): Result<Unit> {
        callCount++
        gate?.await()
        return result
    }
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
    var refreshCallCount = 0
    override suspend fun uploadAvatar(image: PickedImage, onStage: (UploadStage) -> Unit): Result<Unit> {
        uploadAvatarCallCount++
        onStage(UploadStage.Compressing)
        avatarUploadGate?.await()
        onStage(UploadStage.Uploading)
        return avatarResult
    }
    override suspend fun uploadBanner(image: PickedImage, onStage: (UploadStage) -> Unit): Result<Unit> {
        uploadBannerCallCount++
        onStage(UploadStage.Compressing)
        onStage(UploadStage.Uploading)
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
    override suspend fun refresh(): Result<Unit> {
        refreshCallCount++
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
        userData: UserDataRepository = FakeUserDataRepository(),
    ) = AccountViewModel(
        auth,
        profile,
        MutableStateFlow(followedTopics),
        MutableStateFlow(bookmarkIds),
        userData,
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
    fun onAvatarPicked_stagesPendingCropWithoutUploading() = runTest {
        val repo = FakeProfileRepository()
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png")))

        assertEquals(ImageTarget.Avatar, vm.state.value.pendingCrop?.target)
        assertEquals(0, repo.uploadAvatarCallCount) // upload is deferred until the crop is confirmed
    }

    @Test
    fun onCropCancelled_clearsPendingCropWithoutUploading() = runTest {
        val repo = FakeProfileRepository()
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png")))
        vm.onIntent(AccountIntent.CropCancelled)

        assertNull(vm.state.value.pendingCrop)
        assertEquals(0, repo.uploadAvatarCallCount)
    }

    @Test
    fun onCropConfirmed_avatar_success_clearsInProgressAndError() = runTest {
        val repo = FakeProfileRepository()
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png")))
        vm.onIntent(AccountIntent.CropConfirmed(PickedImage(ByteArray(8), "image/png")))

        assertNull(vm.state.value.pendingCrop)
        assertEquals(ImageUploadState.Idle, vm.state.value.avatarUpload)
        assertNull(vm.state.value.avatarError)
        assertEquals(1, repo.uploadAvatarCallCount)
    }

    @Test
    fun onCropConfirmed_avatar_failure_setsErrorAndClearsInProgress() = runTest {
        val repo = FakeProfileRepository()
        repo.avatarResult = Result.failure(RuntimeException("boom"))
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png")))
        vm.onIntent(AccountIntent.CropConfirmed(PickedImage(ByteArray(8), "image/png")))

        assertEquals(ImageUploadState.Error, vm.state.value.avatarUpload)
        assertEquals("Could not update avatar", vm.state.value.avatarError)
    }

    @Test
    fun onCropConfirmed_whileAlreadyUploading_isIgnored() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeProfileRepository()
        repo.avatarUploadGate = gate
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png")))
        vm.onIntent(AccountIntent.CropConfirmed(PickedImage(ByteArray(8), "image/png"))) // suspends on the gate
        vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png")))
        vm.onIntent(AccountIntent.CropConfirmed(PickedImage(ByteArray(8), "image/png"))) // ignored: still in progress
        gate.complete(Unit)

        assertEquals(1, repo.uploadAvatarCallCount)
    }

    @Test
    fun onCropConfirmed_avatar_surfacesInProgressStateWhileUploading() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeProfileRepository()
        repo.avatarUploadGate = gate
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png")))
        vm.onIntent(AccountIntent.CropConfirmed(PickedImage(ByteArray(8), "image/png")))

        // The fake reports Compressing then blocks on the gate before it would report Uploading,
        // so the screen shows an in-flight (busy) state rather than Idle.
        assertEquals(ImageUploadState.Compressing, vm.state.value.avatarUpload)

        gate.complete(Unit)
    }

    @Test
    fun onRefresh_delegatesToProfileRepository() = runTest {
        val repo = FakeProfileRepository()
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.Refresh)

        assertEquals(1, repo.refreshCallCount)
        assertEquals(false, vm.state.value.isRefreshing)
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
    fun onDeleteData_success_deletesDataThenSignsOut() = runTest {
        val userData = FakeUserDataRepository()
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), FakeProfileRepository(), userData = userData)

        vm.onIntent(AccountIntent.DeleteDataConfirmed)

        assertEquals(1, userData.callCount)
        assertEquals(false, vm.state.value.deleteDataInProgress)
        assertNull(vm.state.value.deleteDataError)
        assertEquals(false, vm.state.value.isLoggedIn) // logout() flipped the session to signed out
    }

    @Test
    fun onDeleteData_failure_setsErrorAndKeepsSession() = runTest {
        val userData = FakeUserDataRepository()
        userData.result = Result.failure(RuntimeException("boom"))
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), FakeProfileRepository(), userData = userData)

        vm.onIntent(AccountIntent.DeleteDataConfirmed)

        assertEquals("Could not delete your data", vm.state.value.deleteDataError)
        assertEquals(false, vm.state.value.deleteDataInProgress)
        assertEquals(true, vm.state.value.isLoggedIn) // delete failed, so no sign out — user can retry
    }

    @Test
    fun onDeleteData_whileAlreadyDeleting_isIgnored() = runTest {
        val gate = CompletableDeferred<Unit>()
        val userData = FakeUserDataRepository()
        userData.gate = gate
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), FakeProfileRepository(), userData = userData)

        vm.onIntent(AccountIntent.DeleteDataConfirmed) // suspends on the gate
        vm.onIntent(AccountIntent.DeleteDataConfirmed) // ignored: still in progress
        gate.complete(Unit)

        assertEquals(1, userData.callCount)
    }

    @Test
    fun onCropConfirmed_banner_success_clearsInProgressAndError() = runTest {
        val repo = FakeProfileRepository()
        val vm = accountVm(FakeAuthRepositoryForAccount(loggedIn = true), repo)

        vm.onIntent(AccountIntent.BannerPicked(PickedImage(ByteArray(10), "image/png")))
        assertEquals(ImageTarget.Banner, vm.state.value.pendingCrop?.target)
        vm.onIntent(AccountIntent.CropConfirmed(PickedImage(ByteArray(8), "image/png")))

        assertNull(vm.state.value.pendingCrop)
        assertEquals(ImageUploadState.Idle, vm.state.value.bannerUpload)
        assertNull(vm.state.value.bannerError)
        assertEquals(1, repo.uploadBannerCallCount)
    }
}
