package io.redigit.skimbyte.util

import java.net.HttpURLConnection
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoopbackAuthCallbackServerTest {

    @Test
    fun callbackRequest_forwardsFullUriAndRespondsWithClosePage() {
        var received: String? = null
        // Port 0 = ephemeral, so the test never collides with the fixed production port.
        val server = LoopbackAuthCallbackServer.start(0) { received = it }
        try {
            val port = server.address.port
            val connection = URI("http://127.0.0.1:$port/auth/callback?access_token=at-1&refresh_token=rt-1")
                .toURL().openConnection() as HttpURLConnection
            val body = connection.inputStream.use { it.readBytes().decodeToString() }

            assertEquals(200, connection.responseCode)
            assertTrue(body.contains("You're signed in"))
            assertEquals(
                "http://127.0.0.1:$port/auth/callback?access_token=at-1&refresh_token=rt-1",
                received,
            )
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun unrelatedPath_isNotForwarded() {
        var received: String? = null
        val server = LoopbackAuthCallbackServer.start(0) { received = it }
        try {
            val port = server.address.port
            val connection = URI("http://127.0.0.1:$port/favicon.ico")
                .toURL().openConnection() as HttpURLConnection
            // JDK HttpServer answers 404 for paths with no registered context.
            assertEquals(404, connection.responseCode)
            assertEquals(null, received)
        } finally {
            server.stop(0)
        }
    }
}
