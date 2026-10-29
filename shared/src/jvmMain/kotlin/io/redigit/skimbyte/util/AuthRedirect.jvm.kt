package io.redigit.skimbyte.util

import com.sun.net.httpserver.HttpServer
import io.redigit.skimbyte.data.remote.GoogleAuthCallback
import java.net.InetSocketAddress

// Fixed (not ephemeral) port because the auth server validates redirect URIs against an
// exact-match allowlist — this exact URL must be listed in the auth server's
// auth.oauth.allowedMobileRedirectUris. High/uncommon to make collisions unlikely.
private const val LOOPBACK_PORT = 48217
private const val CALLBACK_PATH = "/auth/callback"

/**
 * RFC 8252 §7.3 loopback redirect for desktop: a JVM process can't register a custom URL scheme
 * with the OS (the system browser would dead-end on `skimbyte://…` with "address is invalid"),
 * so the browser is redirected to this localhost listener instead, which forwards the full
 * callback URL — token query params included — into the shared [GoogleAuthCallback] bridge.
 */
actual fun authRedirectUri(): String = LoopbackAuthCallbackServer.ensureStarted()

internal object LoopbackAuthCallbackServer {
    private var server: HttpServer? = null

    /** Starts the listener on first call (idempotent); returns the redirect URI to advertise. */
    @Synchronized
    fun ensureStarted(): String {
        if (server == null) {
            server = start(LOOPBACK_PORT, GoogleAuthCallback::onRedirect)
        }
        return "http://127.0.0.1:$LOOPBACK_PORT$CALLBACK_PATH"
    }

    /** Separated from the singleton state so tests can run on an ephemeral port. */
    fun start(port: Int, onRedirect: (String) -> Unit): HttpServer {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0)
        server.createContext(CALLBACK_PATH) { exchange ->
            onRedirect("http://127.0.0.1:${server.address.port}${exchange.requestURI}")
            val body = RESPONSE_PAGE.encodeToByteArray()
            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        // Daemon-style single thread: must not keep the JVM alive after the last window closes.
        server.start()
        return server
    }

    private val RESPONSE_PAGE = """
        <!DOCTYPE html>
        <html lang="en"><head><meta charset="utf-8"><title>Skimbyte</title></head>
        <body style="font-family: system-ui, sans-serif; display: grid; place-items: center; height: 100vh; margin: 0; background: #181715; color: #FAF9F5;">
        <div style="text-align: center;">
          <h1 style="font-weight: 600;">You're signed in</h1>
          <p>You can close this tab and return to Skimbyte.</p>
        </div>
        </body></html>
    """.trimIndent()
}
