package io.redigit.skimbyte.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Files

@Composable
actual fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    return {
        scope.launch(Dispatchers.IO) {
            val dialog = FileDialog(null as Frame?, "Choose an image", FileDialog.LOAD)
            dialog.isVisible = true
            val file = dialog.files.firstOrNull()
            val picked = file?.let {
                PickedImage(it.readBytes(), Files.probeContentType(it.toPath()) ?: "image/jpeg")
            }
            withContext(Dispatchers.Main) { onResult(picked) }
        }
    }
}
