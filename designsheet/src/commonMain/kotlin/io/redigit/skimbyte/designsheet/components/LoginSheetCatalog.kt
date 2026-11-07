package io.redigit.skimbyte.designsheet.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.screens.auth.LoginSheetContent
import io.redigit.skimbyte.ui.screens.auth.LoginSheetState

// Renders LoginSheetContent directly rather than LoginSheet (the ModalBottomSheet container),
// since the catalog doesn't need sheet/scrim machinery — just the content/container split
// established in ui/screens/auth/LoginSheet.kt.
@Composable
fun LoginSheetCatalogContent() {
    CatalogPage {
        CatalogSection(title = "Default") {
            LoginSheetContent(
                state = LoginSheetState(),
                onAppleSignInClick = {},
                onGithubSignInClick = {},
                onGoogleSignInClick = {},
                onNotNowClick = {},
            )
        }

        CatalogSection(
            title = "With error",
            description = "A failed provider round-trip surfaces inline; the sheet stays open for retry.",
        ) {
            LoginSheetContent(
                state = LoginSheetState(error = "Google sign-in failed"),
                onAppleSignInClick = {},
                onGithubSignInClick = {},
                onGoogleSignInClick = {},
                onNotNowClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun LoginSheetCatalogPreview() {
    SkimbyteTheme {
        LoginSheetCatalogContent()
    }
}

@Preview
@Composable
private fun LoginSheetCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        LoginSheetCatalogContent()
    }
}
