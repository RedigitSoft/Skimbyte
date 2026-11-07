package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.designsheet.catalog.ScreenFrame
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.screens.auth.ImageCropScreen
import io.redigit.skimbyte.util.AVATAR_ASPECT
import io.redigit.skimbyte.util.BANNER_ASPECT
import io.redigit.skimbyte.util.PickedImage

/**
 * The interactive cropper's chrome. The catalog has no decodable image bytes, so the frame shows
 * its placeholder surface; in the app the picked photo fills it and responds to drag/pinch.
 */
@Composable
fun ImageCropScreenCatalogContent() {
    val blank = PickedImage(bytes = ByteArray(0), mimeType = "image/png")
    CatalogPage {
        CatalogSection(title = "Avatar crop", description = "Square aspect with a circular mask.", framed = false) {
            ScreenFrame {
                ImageCropScreen(
                    image = blank,
                    aspect = AVATAR_ASPECT,
                    circularMask = true,
                    onCancel = {},
                    onConfirm = {},
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                )
            }
        }

        CatalogSection(title = "Banner crop", description = "Wide aspect, no mask.", framed = false) {
            ScreenFrame {
                ImageCropScreen(
                    image = blank,
                    aspect = BANNER_ASPECT,
                    circularMask = false,
                    onCancel = {},
                    onConfirm = {},
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ImageCropScreenCatalogPreview() {
    SkimbyteTheme {
        ImageCropScreenCatalogContent()
    }
}

@Preview
@Composable
private fun ImageCropScreenCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        ImageCropScreenCatalogContent()
    }
}
