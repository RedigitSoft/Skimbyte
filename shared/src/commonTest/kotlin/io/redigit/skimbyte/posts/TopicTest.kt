package io.redigit.skimbyte.posts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TopicTest {

    @Test
    fun entries_containsAllFiveTopics_withNoNulls() {
        assertEquals(5, Topic.entries.size)
        Topic.entries.forEach { assertNotNull(it) }
        assertEquals(listOf("All", "Kotlin", "Android", "KMP", "Tooling"), Topic.entries.map { it.displayLabel })
    }
}
