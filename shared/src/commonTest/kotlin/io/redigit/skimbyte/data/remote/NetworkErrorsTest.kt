package io.redigit.skimbyte.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NetworkErrorsTest {

    @Test
    fun serverErrorStatus_isNotTreatedAsANetworkError() = runTest {
        // expectSuccess = true turns a 5xx into a ResponseException — the server did respond.
        val client = HttpClient(MockEngine { respondError(HttpStatusCode.InternalServerError) }) {
            expectSuccess = true
        }
        val thrown = runCatching { client.get("https://example.test/v1/posts") }.exceptionOrNull()
        assertNotNull(thrown)
        assertFalse(thrown.isNetworkError())
    }

    @Test
    fun requestThatNeverReachesTheServer_isANetworkError() = runTest {
        // The engine throwing stands in for no connectivity / DNS failure / timeout: no response.
        val client = HttpClient(MockEngine { throw RuntimeException("connection refused") })
        val thrown = runCatching { client.get("https://example.test/v1/posts") }.exceptionOrNull()
        assertNotNull(thrown)
        assertTrue(thrown.isNetworkError())
    }
}
