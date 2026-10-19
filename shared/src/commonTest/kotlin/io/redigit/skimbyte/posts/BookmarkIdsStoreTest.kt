package io.redigit.skimbyte.posts

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class BookmarkIdsStoreTest {

    private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient =
        HttpClient(MockEngine) {
            expectSuccess = true
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            engine { addHandler(handler) }
        }

    private fun MockRequestHandleScope.respondJson(body: String) = respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    private fun postJson(id: String) = """
        {
          "id": "$id",
          "topic": "Kotlin",
          "title": "Kotlin 2.4.21",
          "summaryMarkdown": "Short summary",
          "sourceName": "Kotlin Releases",
          "sourceUrl": "https://example.com/$id",
          "imageUrl": null,
          "digestDate": "2026-10-13",
          "publishedAt": "2026-10-12T18:00:00Z"
        }
    """.trimIndent()

    @Test
    fun refresh_populatesIds_onSuccess() = runTest {
        val httpClient = client { respondJson("""{"items":[${postJson("p1")},${postJson("p2")}],"nextCursor":null}""") }
        val store = BookmarkIdsStore(KtorPostsApi(httpClient))

        store.refresh()

        assertEquals(setOf("p1", "p2"), store.ids.value)
    }

    @Test
    fun refresh_leavesIdsUnchanged_onRequestFailure() = runTest {
        val httpClient = client { respond(content = "", status = HttpStatusCode.Unauthorized) }
        val store = BookmarkIdsStore(KtorPostsApi(httpClient))

        store.refresh()

        assertEquals(emptySet(), store.ids.value)
    }
}
