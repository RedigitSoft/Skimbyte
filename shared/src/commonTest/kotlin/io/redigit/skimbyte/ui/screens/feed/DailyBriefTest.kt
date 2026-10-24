package io.redigit.skimbyte.ui.screens.feed

import io.redigit.skimbyte.data.models.Post
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DailyBriefTest {

    private fun post(id: String, date: String?) = Post(
        id = id,
        topic = "Kotlin",
        title = id,
        summaryMarkdown = "",
        sourceName = "src",
        sourceUrl = "https://example.com",
        imageUrl = null,
        digestDate = date,
        publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
        isBookmarked = false,
    )

    @Test
    fun emptyFeed_isZeroOfZero() {
        assertEquals(DailyBrief(0, 0), dailyBrief(emptyList(), currentIndex = 0))
    }

    @Test
    fun countsOnlyLatestDatePosts_asTotal() {
        val posts = listOf(
            post("a", "2026-10-13"),
            post("b", "2026-10-13"),
            post("c", "2026-10-12"),
        )

        // Latest date is 10-13 → total 2, and at index 0 the reader has read 1 of them.
        assertEquals(DailyBrief(read = 1, total = 2), dailyBrief(posts, currentIndex = 0))
    }

    @Test
    fun readCountsOnlyLatestDatePostsUpToIndex() {
        val posts = listOf(
            post("a", "2026-10-13"),
            post("b", "2026-10-13"),
            post("c", "2026-10-12"),
        )

        // Index 2 spans all posts, but only the two 10-13 posts count toward the brief.
        assertEquals(DailyBrief(read = 2, total = 2), dailyBrief(posts, currentIndex = 2))
    }

    @Test
    fun indexBeyondLatestDate_doesNotOvercount() {
        val posts = listOf(post("a", "2026-10-13"), post("b", "2026-10-12"))

        assertEquals(DailyBrief(read = 1, total = 1), dailyBrief(posts, currentIndex = 1))
    }

    @Test
    fun noDates_treatsWholeFeedAsOneBrief() {
        val posts = listOf(post("a", null), post("b", null), post("c", null))

        assertEquals(DailyBrief(read = 2, total = 3), dailyBrief(posts, currentIndex = 1))
    }

    @Test
    fun outOfRangeIndex_isClamped() {
        val posts = listOf(post("a", "2026-10-13"))

        assertEquals(DailyBrief(read = 1, total = 1), dailyBrief(posts, currentIndex = 99))
    }
}
