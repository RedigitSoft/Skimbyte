package io.redigit.skimbyte.ui.screens.feed

import io.redigit.skimbyte.data.models.Post
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** How far the reader has progressed through the latest day's posts. */
internal data class DailyBrief(val read: Int, val total: Int)

/**
 * Computes the "daily brief" progress: how many of the *newest published day's* posts the reader
 * has reached, out of the total for that day. The feed is ordered by publish time, so that group
 * is the contiguous run at the top - one bar segment per post, filling as the reader pages through
 * them. (Grouping deliberately uses the publish date, not `digestDate`: ingestion can backfill a
 * source's old posts under a new digest date, which would scatter the "brief" across the feed and
 * make the total shift as pages load.) [currentIndex] is the reader's position in [posts].
 */
internal fun dailyBrief(
    posts: List<Post>,
    currentIndex: Int,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): DailyBrief {
    if (posts.isEmpty()) return DailyBrief(read = 0, total = 0)
    val latestDay = posts.maxOf { it.publishedAt }.toLocalDateTime(timeZone).date
    val isLatestDay: (Post) -> Boolean = { it.publishedAt.toLocalDateTime(timeZone).date == latestDay }
    val total = posts.count(isLatestDay)
    val safeIndex = currentIndex.coerceIn(0, posts.lastIndex)
    val read = posts.take(safeIndex + 1).count(isLatestDay).coerceIn(0, total)
    return DailyBrief(read = read, total = total)
}
