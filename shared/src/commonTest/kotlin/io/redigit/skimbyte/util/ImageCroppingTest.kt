package io.redigit.skimbyte.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ImageCroppingTest {

    @Test
    fun squareTargetFromWideImage_trimsSidesCentered() {
        val rect = centerCropRect(sourceWidth = 400, sourceHeight = 200, targetAspect = 1f)
        assertEquals(PixelRect(x = 100, y = 0, width = 200, height = 200), rect)
    }

    @Test
    fun squareTargetFromTallImage_trimsTopBottomCentered() {
        val rect = centerCropRect(sourceWidth = 200, sourceHeight = 400, targetAspect = 1f)
        assertEquals(PixelRect(x = 0, y = 100, width = 200, height = 200), rect)
    }

    @Test
    fun wideBannerTarget_trimsHeight() {
        // 3:1 target from a 300x300 square → keep full width, height 100, centered vertically.
        val rect = centerCropRect(sourceWidth = 300, sourceHeight = 300, targetAspect = 3f)
        assertEquals(PixelRect(x = 0, y = 100, width = 300, height = 100), rect)
    }

    @Test
    fun matchingAspect_keepsWholeImage() {
        val rect = centerCropRect(sourceWidth = 300, sourceHeight = 100, targetAspect = 3f)
        assertEquals(PixelRect(x = 0, y = 0, width = 300, height = 100), rect)
    }

    @Test
    fun invalidInputs_throw() {
        assertFailsWith<IllegalArgumentException> { centerCropRect(0, 100, 1f) }
        assertFailsWith<IllegalArgumentException> { centerCropRect(100, 100, 0f) }
    }
}
