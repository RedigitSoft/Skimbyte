package io.redigit.skimbyte.util

import androidx.compose.runtime.Composable

/**
 * Whether this device can capture a photo from a camera — checked before offering the "Take photo"
 * option so it never appears where it can't work (a desktop with no webcam, say). Gallery/file
 * picking is always available and is offered regardless.
 */
@Composable
expect fun isCameraAvailable(): Boolean

/**
 * Returns a zero-arg launch function; calling it opens the camera to capture a photo. [onResult]
 * receives the captured image, or null if the user cancelled or capture failed. Mirrors
 * [rememberImagePickerLauncher] so the edit sheet treats camera and gallery the same way.
 */
@Composable
expect fun rememberCameraLauncher(onResult: (PickedImage?) -> Unit): () -> Unit
