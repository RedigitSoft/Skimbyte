package io.redigit.skimbyte.util

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DateFormattingTest {

    @Test
    fun toDisplayDate_formatsAsAbbreviatedMonthDayYear() {
        val instant = Instant.parse("2026-10-12T18:00:00Z")

        val displayed = instant.toDisplayDate(timeZone = TimeZone.UTC)

        assertEquals("Oct 12, 2026", displayed)
    }
}
