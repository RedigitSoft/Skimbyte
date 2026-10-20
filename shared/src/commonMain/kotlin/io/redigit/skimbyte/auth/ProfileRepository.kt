package io.redigit.skimbyte.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

interface ProfileRepository {
    val profile: StateFlow<Profile?>
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
}
