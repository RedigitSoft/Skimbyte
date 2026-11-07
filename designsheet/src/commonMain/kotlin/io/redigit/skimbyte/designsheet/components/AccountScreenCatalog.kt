package io.redigit.skimbyte.designsheet.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.ScreenFrame
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.screens.auth.AccountScreen
import io.redigit.skimbyte.ui.screens.auth.AccountState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AccountScreenCatalogContent() {
    CatalogPage {
        ScreenFrame {
            AccountScreen(
                state = AccountState(
                    isLoggedIn = true,
                    displayName = "Alex Rivera",
                    handle = "@alexrivera",
                    bio = "Staff Android & Systems Engineer. Writing KMP, Rust microservices, and local LLMs.",
                    isVerified = true,
                    savedCount = 18,
                    followedTopics = Topic.selectable.take(5),
                ),
                onIntent = {},
                onSignInClick = {},
                onSettingsClick = {},
                onAvatarClick = {},
                onAvatarEditClick = {},
                onBannerClick = {},
                onBannerEditClick = {},
                modifier = Modifier.height(560.dp),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AccountScreenCatalogPreview() {
    SkimbyteTheme {
        AccountScreenCatalogContent()
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AccountScreenCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        AccountScreenCatalogContent()
    }
}
