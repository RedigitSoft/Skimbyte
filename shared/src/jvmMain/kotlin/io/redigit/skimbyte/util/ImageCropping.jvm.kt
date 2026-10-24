package io.redigit.skimbyte.util

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual suspend fun cropToAspect(image: PickedImage, targetAspect: Float): PickedImage = runCatching {
    val source = ImageIO.read(ByteArrayInputStream(image.bytes)) ?: return image
    if (source.width <= 0 || source.height <= 0) return image
    val rect = centerCropRect(source.width, source.height, targetAspect)
    // Copy into a fresh image (rather than getSubimage, which shares the source raster) so the
    // encoded PNG contains only the cropped region.
    val cropped = BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB)
    val g = cropped.createGraphics()
    g.drawImage(source, -rect.x, -rect.y, null)
    g.dispose()
    val out = ByteArrayOutputStream()
    ImageIO.write(cropped, "png", out)
    PickedImage(bytes = out.toByteArray(), mimeType = "image/png")
}.getOrDefault(image)
