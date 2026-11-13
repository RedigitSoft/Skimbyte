package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.models.dto.MeResponseDto
import io.redigit.skimbyte.data.remote.AuthApi
import io.redigit.skimbyte.util.PickedImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * End-to-end client pipeline for a profile/banner image upload: a large picked image flows through
 * [ProfileRepositoryImpl] into [AuthApi.uploadImage]. Verifies the bytes handed to the API are
 * compressed to JPEG within the 2 MB cap — i.e. that the running app sends a small image, not the
 * raw multi-megabyte crop. (The desktop app's own picker is a native file dialog that can't be
 * driven headlessly, so this exercises the same code path the UI invokes.)
 */
class ProfileUploadPipelineJvmTest {

    private class CapturingAuthApi : AuthApi {
        var uploadedType: String? = null
        var uploaded: PickedImage? = null
        override suspend fun login(email: String, password: String) = error("unused")
        override suspend fun register(email: String, password: String) = error("unused")
        override suspend fun refresh(refreshToken: String) = error("unused")
        override suspend fun logout(refreshToken: String) = Unit
        override suspend fun me(): MeResponseDto = MeResponseDto("u1", avatarUrl = "avatars/u1/a.jpg")
        override suspend fun uploadImage(type: String, image: PickedImage) {
            uploadedType = type
            uploaded = image
        }
    }

    private fun noisyPng(width: Int, height: Int): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val random = Random(7)
        for (y in 0 until height) {
            for (x in 0 until width) image.setRGB(x, y, random.nextInt())
        }
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "png", out)
        return out.toByteArray()
    }

    @Test
    fun uploadAvatar_sendsCompressedJpegUnderTheCap() = runTest(UnconfinedTestDispatcher()) {
        val api = CapturingAuthApi()
        val repo = ProfileRepositoryImpl(api, MutableStateFlow(Session("token")), backgroundScope)
        advanceUntilIdle()

        val cap = 2 * 1024 * 1024
        val rawCrop = noisyPng(2000, 2000)
        assertTrue(rawCrop.size > cap, "precondition: the raw crop (${rawCrop.size} B) should exceed the cap")

        val result = repo.uploadAvatar(PickedImage(rawCrop, "image/png"))
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        val sent = assertNotNull(api.uploaded, "the API should have received an image")
        assertEquals("avatar", api.uploadedType)
        assertEquals("image/jpeg", sent.mimeType)
        assertTrue(sent.bytes.size <= cap, "sent ${sent.bytes.size} B should be <= $cap B")
        // JPEG start-of-image marker, confirming it really was re-encoded (not the original PNG).
        assertEquals(0xFF.toByte(), sent.bytes[0])
        assertEquals(0xD8.toByte(), sent.bytes[1])
    }

    @Test
    fun uploadBanner_sendsCompressedJpegUnderTheCap() = runTest(UnconfinedTestDispatcher()) {
        val api = CapturingAuthApi()
        val repo = ProfileRepositoryImpl(api, MutableStateFlow(Session("token")), backgroundScope)
        advanceUntilIdle()

        val result = repo.uploadBanner(PickedImage(noisyPng(2400, 1000), "image/png"))
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        val sent = assertNotNull(api.uploaded)
        assertEquals("banner", api.uploadedType)
        assertEquals("image/jpeg", sent.mimeType)
        assertTrue(sent.bytes.size <= 2 * 1024 * 1024)
    }
}
