package io.redigit.skimbyte.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete

/**
 * The skimbyte-server routes about the current user (as opposed to [AuthApi], which talks to
 * the auth server at [ApiConfig.AUTH_BASE_URL]).
 */
interface MeApi {
    /**
     * `DELETE /v1/me` — permanently deletes everything skimbyte-server stores for the caller
     * (currently bookmarks). The sign-in account itself is untouched; see PRIVACY.md.
     */
    suspend fun deleteUserData()
}

/** Uses the main (demo-aware, bearer-attaching) [HttpClient] — the same one the posts API uses. */
class KtorMeApi(private val client: HttpClient) : MeApi {
    override suspend fun deleteUserData() {
        client.delete("${ApiConfig.BASE_URL}/v1/me")
    }
}
