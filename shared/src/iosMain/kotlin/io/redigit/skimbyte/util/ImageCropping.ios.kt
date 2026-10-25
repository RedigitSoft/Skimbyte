package io.redigit.skimbyte.util

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

// iOS Compose is Skia-backed, so the crop codec uses Skia directly - the same implementation shared
// with the JS and Wasm targets.

actual suspend fun cropToAspect(image: PickedImage, targetAspect: Float): PickedImage = runCatching {
    val decoded = Image.makeFromEncoded(image.bytes)
    skiaCrop(image, centerCropRect(decoded.width, decoded.height, targetAspect))
}.getOrDefault(image)

actual fun decodeImageSize(bytes: ByteArray): ImageSize? = runCatching {
    val image = Image.makeFromEncoded(bytes)
    ImageSize(image.width, image.height)
}.getOrNull()

actual suspend fun cropToRect(image: PickedImage, rect: PixelRect): PickedImage = skiaCrop(image, rect)

private fun skiaCrop(image: PickedImage, rect: PixelRect): PickedImage = runCatching {
    val source = Image.makeFromEncoded(image.bytes)
    val safe = rect.clampedTo(source.width, source.height) ?: return image
    val surface = Surface.makeRasterN32Premul(safe.width, safe.height)
    surface.canvas.drawImageRect(
        source,
        Rect.makeXYWH(safe.x.toFloat(), safe.y.toFloat(), safe.width.toFloat(), safe.height.toFloat()),
        Rect.makeWH(safe.width.toFloat(), safe.height.toFloat()),
    )
    val data = surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG) ?: return image
    PickedImage(bytes = data.bytes, mimeType = "image/png")
}.getOrDefault(image)
