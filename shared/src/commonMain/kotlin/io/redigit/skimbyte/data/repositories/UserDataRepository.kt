package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.remote.MeApi

/**
 * Deletes everything stored server-side for the current user in this app via `DELETE /v1/me` —
 * server removes the bookmarks it holds and cascades to the auth server, which wipes
 * the app-scoped profile data (uploaded images).
 * See PRIVACY.md.
 */
interface UserDataRepository {
    suspend fun deleteAll(): Result<Unit>
}

class UserDataRepositoryImpl(private val meApi: MeApi) : UserDataRepository {
    override suspend fun deleteAll(): Result<Unit> = runCatching { meApi.deleteUserData() }
}
