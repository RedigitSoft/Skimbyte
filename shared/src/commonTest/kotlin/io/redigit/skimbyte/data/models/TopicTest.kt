package io.redigit.skimbyte.data.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TopicTest {

    @Test
    fun entries_containsAllFiveTopics_withNoNulls() {
        assertEquals(5, Topic.defaults.size)
        Topic.defaults.forEach { assertNotNull(it) }
        assertEquals(listOf("All", "Kotlin", "Android", "KMP", "Tooling"), Topic.defaults.map { it.displayLabel })
    }
}
