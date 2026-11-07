package io.redigit.skimbyte.designsheet.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.screens.auth.ImageEditSheetContent

@Composable
fun ImageEditSheetCatalogContent() {
    CatalogPage {
        CatalogSection(title = "With a current image", description = "The remove action is available.") {
            ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = {})
        }

        CatalogSection(title = "Without a current image", description = "The remove action is hidden.") {
            ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = null)
        }
    }
}

@Preview
@Composable
private fun ImageEditSheetCatalogPreview() {
    SkimbyteTheme {
        ImageEditSheetCatalogContent()
    }
}

@Preview
@Composable
private fun ImageEditSheetCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        ImageEditSheetCatalogContent()
    }
}
