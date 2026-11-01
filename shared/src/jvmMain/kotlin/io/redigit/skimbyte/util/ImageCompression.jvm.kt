package io.redigit.skimbyte.util

import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

actual suspend fun compressForUpload(image: PickedImage, maxBytes: Int): PickedImage = runCatching {
    if (image.bytes.size <= maxBytes) return image
    var source = ImageIO.read(ByteArrayInputStream(image.bytes)) ?: return image
    var smallest: ByteArray? = null
    while (true) {
        for (quality in UPLOAD_JPEG_QUALITIES) {
            val encoded = encodeJpeg(source, quality / 100f)
            if (encoded.size <= maxBytes) return PickedImage(encoded, "image/jpeg")
            smallest = encoded
        }
        val width = (source.width * UPLOAD_DOWNSCALE_FACTOR).toInt()
        val height = (source.height * UPLOAD_DOWNSCALE_FACTOR).toInt()
        if (width < UPLOAD_MIN_DIMENSION || height < UPLOAD_MIN_DIMENSION) break
        source = source.scaledTo(width, height)
    }
    smallest?.let { PickedImage(it, "image/jpeg") } ?: image
}.getOrDefault(image)

// JPEG has no alpha channel; flatten onto white before encoding so transparent PNG crops don't turn
// black.
private fun encodeJpeg(image: BufferedImage, quality: Float): ByteArray {
    val rgb = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    rgb.createGraphics().apply {
        color = Color.WHITE
        fillRect(0, 0, image.width, image.height)
        drawImage(image, 0, 0, null)
        dispose()
    }
    val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
    val params = writer.defaultWriteParam.apply {
        compressionMode = ImageWriteParam.MODE_EXPLICIT
        compressionQuality = quality
    }
    val out = ByteArrayOutputStream()
    ImageIO.createImageOutputStream(out).use { stream ->
        writer.output = stream
        writer.write(null, IIOImage(rgb, null, null), params)
    }
    writer.dispose()
    return out.toByteArray()
}

private fun BufferedImage.scaledTo(width: Int, height: Int): BufferedImage {
    val scaled = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    scaled.createGraphics().apply {
        setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        drawImage(this@scaledTo, 0, 0, width, height, null)
        dispose()
    }
    return scaled
}
