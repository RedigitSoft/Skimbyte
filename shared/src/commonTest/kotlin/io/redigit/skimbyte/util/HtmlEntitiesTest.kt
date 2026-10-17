package io.redigit.skimbyte.util

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlEntitiesTest {

    @Test
    fun decimalNumericEntity_decodesToCharacter() {
        assertEquals("It’s here", "It&#8217;s here".decodeHtmlEntities())
    }

    @Test
    fun namedEntities_decodeToCharacters() {
        assertEquals("Kotlin & Android", "Kotlin &amp; Android".decodeHtmlEntities())
        assertEquals("<tag>", "&lt;tag&gt;".decodeHtmlEntities())
    }

    @Test
    fun hexNumericEntity_decodesToCharacter() {
        assertEquals(" ", "&#xA0;".decodeHtmlEntities())
    }

    @Test
    fun textWithoutEntities_isUnchanged() {
        assertEquals("plain text, no entities here", "plain text, no entities here".decodeHtmlEntities())
    }
}
