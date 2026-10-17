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
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PostsRepositoryImplTest {

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
    fun fetchPosts_sendsTopicApiValue_forNonAllTopic() = runTest {
        var capturedUrl: Url? = null
        val httpClient = client { request ->
            capturedUrl = request.url
            respondJson("""{"items":[],"nextCursor":null}""")
        }
        val repository = PostsRepositoryImpl(KtorPostsApi(httpClient), BookmarkIdsStore(KtorPostsApi(httpClient)))

        repository.fetchPosts(Topic.Kotlin, cursor = null)

        assertEquals("Kotlin", capturedUrl?.parameters?.get("topic"))
    }

    @Test
    fun fetchPosts_all_omitsTopicParam() = runTest {
        var capturedUrl: Url? = null
        val httpClient = client { request ->
            capturedUrl = request.url
            respondJson("""{"items":[],"nextCursor":null}""")
        }
        val repository = PostsRepositoryImpl(KtorPostsApi(httpClient), BookmarkIdsStore(KtorPostsApi(httpClient)))

        repository.fetchPosts(Topic.All, cursor = null)

        assertNull(capturedUrl?.parameters?.get("topic"))
    }

    @Test
    fun fetchPosts_passesThroughCursor_andParsesNextCursor() = runTest {
        var capturedUrl: Url? = null
        val httpClient = client { request ->
            capturedUrl = request.url
            respondJson("""{"items":[${postJson("p1")}],"nextCursor":"abc123"}""")
        }
        val repository = PostsRepositoryImpl(KtorPostsApi(httpClient), BookmarkIdsStore(KtorPostsApi(httpClient)))

        val result = repository.fetchPosts(Topic.All, cursor = "page1cursor")

        assertEquals("page1cursor", capturedUrl?.parameters?.get("cursor"))
        assertEquals("abc123", result.getOrThrow().nextCursor)
        assertEquals("p1", result.getOrThrow().items.single().id)
    }

    @Test
    fun fetchPosts_marksBookmarked_fromBookmarkIdsStoreIds() = runTest {
        val httpClient = client { respondJson("""{"items":[${postJson("bookmarked-post")},${postJson("other-post")}],"nextCursor":null}""") }
        val api = KtorPostsApi(httpClient)
        val bookmarkIdsStore = BookmarkIdsStore(api)
        bookmarkIdsStore.markBookmarked("bookmarked-post")
        val repository = PostsRepositoryImpl(api, bookmarkIdsStore)

        val posts = repository.fetchPosts(Topic.All, cursor = null).getOrThrow().items

        assertTrue(posts.first { it.id == "bookmarked-post" }.isBookmarked)
        assertFalse(posts.first { it.id == "other-post" }.isBookmarked)
    }

    @Test
    fun fetchPosts_serverError_returnsFailureNotException() = runTest {
        val httpClient = client { respond(content = "boom", status = HttpStatusCode.InternalServerError) }
        val api = KtorPostsApi(httpClient)
        val repository = PostsRepositoryImpl(api, BookmarkIdsStore(api))

        val result = repository.fetchPosts(Topic.All, cursor = null)

        assertTrue(result.isFailure)
    }

    @Test
    fun setBookmarked_true_marksIdLocally_beforeNetworkResolves() = runTest {
        val httpClient = client { respond(content = "", status = HttpStatusCode.NoContent) }
        val api = KtorPostsApi(httpClient)
        val bookmarkIdsStore = BookmarkIdsStore(api)
        val repository = PostsRepositoryImpl(api, bookmarkIdsStore)

        val result = repository.setBookmarked("post-1", bookmarked = true)

        assertTrue(result.isSuccess)
        assertTrue("post-1" in bookmarkIdsStore.ids.value)
    }

    @Test
    fun setBookmarked_true_revertsLocalState_onNetworkFailure() = runTest {
        val httpClient = client { respond(content = "boom", status = HttpStatusCode.InternalServerError) }
        val api = KtorPostsApi(httpClient)
        val bookmarkIdsStore = BookmarkIdsStore(api)
        val repository = PostsRepositoryImpl(api, bookmarkIdsStore)

        val result = repository.setBookmarked("post-1", bookmarked = true)

        assertTrue(result.isFailure)
        assertFalse("post-1" in bookmarkIdsStore.ids.value)
    }
}
