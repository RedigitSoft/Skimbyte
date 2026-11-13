package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.remote.MeApi

/**
 * Deletes everything skimbyte-server stores for the current user (currently bookmarks) via
 * `DELETE /v1/me`. A data concern, kept out of [AuthRepository] — signing out afterward is the
 * caller's decision. The sign-in account itself is not touched; see PRIVACY.md.
 */
interface UserDataRepository {
    suspend fun deleteAll(): Result<Unit>
}

class UserDataRepositoryImpl(private val meApi: MeApi) : UserDataRepository {
    override suspend fun deleteAll(): Result<Unit> = runCatching { meApi.deleteUserData() }
}
