package io.redigit.skimbyte.util

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@Composable
actual fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit {
    return {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.onchange = { _: Event ->
            val file: File? = input.files?.item(0)
            if (file == null) {
                onResult(null)
            } else {
                val reader = FileReader()
                reader.onload = { _: Event ->
                    val buffer = reader.result as ArrayBuffer
                    val view = DataView(buffer)
                    val bytes = ByteArray(buffer.byteLength) { i -> view.getInt8(i) }
                    onResult(PickedImage(bytes, file.type))
                }
                reader.readAsArrayBuffer(file)
            }
        }
        input.click()
    }
}
