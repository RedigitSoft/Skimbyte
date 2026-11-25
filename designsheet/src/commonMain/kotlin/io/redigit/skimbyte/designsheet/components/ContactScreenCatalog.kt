package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.ScreenFrame
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.screens.settings.ContactScreen

@Composable
fun ContactScreenCatalogContent() {
    CatalogPage {
        ScreenFrame {
            ContactScreen(
                onBack = {},
                onSendEmail = {},
                onWebsiteClick = {},
                onIssuesClick = {},
                modifier = Modifier.height(560.dp),
            )
        }
    }
}

@Preview
@Composable
private fun ContactScreenCatalogPreview() {
    SkimbyteTheme {
        ContactScreenCatalogContent()
    }
}

@Preview
@Composable
private fun ContactScreenCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        ContactScreenCatalogContent()
    }
}