package io.redigit.skimbyte.util

import kotlin.math.max
import kotlin.math.roundToInt

/** A pixel rectangle within a source image. */
data class PixelRect(val x: Int, val y: Int, val width: Int, val height: Int)

/** The scale that makes a [srcW]x[srcH] image just cover a [frameW]x[frameH] crop frame. */
fun coverBaseScale(frameW: Float, frameH: Float, srcW: Int, srcH: Int): Float =
    max(frameW / srcW, frameH / srcH)

/** The furthest the scaled image can pan on one axis while still fully covering the frame. */
fun maxPan(frameSize: Float, sourceSize: Int, totalScale: Float): Float =
    ((sourceSize * totalScale - frameSize) / 2f).coerceAtLeast(0f)

/**
 * The source-pixel rectangle currently inside the crop frame, given the cover-fit base plus the
 * user's [userScale] and pan offsets (frame pixels, measured from the frame centre). Pure, so the
 * interactive cropper's gesture mapping is unit-testable independent of Compose.
 */
fun computeCropRect(
    frameW: Float,
    frameH: Float,
    srcW: Int,
    srcH: Int,
    userScale: Float,
    panX: Float,
    panY: Float,
): PixelRect {
    val k = coverBaseScale(frameW, frameH, srcW, srcH) * userScale
    if (k <= 0f) return PixelRect(0, 0, srcW, srcH)
    val sx0 = srcW / 2f + (-frameW / 2f - panX) / k
    val sy0 = srcH / 2f + (-frameH / 2f - panY) / k
    return PixelRect(
        x = sx0.roundToInt(),
        y = sy0.roundToInt(),
        width = (frameW / k).roundToInt().coerceAtLeast(1),
        height = (frameH / k).roundToInt().coerceAtLeast(1),
    )
}

/**
 * The centered crop rectangle that trims a [sourceWidth] x [sourceHeight] image to [targetAspect]
 * (width / height). Pure, so the platform crop codecs share one definition and it can be unit-tested.
 */
fun centerCropRect(sourceWidth: Int, sourceHeight: Int, targetAspect: Float): PixelRect {
    require(sourceWidth > 0 && sourceHeight > 0) { "source dimensions must be positive" }
    require(targetAspect > 0f) { "targetAspect must be positive" }
    val sourceAspect = sourceWidth.toFloat() / sourceHeight.toFloat()
    return if (sourceAspect > targetAspect) {
        // Too wide for the target: keep full height, trim the sides.
        val width = (sourceHeight * targetAspect).toInt().coerceIn(1, sourceWidth)
        PixelRect(x = (sourceWidth - width) / 2, y = 0, width = width, height = sourceHeight)
    } else {
        // Too tall for the target: keep full width, trim top and bottom.
        val height = (sourceWidth / targetAspect).toInt().coerceIn(1, sourceHeight)
        PixelRect(x = 0, y = (sourceHeight - height) / 2, width = sourceWidth, height = height)
    }
}

/**
 * Clamps this rectangle to lie within a [sourceWidth] x [sourceHeight] image, or null if it doesn't
 * overlap the image at all (degenerate). Keeps the platform crop codecs from throwing on an
 * off-image or oversized rect produced by gesture rounding.
 */
fun PixelRect.clampedTo(sourceWidth: Int, sourceHeight: Int): PixelRect? {
    if (sourceWidth <= 0 || sourceHeight <= 0) return null
    val left = x.coerceIn(0, sourceWidth - 1)
    val top = y.coerceIn(0, sourceHeight - 1)
    val right = (x + width).coerceIn(left + 1, sourceWidth)
    val bottom = (y + height).coerceIn(top + 1, sourceHeight)
    return PixelRect(left, top, right - left, bottom - top)
}

/** Common aspect ratios used by the profile images. */
const val AVATAR_ASPECT = 1f
const val BANNER_ASPECT = 3f

/** Pixel dimensions of a decoded image. */
data class ImageSize(val width: Int, val height: Int)

/**
 * Center-crops [image] to [targetAspect] (width / height) on-device and returns the cropped bytes.
 * Real on Android and JVM; a no-op passthrough on iOS/JS/Wasm until a platform image codec is wired
 * there. Returns the original image unchanged if it can't be decoded.
 */
expect suspend fun cropToAspect(image: PickedImage, targetAspect: Float): PickedImage

/**
 * Decodes just the pixel dimensions of [bytes] without loading the full bitmap, or null if the
 * bytes can't be decoded. Lets the interactive cropper map on-screen gestures back to source pixels.
 */
expect fun decodeImageSize(bytes: ByteArray): ImageSize?

/**
 * Crops [image] to the source-pixel [rect] and returns the cropped bytes (PNG). Powers the
 * interactive cropper, which computes [rect] from the user's pan/zoom. Returns the original image
 * unchanged if it can't be decoded or the rect is degenerate.
 */
expect suspend fun cropToRect(image: PickedImage, rect: PixelRect): PickedImage
