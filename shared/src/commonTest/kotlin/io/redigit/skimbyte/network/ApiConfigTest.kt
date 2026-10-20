package io.redigit.skimbyte.network

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiConfigTest {

    @Test
    fun `bare path is prefixed with the Spaces CDN base url`() {
        assertEquals(
            "${ApiConfig.SPACES_CDN_BASE_URL}/avatars/u1/abc.jpg",
            ApiConfig.resolveSpacesUrl("avatars/u1/abc.jpg"),
        )
    }

    @Test
    fun `already absolute url is passed through unchanged`() {
        assertEquals("https://other.cdn/x.jpg", ApiConfig.resolveSpacesUrl("https://other.cdn/x.jpg"))
    }
}
