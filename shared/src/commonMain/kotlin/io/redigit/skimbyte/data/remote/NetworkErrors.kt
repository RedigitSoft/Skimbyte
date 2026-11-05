package io.redigit.skimbyte.data.remote

import io.ktor.client.plugins.ResponseException

/**
 * Whether [this] failure means the request never reached the server, as opposed to the server
 * responding with an error status.
 *
 * The [HttpClient] runs with `expectSuccess = true` (see `SkimbyteHttpClient`), so a non-2xx
 * response surfaces as a [ResponseException] — the request *did* complete, the server is simply
 * unhappy — which is a server-side problem, not a connectivity one. Everything else a request can
 * throw (no connectivity, DNS failure, timeout) means we never got a response, so the user may be
 * able to fix it by checking their connection.
 */
fun Throwable.isNetworkError(): Boolean = this !is ResponseException
