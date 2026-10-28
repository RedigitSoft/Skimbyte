package io.redigit.skimbyte.network

import io.redigit.skimbyte.data.remote.ApiConfig
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

    @Test
    fun `relative assets path is prefixed with the asset base url`() {
        assertEquals(
            "${ApiConfig.ASSET_BASE_URL}/assets/placeholder-kotlin.png",
            ApiConfig.resolveImageUrl("/assets/placeholder-kotlin.png"),
        )
    }

    @Test
    fun `absolute image url is passed through unchanged`() {
        assertEquals(
            "https://blog.jetbrains.com/x.png",
            ApiConfig.resolveImageUrl("https://blog.jetbrains.com/x.png"),
        )
    }
}
