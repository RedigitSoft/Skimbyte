package io.redigit.skimbyte.util

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

// JS Compose is Skia-backed, so the compressor uses Skia directly - the same implementation shared
// with the iOS and Wasm targets.
actual suspend fun compressForUpload(image: PickedImage, maxBytes: Int): PickedImage = runCatching {
    if (image.bytes.size <= maxBytes) return image
    var decoded = Image.makeFromEncoded(image.bytes)
    var smallest: ByteArray? = null
    while (true) {
        for (quality in UPLOAD_JPEG_QUALITIES) {
            val encoded = decoded.encodeToData(EncodedImageFormat.JPEG, quality)?.bytes ?: continue
            if (encoded.size <= maxBytes) return PickedImage(encoded, "image/jpeg")
            smallest = encoded
        }
        val width = (decoded.width * UPLOAD_DOWNSCALE_FACTOR).toInt()
        val height = (decoded.height * UPLOAD_DOWNSCALE_FACTOR).toInt()
        if (width < UPLOAD_MIN_DIMENSION || height < UPLOAD_MIN_DIMENSION) break
        val surface = Surface.makeRasterN32Premul(width, height)
        surface.canvas.drawImageRect(
            decoded,
            Rect.makeWH(decoded.width.toFloat(), decoded.height.toFloat()),
            Rect.makeWH(width.toFloat(), height.toFloat()),
        )
        decoded = surface.makeImageSnapshot()
    }
    smallest?.let { PickedImage(it, "image/jpeg") } ?: image
}.getOrDefault(image)
