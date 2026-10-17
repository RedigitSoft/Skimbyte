package io.redigit.skimbyte.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.redigit.skimbyte.auth.SessionStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeSessionStorage(accessToken: String?, refreshToken: String?) : SessionStorage {
    private var access = accessToken
    private var refresh = refreshToken

    override fun accessToken(): String? = access
    override fun refreshToken(): String? = refresh
    override fun save(accessToken: String, refreshToken: String) {
        access = accessToken
        refresh = refreshToken
    }
    override fun clear() {
        access = null
        refresh = null
    }
}

class TokenRefreshTest {

    @Test
    fun on401_refreshIsInvokedOnce_andOriginalRequestRetriedWithNewToken() = runTest {
        val sessionStorage = FakeSessionStorage(accessToken = "expired-token", refreshToken = "refresh-token")
        var protectedCallCount = 0
        var refreshCallCount = 0

        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/auth/token/refresh" -> {
                    refreshCallCount++
                    respond(
                        content = """{"accessToken":"new-access","refreshToken":"new-refresh","expiresIn":3600}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
                "/v1/bookmarks" -> {
                    protectedCallCount++
                    val authHeader = request.headers[HttpHeaders.Authorization]
                    if (authHeader == "Bearer new-access") {
                        respond(
                            content = """{"items":[],"nextCursor":null}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    } else {
                        respond(content = "", status = HttpStatusCode.Unauthorized)
                    }
                }
                else -> error("unexpected request: ${request.url}")
            }
        }

        val client = createHttpClient(sessionStorage, mockEngine)
        val response = client.get("https://example.invalid/v1/bookmarks")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, refreshCallCount)
        assertEquals(2, protectedCallCount)
        assertEquals("new-access", sessionStorage.accessToken())
    }
}
