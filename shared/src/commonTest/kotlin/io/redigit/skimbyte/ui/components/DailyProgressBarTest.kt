package io.redigit.skimbyte.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DailyProgressBarTest {

    @Test
    fun zeroTotal_hasNoSegments() {
        assertTrue(progressSegments(read = 0, total = 0).isEmpty())
        assertTrue(progressSegments(read = 5, total = 0).isEmpty())
    }

    @Test
    fun fewerThanMax_usesOneSegmentPerItem() {
        val segments = progressSegments(read = 3, total = 8, maxSegments = 12)

        assertEquals(8, segments.size)
        assertEquals(listOf(true, true, true, false, false, false, false, false), segments)
    }

    @Test
    fun allRead_fillsEverySegment() {
        assertEquals(List(8) { true }, progressSegments(read = 8, total = 8))
    }

    @Test
    fun noneRead_fillsNoSegment() {
        assertEquals(List(8) { false }, progressSegments(read = 0, total = 8))
    }

    @Test
    fun moreThanMax_capsSegmentsAndFillsProportionally() {
        // 20 of 40 read, capped at 12 segments → half of 12 = 6 filled.
        val segments = progressSegments(read = 20, total = 40, maxSegments = 12)

        assertEquals(12, segments.size)
        assertEquals(6, segments.count { it })
        assertEquals(List(6) { true } + List(6) { false }, segments)
    }

    @Test
    fun readBeyondTotal_isClampedToFull() {
        assertEquals(List(8) { true }, progressSegments(read = 99, total = 8))
    }

    @Test
    fun negativeRead_isClampedToEmpty() {
        assertEquals(List(8) { false }, progressSegments(read = -3, total = 8))
    }
}
