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
import io.redigit.skimbyte.data.models.dto.PageDto
import io.redigit.skimbyte.data.remote.createHttpClient
import io.redigit.skimbyte.data.models.dto.PostDto
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
    single<HttpClient> { createHttpClient(get(), get(), demoMockEngine()) }
}

private const val DEMO_EMAIL = "demo@skimbyte.app"
private const val DEMO_PASSWORD = "demo"
private const val PAGE_SIZE = 8

private val demoJson = Json { ignoreUnknownKeys = true }

// Branded sample-post images, hosted with the website (marketing/generate.mjs keeps them
// fresh). Offline, the image simply falls back to the monogram placeholder under it.
private const val PLACEHOLDER_DARK = "https://skimbyte.com/post-placeholder-dark.png"
private const val PLACEHOLDER_LIGHT = "https://skimbyte.com/post-placeholder-light.png"

private fun demoPost(
    id: String,
    topic: String,
    title: String,
    summary: String,
    hoursAgo: Int,
    imageUrl: String? = null,
) = PostDto(
    id = id,
    topic = topic,
    title = title,
    summaryMarkdown = summary,
    sourceName = "Skimbyte Samples",
    sourceUrl = "https://example.com/posts/$id",
    imageUrl = imageUrl,
    digestDate = "2026-11-13",
    publishedAt = Instant.parse("2026-11-13T08:00:00Z") - hoursAgo.hours,
)

// Hand-written sample stories about Skimbyte itself — clearly demo content, never fake news.
private val featuredDemoPosts: List<PostDto> = listOf(
    demoPost(
        id = "demo-featured-0",
        topic = "Tooling & Products",
        title = "This is a Skimbyte brief: the day's news in a minute",
        summary = "A Skimbyte story is a one-minute summary of a real article, with the source it " +
            "came from and the read time. Skim it, **save it for later**, or jump straight to the " +
            "full piece. This sample card ships with the app so demo mode works without a network.",
        hoursAgo = 0,
        imageUrl = PLACEHOLDER_DARK,
    ),
    demoPost(
        id = "demo-featured-1",
        topic = "Kotlin",
        title = "What a release note really means, the Skimbyte way",
        summary = "A sample brief showing how Skimbyte condenses a long changelog into the three " +
            "changes that actually affect your project. **Demo mode** runs on bundled stories like " +
            "this one — no account, no backend, every screen still works.",
        hoursAgo = 1,
        imageUrl = PLACEHOLDER_LIGHT,
    ),
    demoPost(
        id = "demo-featured-2",
        topic = "KMP / Compose Multiplatform",
        title = "One Kotlin codebase, and this card is proof",
        summary = "The card you are reading renders from the same Compose code on Android, iOS, " +
            "desktop, and the browser. Skimbyte is **open source** — clone it and run every target " +
            "on this bundled sample feed.",
        hoursAgo = 2,
        imageUrl = PLACEHOLDER_DARK,
    ),
    demoPost(
        id = "demo-featured-3",
        topic = "AI for Mobile Devs",
        title = "Summaries for humans: how Skimbyte keeps the caveats",
        summary = "Sample content: Skimbyte's daily pipeline turns announcements into plain-words " +
            "briefs and keeps the caveats in. Pick your topics and the feed stays **short enough " +
            "to finish with your coffee**.",
        hoursAgo = 3,
        imageUrl = PLACEHOLDER_LIGHT,
    ),
)

private val demoPosts: List<PostDto> = buildList {
    addAll(featuredDemoPosts)
    val topics = listOf(
        "Kotlin",
        "Android",
        "KMP / Compose Multiplatform",
        "Tooling & Products",
    )
    topics.forEachIndexed { topicIndex, topic ->
        repeat(6) { i ->
            add(
                demoPost(
                    id = "demo-${topicIndex}-$i",
                    topic = topic,
                    title = "Skimbyte sample brief: $topic #${i + 1}",
                    summary = "Bundled sample content for the **$topic** feed. Skimbyte's demo " +
                        "mode ships with stories like this so every screen works without a backend.",
                    hoursAgo = 4 + topicIndex * 6 + i,
                    imageUrl = if (i % 2 == 0) PLACEHOLDER_LIGHT else PLACEHOLDER_DARK,
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
            "/v1/me" -> {
                // Account deletion: drop all of this user's server-side data (bookmarks).
                if (request.method.value == "DELETE") bookmarked.clear()
                respond(content = "", status = HttpStatusCode.NoContent)
            }
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
