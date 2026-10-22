package io.redigit.skimbyte.auth

import io.redigit.skimbyte.data.models.Profile
import io.redigit.skimbyte.data.models.toDomain
import io.redigit.skimbyte.util.PickedImage
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
}

class ProfileRepositoryImpl(
    private val authApi: AuthApi,
    session: StateFlow<Session?>,
    scope: CoroutineScope,
) : ProfileRepository {
    private val _profile = MutableStateFlow<Profile?>(null)
    override val profile: StateFlow<Profile?> = _profile.asStateFlow()

    init {
        scope.launch {
            session.collectLatest { current ->
                if (current == null) {
                    _profile.value = null
                } else {
                    // Rethrow cancellation rather than swallowing it: collectLatest cancels this
                    // block on the next session change, and a caught CancellationException must
                    // not be treated as an ordinary fetch failure.
                    _profile.value = runCatching { authApi.me().toDomain() }
                        .onFailure { if (it is CancellationException) throw it }
                        .getOrNull()
                }
            }
        }
    }

    override suspend fun uploadAvatar(image: PickedImage): Result<Unit> = upload("avatar", image)
    override suspend fun uploadBanner(image: PickedImage): Result<Unit> = upload("banner", image)

    private suspend fun upload(type: String, image: PickedImage): Result<Unit> {
        if (image.bytes.size > MAX_IMAGE_BYTES) return Result.failure(IllegalArgumentException("Image too large"))
        return runCatching {
            authApi.uploadImage(type, image)
            _profile.value = runCatching { authApi.me().toDomain() }.getOrNull() ?: _profile.value
        }
    }

    companion object {
        const val MAX_IMAGE_BYTES = 3 * 1024 * 1024
    }
}
