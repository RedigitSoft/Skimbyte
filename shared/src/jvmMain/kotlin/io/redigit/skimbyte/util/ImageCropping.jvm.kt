package io.redigit.skimbyte.util

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual suspend fun cropToAspect(image: PickedImage, targetAspect: Float): PickedImage = runCatching {
    val source = ImageIO.read(ByteArrayInputStream(image.bytes)) ?: return image
    if (source.width <= 0 || source.height <= 0) return image
    cropBufferedImage(source, centerCropRect(source.width, source.height, targetAspect))
}.getOrDefault(image)

actual fun decodeImageSize(bytes: ByteArray): ImageSize? = runCatching {
    val source = ImageIO.read(ByteArrayInputStream(bytes)) ?: return null
    if (source.width > 0 && source.height > 0) ImageSize(source.width, source.height) else null
}.getOrNull()

actual suspend fun cropToRect(image: PickedImage, rect: PixelRect): PickedImage = runCatching {
    val source = ImageIO.read(ByteArrayInputStream(image.bytes)) ?: return image
    val safe = rect.clampedTo(source.width, source.height) ?: return image
    cropBufferedImage(source, safe)
}.getOrDefault(image)

// Copy into a fresh image (rather than getSubimage, which shares the source raster) so the encoded
// PNG contains only the cropped region.
private fun cropBufferedImage(source: BufferedImage, rect: PixelRect): PickedImage {
    val cropped = BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB)
    val g = cropped.createGraphics()
    g.drawImage(source, -rect.x, -rect.y, null)
    g.dispose()
    val out = ByteArrayOutputStream()
    ImageIO.write(cropped, "png", out)
    return PickedImage(bytes = out.toByteArray(), mimeType = "image/png")
}
