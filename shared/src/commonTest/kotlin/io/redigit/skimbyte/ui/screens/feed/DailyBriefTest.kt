package io.redigit.skimbyte.ui.screens.feed

import io.redigit.skimbyte.data.models.Post
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DailyBriefTest {

    private val utc = TimeZone.UTC

    private fun post(id: String, publishedAt: String) = Post(
        id = id,
        topic = "Kotlin",
        title = id,
        summaryMarkdown = "",
        sourceName = "src",
        sourceUrl = "https://example.com",
        imageUrl = null,
        digestDate = null,
        publishedAt = Instant.parse(publishedAt),
        isBookmarked = false,
    )

    @Test
    fun emptyFeed_isZeroOfZero() {
        assertEquals(DailyBrief(0, 0), dailyBrief(emptyList(), currentIndex = 0, timeZone = utc))
    }

    @Test
    fun countsOnlyNewestDayPosts_asTotal() {
        val posts = listOf(
            post("a", "2026-10-23T18:00:00Z"),
            post("b", "2026-10-23T09:00:00Z"),
            post("c", "2026-10-23T07:30:00Z"),
            post("d", "2026-10-20T12:00:00Z"),
        )

        // Newest day is 10-23 with three posts → 3 segments; at index 0 the reader has read 1.
        assertEquals(DailyBrief(read = 1, total = 3), dailyBrief(posts, currentIndex = 0, timeZone = utc))
    }

    @Test
    fun readCountsOnlyNewestDayPostsUpToIndex() {
        val posts = listOf(
            post("a", "2026-10-23T18:00:00Z"),
            post("b", "2026-10-23T09:00:00Z"),
            post("c", "2026-10-20T12:00:00Z"),
        )

        // Index 2 spans all posts, but only the two 10-23 posts count toward the brief.
        assertEquals(DailyBrief(read = 2, total = 2), dailyBrief(posts, currentIndex = 2, timeZone = utc))
    }

    @Test
    fun digestDateIsIgnored_groupingIsByPublishDay() {
        // A backfilled source: ingested recently (same digest date) but published weeks apart.
        val posts = listOf(
            post("a", "2026-10-23T18:00:00Z").copy(digestDate = "2026-10-25"),
            post("b", "2026-09-10T09:00:00Z").copy(digestDate = "2026-10-25"),
        )

        assertEquals(DailyBrief(read = 1, total = 1), dailyBrief(posts, currentIndex = 0, timeZone = utc))
    }

    @Test
    fun dayBoundaryRespectsTimeZone() {
        // 23:30 UTC on the 22nd is already Oct 23 in UTC+2 — both posts share a day there, not in UTC.
        val posts = listOf(
            post("a", "2026-10-23T08:00:00Z"),
            post("b", "2026-10-22T23:30:00Z"),
        )

        assertEquals(DailyBrief(read = 1, total = 1), dailyBrief(posts, currentIndex = 0, timeZone = utc))
        assertEquals(
            DailyBrief(read = 1, total = 2),
            dailyBrief(posts, currentIndex = 0, timeZone = TimeZone.of("UTC+2")),
        )
    }

    @Test
    fun outOfRangeIndex_isClamped() {
        val posts = listOf(post("a", "2026-10-23T08:00:00Z"))

        assertEquals(DailyBrief(read = 1, total = 1), dailyBrief(posts, currentIndex = 99, timeZone = utc))
    }
}
