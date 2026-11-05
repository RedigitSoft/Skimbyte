package io.redigit.skimbyte.util

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

@Composable
actual fun isCameraAvailable(): Boolean {
    val context = LocalContext.current
    return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}

@Composable
actual fun rememberCameraLauncher(onResult: (PickedImage?) -> Unit): () -> Unit {
    // TakePicturePreview hands back a Bitmap via the system camera app — no FileProvider, no CAMERA
    // permission. It's a reduced-size image, which is fine: the crop + compressForUpload step
    // downsizes to the 2 MB cap anyway.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        onResult(bitmap?.toJpegPickedImage())
    }
    return { launcher.launch(null) }
}

private fun Bitmap.toJpegPickedImage(): PickedImage {
    val out = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 95, out)
    return PickedImage(out.toByteArray(), "image/jpeg")
}
