package io.redigit.skimbyte.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReadingTimeTest {

    @Test
    fun emptyText_isOneMinute() {
        assertEquals(1, estimateReadingMinutes(""))
        assertEquals(1, estimateReadingMinutes("   \n\t "))
    }

    @Test
    fun shortText_roundsUpToOneMinute() {
        assertEquals(1, estimateReadingMinutes("a handful of words"))
    }

    @Test
    fun exactlyOneMinuteWorthOfWords_isOneMinute() {
        val text = List(200) { "word" }.joinToString(" ")

        assertEquals(1, estimateReadingMinutes(text, wordsPerMinute = 200))
    }

    @Test
    fun oneWordOverAMinute_roundsUpToTwoMinutes() {
        val text = List(201) { "word" }.joinToString(" ")

        assertEquals(2, estimateReadingMinutes(text, wordsPerMinute = 200))
    }

    @Test
    fun collapsesRunsOfWhitespaceWhenCounting() {
        // Four words at 1 wpm → 4 minutes; proves runs of spaces/newlines/tabs collapse to a
        // single separator rather than inflating the count with empty tokens.
        assertEquals(4, estimateReadingMinutes("one\n\ntwo   three\tfour", wordsPerMinute = 1))
    }

    @Test
    fun honorsCustomWordsPerMinute() {
        val text = List(100) { "word" }.joinToString(" ")

        assertEquals(10, estimateReadingMinutes(text, wordsPerMinute = 10))
    }

    @Test
    fun nonPositiveWordsPerMinute_throws() {
        assertFailsWith<IllegalArgumentException> { estimateReadingMinutes("hello", wordsPerMinute = 0) }
    }
}
