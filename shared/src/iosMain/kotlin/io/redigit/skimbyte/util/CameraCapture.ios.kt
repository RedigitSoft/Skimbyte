package io.redigit.skimbyte.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.getBytes
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
private class CameraDelegate(private val onResult: (PickedImage?) -> Unit) :
    NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val data = image?.let { UIImageJPEGRepresentation(it, 0.95) }
        onResult(data?.toJpegPickedImage())
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onResult(null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toJpegPickedImage(): PickedImage {
    val bytes = ByteArray(length.toInt())
    bytes.usePinned { getBytes(it.addressOf(0), length) }
    return PickedImage(bytes, "image/jpeg")
}

@Composable
actual fun isCameraAvailable(): Boolean =
    UIImagePickerController.isSourceTypeAvailable(
        UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
    )

@Composable
actual fun rememberCameraLauncher(onResult: (PickedImage?) -> Unit): () -> Unit {
    // Held across recompositions so the picker's weak delegate reference stays alive while open.
    val delegate = remember(onResult) { CameraDelegate(onResult) }
    return {
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        picker.delegate = delegate
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.presentViewController(picker, animated = true, completion = null)
    }
}
