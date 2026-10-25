package io.redigit.skimbyte.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

actual suspend fun cropToAspect(image: PickedImage, targetAspect: Float): PickedImage = runCatching {
    val source = decodeOrientedBitmap(image.bytes) ?: return image
    if (source.width <= 0 || source.height <= 0) return image
    cropBitmap(source, centerCropRect(source.width, source.height, targetAspect))
}.getOrDefault(image)

actual fun decodeImageSize(bytes: ByteArray): ImageSize? {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    if (opts.outWidth <= 0 || opts.outHeight <= 0) return null
    // Report post-orientation dimensions, matching how Coil renders the image in the cropper;
    // otherwise the crop rect is computed against swapped axes for rotated camera photos.
    return if (hasSwappedAxes(bytes)) {
        ImageSize(opts.outHeight, opts.outWidth)
    } else {
        ImageSize(opts.outWidth, opts.outHeight)
    }
}

actual suspend fun cropToRect(image: PickedImage, rect: PixelRect): PickedImage = runCatching {
    val source = decodeOrientedBitmap(image.bytes) ?: return image
    val safe = rect.clampedTo(source.width, source.height) ?: return image
    cropBitmap(source, safe)
}.getOrDefault(image)

private fun cropBitmap(source: Bitmap, rect: PixelRect): PickedImage {
    val cropped = Bitmap.createBitmap(source, rect.x, rect.y, rect.width, rect.height)
    val out = ByteArrayOutputStream()
    cropped.compress(Bitmap.CompressFormat.PNG, 100, out)
    return PickedImage(bytes = out.toByteArray(), mimeType = "image/png")
}

// Decodes and bakes in EXIF orientation so cropping runs on upright pixels that match what Coil
// shows. Camera photos commonly store sideways pixels plus an orientation flag; without applying it
// the cropper preview and the uploaded crop disagree.
private fun decodeOrientedBitmap(bytes: ByteArray): Bitmap? {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    val matrix = orientationMatrix(bytes) ?: return bitmap
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun orientationMatrix(bytes: ByteArray): Matrix? {
    val matrix = Matrix()
    when (exifOrientation(bytes)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f); matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(270f); matrix.postScale(-1f, 1f)
        }
        else -> return null
    }
    return matrix
}

private fun hasSwappedAxes(bytes: ByteArray): Boolean = when (exifOrientation(bytes)) {
    ExifInterface.ORIENTATION_ROTATE_90,
    ExifInterface.ORIENTATION_ROTATE_270,
    ExifInterface.ORIENTATION_TRANSPOSE,
    ExifInterface.ORIENTATION_TRANSVERSE,
    -> true
    else -> false
}

private fun exifOrientation(bytes: ByteArray): Int = runCatching {
    ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL,
    )
}.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
