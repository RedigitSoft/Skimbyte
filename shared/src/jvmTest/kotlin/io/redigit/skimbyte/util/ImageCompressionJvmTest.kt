package io.redigit.skimbyte.util

import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCompressionJvmTest {

    // Random-noise RGB barely compresses, so a large PNG of it reliably exceeds the upload cap —
    // a realistic stand-in for a high-resolution photo straight out of the cropper.
    private fun noisyPng(width: Int, height: Int): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val random = Random(42)
        for (y in 0 until height) {
            for (x in 0 until width) image.setRGB(x, y, random.nextInt())
        }
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "png", out)
        return out.toByteArray()
    }

    @Test
    fun compressesLargeImageUnderCap() = runBlocking {
        val cap = 2 * 1024 * 1024
        val png = noisyPng(2000, 2000)
        assertTrue(png.size > cap, "precondition: source (${png.size} B) should exceed the cap")

        val result = compressForUpload(PickedImage(png, "image/png"), cap)

        assertTrue(result.bytes.size <= cap, "compressed size ${result.bytes.size} B should be <= $cap B")
        assertEquals("image/jpeg", result.mimeType)
    }

    @Test
    fun recursesThroughMultipleDownscalesForAVeryTightCap() = runBlocking {
        // A 50 KB cap on a 2000x2000 noise image can't be met by lowering quality alone, so this
        // only passes if the compressor keeps downscaling and re-encoding across several cycles.
        val cap = 50 * 1024
        val png = noisyPng(2000, 2000)

        val result = compressForUpload(PickedImage(png, "image/png"), cap)

        assertTrue(result.bytes.size <= cap, "size ${result.bytes.size} B should be <= $cap B")
        assertEquals("image/jpeg", result.mimeType)
    }

    @Test
    fun leavesImageAlreadyUnderCapUnchanged() = runBlocking {
        val small = PickedImage(noisyPng(64, 64), "image/png")
        assertTrue(small.bytes.size <= 2 * 1024 * 1024)

        val result = compressForUpload(small, 2 * 1024 * 1024)

        assertEquals(small, result) // same instance returned, untouched
    }

    @Test
    fun returnsOriginalWhenBytesCannotBeDecoded() = runBlocking {
        val blob = PickedImage(ByteArray(3 * 1024 * 1024), "image/png") // zeros, not a real image

        val result = compressForUpload(blob, 2 * 1024 * 1024)

        assertEquals(blob, result) // undecodable → unchanged; the caller rejects it as still over-cap
    }
}
