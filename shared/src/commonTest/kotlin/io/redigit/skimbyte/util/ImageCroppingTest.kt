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

    @Test
    fun clampedTo_trimsRectToImageBounds() {
        val clamped = PixelRect(x = -10, y = -10, width = 500, height = 500).clampedTo(400, 300)
        assertEquals(PixelRect(x = 0, y = 0, width = 400, height = 300), clamped)
    }

    @Test
    fun clampedTo_rectFullyOffImage_isPulledInsideStillNonEmpty() {
        val clamped = PixelRect(x = 1000, y = 1000, width = 50, height = 50).clampedTo(400, 300)
        // x/y are coerced to the last valid pixel, leaving a 1px-minimum rect inside the image.
        assertEquals(PixelRect(x = 399, y = 299, width = 1, height = 1), clamped)
    }

    @Test
    fun coverBaseScale_picksTheLargerAxisRatioToCoverTheFrame() {
        // 100x100 frame, 200x400 source: width ratio 0.5, height ratio 0.25 → cover uses 0.5.
        assertEquals(0.5f, coverBaseScale(frameW = 100f, frameH = 100f, srcW = 200, srcH = 400))
    }

    @Test
    fun maxPan_isZeroWhenImageExactlyCoversFrame() {
        assertEquals(0f, maxPan(frameSize = 100f, sourceSize = 100, totalScale = 1f))
        assertEquals(50f, maxPan(frameSize = 100f, sourceSize = 200, totalScale = 1f))
    }

    @Test
    fun computeCropRect_centeredNoZoom_isTheCenterCoverRegion() {
        // 100x100 frame over a 200x400 source, cover base 0.5, no user zoom/pan: the frame maps to
        // the centered 200x200 region (full width, vertically centered) - matching a center crop.
        val rect = computeCropRect(
            frameW = 100f, frameH = 100f, srcW = 200, srcH = 400, userScale = 1f, panX = 0f, panY = 0f,
        )
        assertEquals(PixelRect(x = 0, y = 100, width = 200, height = 200), rect)
    }

    @Test
    fun computeCropRect_zoomingInShrinksTheSampledRegion() {
        val rect = computeCropRect(
            frameW = 100f, frameH = 100f, srcW = 200, srcH = 400, userScale = 2f, panX = 0f, panY = 0f,
        )
        // Double zoom halves the sampled source dimensions (200x200 → 100x100), still centered.
        assertEquals(100, rect.width)
        assertEquals(100, rect.height)
        assertEquals(50, rect.x)
        assertEquals(150, rect.y)
    }
}
