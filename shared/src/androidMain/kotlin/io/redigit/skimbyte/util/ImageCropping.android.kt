package io.redigit.skimbyte.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

actual suspend fun cropToAspect(image: PickedImage, targetAspect: Float): PickedImage = runCatching {
    val source = BitmapFactory.decodeByteArray(image.bytes, 0, image.bytes.size) ?: return image
    if (source.width <= 0 || source.height <= 0) return image
    val rect = centerCropRect(source.width, source.height, targetAspect)
    val cropped = Bitmap.createBitmap(source, rect.x, rect.y, rect.width, rect.height)
    val out = ByteArrayOutputStream()
    cropped.compress(Bitmap.CompressFormat.PNG, 100, out)
    PickedImage(bytes = out.toByteArray(), mimeType = "image/png")
}.getOrDefault(image)
