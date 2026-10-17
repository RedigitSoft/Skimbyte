package io.redigit.skimbyte.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlin.time.Instant

private val displayDateFormat = LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
    chars(", ")
    year()
}

fun Instant.toDisplayDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): String =
    toLocalDateTime(timeZone).format(displayDateFormat)
