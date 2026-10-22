package io.redigit.skimbyte.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.redigit.skimbyte.testing.FakeSessionStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Models the real auth-server: a refresh token is single-use, atomically claimed (mirrors
 * `SET NX` in RefreshTokenService.rotate). Presenting an already-used one means the server
 * assumes theft and tears down the whole session (RotationResult.Reuse in auth-server).
 * The Mutex below guarantees that atomicity regardless of how MockEngine schedules the two
 * concurrent requests — exactly like the real Redis-backed server would.
 */
private class SingleUseFakeAuthServer(initialRefreshToken: String) {
    private val lock = Mutex()
    private var validRefreshToken: String? = initialRefreshToken
    private var currentAccessToken = "access-0"
    private var rotation = 0
    private var sessionRevoked = false

    fun engine() = MockEngine { request ->
        when (request.url.encodedPath) {
            "/auth/token/refresh" -> handleRefresh((request.body as TextContent).text)
            "/v1/protected" -> {
                val authHeader = request.headers[HttpHeaders.Authorization]
                if (!sessionRevoked && authHeader == "Bearer $currentAccessToken") {
                    respond(content = "ok", status = HttpStatusCode.OK)
                } else {
                    respond(content = "", status = HttpStatusCode.Unauthorized)
                }
            }
            else -> error("unexpected request: ${request.url}")
        }
    }

    private suspend fun MockRequestHandleScope.handleRefresh(body: String): HttpResponseData = lock.withLock {
        val presented = Regex(""""refreshToken":"([^"]+)"""").find(body)!!.groupValues[1]
        if (sessionRevoked || presented != validRefreshToken) {
            sessionRevoked = true
            validRefreshToken = null
            return@withLock respond(
                content = """{"error":"token_reuse","message":"Refresh token reuse detected; session revoked"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        rotation++
        currentAccessToken = "access-$rotation"
        validRefreshToken = "refresh-$rotation"
        respond(
            content = """{"accessToken":"$currentAccessToken","refreshToken":"$validRefreshToken","expiresIn":3600}""",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    }
}

class ConcurrentClientRefreshTest {

    @Test
    fun twoHttpClientsSharingSessionStorage_bothSucceedOnConcurrentRefresh() = runTest {
        val sessionStorage = FakeSessionStorage(accessToken = "expired-access", refreshToken = "refresh-0")
        val server = SingleUseFakeAuthServer(initialRefreshToken = "refresh-0")
        val refreshCoordinator = RefreshCoordinator(sessionStorage)

        // Mirrors the app's real topology: the general HttpClient (feed/posts) and the
        // dedicated auth HttpClient (AuthModule's authHttpClientQualifier) are two separate
        // instances pointed at the same session storage and the same backing server, sharing
        // one Koin-provided RefreshCoordinator.
        val generalClient = createHttpClient(sessionStorage, refreshCoordinator, server.engine())
        val authClient = createHttpClient(sessionStorage, refreshCoordinator, server.engine())

        val generalCall = async { generalClient.get("https://example.invalid/v1/protected") }
        val authCall = async { authClient.get("https://example.invalid/v1/protected") }

        assertEquals(HttpStatusCode.OK, generalCall.await().status)
        assertEquals(HttpStatusCode.OK, authCall.await().status)
    }
}
