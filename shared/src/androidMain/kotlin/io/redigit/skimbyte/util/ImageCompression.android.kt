package io.redigit.skimbyte.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

actual suspend fun compressForUpload(image: PickedImage, maxBytes: Int): PickedImage = runCatching {
    if (image.bytes.size <= maxBytes) return image
    var bitmap = BitmapFactory.decodeByteArray(image.bytes, 0, image.bytes.size) ?: return image
    var smallest: ByteArray? = null
    while (true) {
        for (quality in UPLOAD_JPEG_QUALITIES) {
            val encoded = ByteArrayOutputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.toByteArray()
            }
            if (encoded.size <= maxBytes) return PickedImage(encoded, "image/jpeg")
            smallest = encoded
        }
        val width = (bitmap.width * UPLOAD_DOWNSCALE_FACTOR).toInt()
        val height = (bitmap.height * UPLOAD_DOWNSCALE_FACTOR).toInt()
        if (width < UPLOAD_MIN_DIMENSION || height < UPLOAD_MIN_DIMENSION) break
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
    // Best effort: hand back the smallest JPEG we produced even if still above the cap, so the
    // caller can decide (it treats an over-cap result as a failure).
    smallest?.let { PickedImage(it, "image/jpeg") } ?: image
}.getOrDefault(image)
