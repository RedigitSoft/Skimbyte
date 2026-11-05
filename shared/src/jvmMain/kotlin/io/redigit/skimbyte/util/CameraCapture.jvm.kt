package io.redigit.skimbyte.util

import androidx.compose.runtime.Composable

// Desktop has no portable webcam API (and this app bundles no capture library), so camera capture
// is reported unavailable and the "Take photo" option is hidden; the file picker still works.
@Composable
actual fun isCameraAvailable(): Boolean = false

@Composable
actual fun rememberCameraLauncher(onResult: (PickedImage?) -> Unit): () -> Unit = { onResult(null) }
