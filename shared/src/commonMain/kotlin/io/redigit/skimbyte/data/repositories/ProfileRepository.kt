package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.models.Profile
import io.redigit.skimbyte.data.models.toDomain
import io.redigit.skimbyte.data.remote.AuthApi
import io.redigit.skimbyte.util.MAX_UPLOAD_IMAGE_BYTES
import io.redigit.skimbyte.util.PickedImage
import io.redigit.skimbyte.util.compressForUpload
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

interface ProfileRepository {
    val profile: StateFlow<Profile?>
    suspend fun uploadAvatar(image: PickedImage): Result<Unit>
    suspend fun uploadBanner(image: PickedImage): Result<Unit>
    suspend fun removeAvatar(): Result<Unit>
    suspend fun removeBanner(): Result<Unit>
    /** Re-fetches the profile from `/me`, e.g. on pull-to-refresh. */
    suspend fun refresh(): Result<Unit>
}

class ProfileRepositoryImpl(
    private val authApi: AuthApi,
    session: StateFlow<Session?>,
    scope: CoroutineScope,
) : ProfileRepository {
    private val _profile = MutableStateFlow<Profile?>(null)
    override val profile: StateFlow<Profile?> = _profile.asStateFlow()

    // The server reuses the same image URL after a re-upload, so without a changing query the UI
    // keeps the old string (no recomposition) and Coil serves the cached bytes. A per-image version
    // stamped on upload busts both. It's kept here (not just applied once) so a later /me refresh -
    // e.g. from a session change - re-stamps the URL rather than reverting to the stale cached one.
    private var avatarVersion: Long? = null
    private var bannerVersion: Long? = null

    init {
        scope.launch {
            session.collectLatest { current ->
                if (current == null) {
                    avatarVersion = null
                    bannerVersion = null
                    _profile.value = null
                } else {
                    // Rethrow cancellation rather than swallowing it: collectLatest cancels this
                    // block on the next session change, and a caught CancellationException must
                    // not be treated as an ordinary fetch failure.
                    _profile.value = runCatching { authApi.me().toDomain().withVersions() }
                        .onFailure { if (it is CancellationException) throw it }
                        .getOrNull()
                }
            }
        }
    }

    override suspend fun uploadAvatar(image: PickedImage): Result<Unit> = upload(TYPE_AVATAR, image)
    override suspend fun uploadBanner(image: PickedImage): Result<Unit> = upload(TYPE_BANNER, image)

    // Optimistic/local only: there is no delete endpoint yet, so this clears the URL in-memory and
    // does not persist across a reload. Point at DELETE /me/avatar|banner once it exists.
    override suspend fun removeAvatar(): Result<Unit> = runCatching {
        avatarVersion = null
        _profile.value = _profile.value?.copy(avatarUrl = null)
    }

    override suspend fun removeBanner(): Result<Unit> = runCatching {
        bannerVersion = null
        _profile.value = _profile.value?.copy(bannerUrl = null)
    }

    override suspend fun refresh(): Result<Unit> = runCatching {
        _profile.value = authApi.me().toDomain().withVersions()
    }

    private suspend fun upload(type: String, image: PickedImage): Result<Unit> = runCatching {
        // Crops come out as large PNGs; shrink to the upload cap (re-encoding as JPEG, downscaling
        // if needed) so avatars and banners stay small.
        val prepared = compressForUpload(image, MAX_UPLOAD_IMAGE_BYTES)
        // Safety net: if even full compression can't get under the cap (e.g. undecodable bytes),
        // fail rather than pushing a multi-MB upload the server would reject.
        if (prepared.bytes.size > MAX_UPLOAD_IMAGE_BYTES) {
            throw IllegalArgumentException("Image too large")
        }
        authApi.uploadImage(type, prepared)
        val version = kotlin.time.Clock.System.now().epochSeconds
        when (type) {
            TYPE_AVATAR -> avatarVersion = version
            TYPE_BANNER -> bannerVersion = version
        }
        _profile.value = runCatching { authApi.me().toDomain().withVersions() }.getOrNull() ?: _profile.value
    }

    private fun Profile.withVersions(): Profile =
        copy(avatarUrl = avatarUrl.withVersion(avatarVersion), bannerUrl = bannerUrl.withVersion(bannerVersion))

    private fun String?.withVersion(version: Long?): String? = when {
        this == null || version == null -> this
        contains('?') -> "$this&v=$version"
        else -> "$this?v=$version"
    }

    companion object {
        private const val TYPE_AVATAR = "avatar"
        private const val TYPE_BANNER = "banner"
    }
}
