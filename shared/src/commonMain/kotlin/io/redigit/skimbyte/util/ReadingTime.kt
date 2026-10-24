package io.redigit.skimbyte.util

import kotlin.math.ceil
import kotlin.math.max

private val whitespace = Regex("\\s+")

/**
 * Estimates how many minutes it takes to read [text], used for the "N min read" badge on a story
 * card. Counts whitespace-separated tokens and divides by [wordsPerMinute] (200 is a common average
 * for adult silent reading of prose), rounding up. Always returns at least 1 so even a one-line
 * summary reads as "1 min read" rather than "0".
 */
fun estimateReadingMinutes(text: String, wordsPerMinute: Int = 200): Int {
    require(wordsPerMinute > 0) { "wordsPerMinute must be positive, was $wordsPerMinute" }
    val words = text.trim().split(whitespace).count { it.isNotEmpty() }
    if (words == 0) return 1
    return max(1, ceil(words.toDouble() / wordsPerMinute).toInt())
}
