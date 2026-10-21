package io.redigit.skimbyte.util

import androidx.compose.runtime.Composable

data class PickedImage(val bytes: ByteArray, val mimeType: String)

/**
 * Returns a zero-arg launch function; calling it opens the platform's native image picker.
 * [onResult] receives the picked image, or null if the user cancelled.
 */
@Composable
expect fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit
