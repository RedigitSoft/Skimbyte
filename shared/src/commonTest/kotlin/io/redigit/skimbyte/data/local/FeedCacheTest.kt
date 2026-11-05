package io.redigit.skimbyte.data.local

import io.redigit.skimbyte.data.models.dto.PageDto
import io.redigit.skimbyte.data.models.dto.PostDto
import io.redigit.skimbyte.testing.FakeSettings
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FeedCacheTest {

    private fun post(id: String) = PostDto(
        id = id,
        topic = "Kotlin",
        title = "Title $id",
        summaryMarkdown = "Summary",
        sourceName = "Source",
        sourceUrl = "https://example.test/$id",
        publishedAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    @Test
    fun saveThenLoad_roundTripsThePage() {
        val cache = SettingsFeedCache(FakeSettings())
        val page = PageDto(items = listOf(post("a"), post("b")), nextCursor = "next")

        cache.save("all", page)

        assertEquals(page, cache.load("all"))
    }

    @Test
    fun load_missingKey_returnsNull() {
        assertNull(SettingsFeedCache(FakeSettings()).load("all"))
    }

    @Test
    fun load_corruptValue_returnsNull() {
        val cache = SettingsFeedCache(FakeSettings(mapOf("feed_cache_all" to "not valid json")))
        assertNull(cache.load("all"))
    }

    @Test
    fun entriesAreScopedPerTopicKey() {
        val cache = SettingsFeedCache(FakeSettings())
        cache.save("all", PageDto(listOf(post("a")), nextCursor = null))
        cache.save("Kotlin", PageDto(listOf(post("b")), nextCursor = null))

        assertEquals(listOf("a"), cache.load("all")?.items?.map { it.id })
        assertEquals(listOf("b"), cache.load("Kotlin")?.items?.map { it.id })
    }
}
