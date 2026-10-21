package io.redigit.skimbyte.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSItemProviderReadingProtocol
import platform.Foundation.getBytes
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
private class ImagePickerDelegate(private val onResult: (PickedImage?) -> Unit) :
    NSObject(), PHPickerViewControllerDelegateProtocol {

    @OptIn(BetaInteropApi::class)
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        val provider = result?.itemProvider
        val imageClass = UIImage.`class`() as NSItemProviderReadingProtocol
        if (provider == null || !provider.canLoadObjectOfClass(imageClass)) {
            onResult(null)
            return
        }
        provider.loadObjectOfClass(imageClass) { image, _ ->
            val uiImage = image as? UIImage
            val data = uiImage?.let { UIImagePNGRepresentation(it) }
            onResult(data?.toPickedImage())
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toPickedImage(): PickedImage {
    val bytes = ByteArray(length.toInt())
    bytes.usePinned { pinned ->
        getBytes(pinned.addressOf(0), length)
    }
    return PickedImage(bytes, "image/png")
}

@Composable
actual fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit {
    val delegate = remember(onResult) { ImagePickerDelegate(onResult) }
    return {
        val configuration = PHPickerConfiguration()
        configuration.filter = platform.PhotosUI.PHPickerFilter.imagesFilter
        val picker = PHPickerViewController(configuration = configuration)
        picker.delegate = delegate
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.presentViewController(picker, animated = true, completion = null)
    }
}
