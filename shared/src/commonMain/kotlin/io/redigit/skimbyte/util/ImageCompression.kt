package io.redigit.skimbyte.util

/** Upload ceiling for profile/banner images: the result of [compressForUpload] fits within this. */
const val MAX_UPLOAD_IMAGE_BYTES: Int = 2 * 1024 * 1024

// Shared tuning for the platform [compressForUpload] actuals: at each size try these JPEG qualities
// in order; if even the lowest still doesn't fit, downscale by [UPLOAD_DOWNSCALE_FACTOR] and repeat
// the whole ladder — looping until the result is within the cap. [UPLOAD_MIN_DIMENSION] is only a
// termination floor for pathological inputs: any real image reaches the cap long before it (a JPEG
// at these dimensions is far under 2 MB), so the loop effectively always succeeds first.
internal val UPLOAD_JPEG_QUALITIES = intArrayOf(90, 80, 70, 60, 50, 40)
internal const val UPLOAD_DOWNSCALE_FACTOR = 0.75
internal const val UPLOAD_MIN_DIMENSION = 16

/**
 * Ensures [image] is at most [maxBytes] by re-encoding it as JPEG, stepping the quality down and
 * then repeatedly downscaling and re-compressing until it fits (not a single pass — it keeps
 * shrinking and re-encoding each cycle until the output is within [maxBytes]). Returns [image]
 * unchanged when it is already within the limit, or when it cannot be decoded. Backed per-platform
 * because only the native image codecs can resize and re-encode bitmaps; the crop step still emits
 * large PNGs, so this is the common chokepoint that keeps avatar and banner uploads small.
 */
expect suspend fun compressForUpload(
    image: PickedImage,
    maxBytes: Int = MAX_UPLOAD_IMAGE_BYTES,
): PickedImage
