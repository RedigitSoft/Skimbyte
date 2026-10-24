package io.redigit.skimbyte.ui.screens.feed

import io.redigit.skimbyte.data.models.Post

/** How far the reader has progressed through the latest digest date's posts. */
internal data class DailyBrief(val read: Int, val total: Int)

/**
 * Computes the "daily brief" progress: how many of the *latest digest date's* posts the reader has
 * reached, out of the total for that date - so the counter reflects today's briefs rather than the
 * whole (multi-day, paginated) feed. [currentIndex] is the reader's position in [posts].
 *
 * Digest dates are ISO `yyyy-MM-dd` strings, so the lexicographic max is the most recent date. If no
 * post carries a date, the whole feed is treated as a single brief.
 */
internal fun dailyBrief(posts: List<Post>, currentIndex: Int): DailyBrief {
    if (posts.isEmpty()) return DailyBrief(read = 0, total = 0)
    val latestDate = posts.mapNotNull { it.digestDate }.maxOrNull()
    val isToday: (Post) -> Boolean = { post -> latestDate == null || post.digestDate == latestDate }
    val total = posts.count(isToday)
    val safeIndex = currentIndex.coerceIn(0, posts.lastIndex)
    val read = posts.take(safeIndex + 1).count(isToday).coerceIn(0, total)
    return DailyBrief(read = read, total = total)
}
