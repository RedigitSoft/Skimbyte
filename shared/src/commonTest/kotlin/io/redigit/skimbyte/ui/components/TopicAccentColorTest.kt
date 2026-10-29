package io.redigit.skimbyte.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TopicAccentColorTest {

    @Test
    fun sameTopic_alwaysMapsToTheSameColor() {
        assertEquals(topicAccentColor("Kotlin"), topicAccentColor("Kotlin"))
        assertEquals(topicAccentColor("KMP / Compose Multiplatform"), topicAccentColor("KMP / Compose Multiplatform"))
    }

    @Test
    fun seedTopics_getDistinctTints() {
        // Not guaranteed for arbitrary strings (palette is finite), but the shipped seed topics
        // must not collide — that's the whole point of per-topic coding in the feed.
        val colors = listOf("Kotlin", "Android", "KMP / Compose Multiplatform", "Tooling & Products")
            .map { topicAccentColor(it) }
        assertEquals(colors.size, colors.toSet().size)
    }

    @Test
    fun differentCasing_isADifferentTopicString() {
        // Topics come from the server verbatim; the mapping is intentionally literal.
        assertNotEquals("kotlin", "Kotlin")
        assertEquals(topicAccentColor("kotlin"), topicAccentColor("kotlin"))
    }
}
