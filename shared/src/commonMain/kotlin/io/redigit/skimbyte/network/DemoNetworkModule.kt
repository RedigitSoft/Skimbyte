package io.redigit.skimbyte.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.redigit.skimbyte.network.dto.PageDto
import io.redigit.skimbyte.posts.dto.PostDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * Stands in for a live API (none exists yet — see ApiConfig.DEMO_MODE) so the app is runnable
 * and demo-able end to end. Swapped out for `networkModule` with a single flag flip once a live
 * API is confirmed reachable; nothing above the HttpClient needs to change.
 */
val demoNetworkModule = module {
    single<HttpClient> { createHttpClient(get(), demoMockEngine()) }
}

private const val DEMO_EMAIL = "demo@skimbyte.app"
private const val DEMO_PASSWORD = "demo"
private const val PAGE_SIZE = 8

private val demoJson = Json { ignoreUnknownKeys = true }

private fun demoPost(id: String, topic: String, title: String, summary: String, source: String, hoursAgo: Int) = PostDto(
    id = id,
    topic = topic,
    title = title,
    summaryMarkdown = summary,
    sourceName = source,
    sourceUrl = "https://example.com/posts/$id",
    imageUrl = null,
    digestDate = "2026-10-13",
    publishedAt = Instant.parse("2026-10-13T12:00:00Z") - hoursAgo.hours,
)

private val demoPosts: List<PostDto> = buildList {
    val topics = listOf(
        "Kotlin" to "Kotlin Blog",
        "Android" to "Android Developers Blog",
        "KMP / Compose Multiplatform" to "Compose Multiplatform Releases",
        "Tooling & Products" to "JetBrains Blog (all)",
    )
    topics.forEachIndexed { topicIndex, (topic, source) ->
        repeat(6) { i ->
            add(
                demoPost(
                    id = "demo-${topicIndex}-$i",
                    topic = topic,
                    title = "$topic update #${i + 1}",
                    summary = "Demo summary for a **$topic** post, standing in until a live API is available.",
                    source = source,
                    hoursAgo = topicIndex * 6 + i,
                ),
            )
        }
    }
}.sortedByDescending { it.publishedAt }

private fun demoMockEngine(): MockEngine {
    val bookmarked = mutableSetOf<String>()
    return MockEngine { request ->
        when (request.url.encodedPath) {
            "/v1/auth/login" -> respondAuthSuccessIfDemoCredentials(request)
            "/v1/auth/refresh" -> respondJson(authResponseJson())
            "/v1/posts" -> respondJson(pagedPostsJson(topicFilter = request.url.parameters["topic"]))
            "/v1/bookmarks" -> respondJson(pagedPostsJson(topicFilter = null, idsOnly = bookmarked))
            else -> {
                val postId = bookmarkPostId(request.url.encodedPath)
                when {
                    postId != null && request.method.value == "POST" -> {
                        bookmarked += postId
                        respond(content = "", status = HttpStatusCode.NoContent)
                    }
                    postId != null && request.method.value == "DELETE" -> {
                        bookmarked -= postId
                        respond(content = "", status = HttpStatusCode.NoContent)
                    }
                    else -> respond(content = "not found", status = HttpStatusCode.NotFound)
                }
            }
        }
    }
}

private val bookmarkPathRegex = Regex("""/v1/posts/([^/]+)/bookmarks""")

private fun bookmarkPostId(path: String): String? = bookmarkPathRegex.matchEntire(path)?.groupValues?.get(1)

private fun MockRequestHandleScope.respondAuthSuccessIfDemoCredentials(request: HttpRequestData): HttpResponseData {
    // Demo mode accepts only the documented credential so the failure-path UI is exercised too.
    val bodyText = (request.body as? TextContent)?.text ?: ""
    return if (bodyText.contains(DEMO_EMAIL) && bodyText.contains(DEMO_PASSWORD)) {
        respondJson(authResponseJson())
    } else {
        respond(content = """{"error":{"code":"invalid_credentials","message":"Invalid email or password"}}""", status = HttpStatusCode.Unauthorized)
    }
}

private fun authResponseJson() = """{"accessToken":"demo-access","refreshToken":"demo-refresh","expiresIn":3600}"""

private fun pagedPostsJson(topicFilter: String?, idsOnly: Set<String>? = null): String {
    val filtered = demoPosts.filter { post ->
        (topicFilter == null || post.topic == topicFilter) && (idsOnly == null || post.id in idsOnly)
    }
    val page = filtered.take(PAGE_SIZE)
    val nextCursor = if (filtered.size > PAGE_SIZE) "next" else null
    return demoJson.encodeToString(PageDto(items = page, nextCursor = nextCursor))
}

private fun MockRequestHandleScope.respondJson(body: String) = respond(
    content = body,
    status = HttpStatusCode.OK,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
)
