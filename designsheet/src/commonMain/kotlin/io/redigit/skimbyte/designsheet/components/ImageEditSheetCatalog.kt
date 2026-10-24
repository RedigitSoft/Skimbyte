package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.screens.auth.ImageEditSheetContent

@Composable
fun ImageEditSheetCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // With a current image (remove available).
        ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = {})
        // Without a current image (remove hidden).
        ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = null)
    }
}

@Preview
@Composable
private fun ImageEditSheetCatalogPreview() {
    SkimbyteTheme {
        ImageEditSheetCatalogContent()
    }
}
