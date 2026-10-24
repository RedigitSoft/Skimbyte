package io.redigit.skimbyte.util

/** A pixel rectangle within a source image. */
data class PixelRect(val x: Int, val y: Int, val width: Int, val height: Int)

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

/** Common aspect ratios used by the profile images. */
const val AVATAR_ASPECT = 1f
const val BANNER_ASPECT = 3f

/**
 * Center-crops [image] to [targetAspect] (width / height) on-device and returns the cropped bytes.
 * Real on Android and JVM; a no-op passthrough on iOS/JS/Wasm until a platform image codec is wired
 * there. Returns the original image unchanged if it can't be decoded.
 */
expect suspend fun cropToAspect(image: PickedImage, targetAspect: Float): PickedImage
