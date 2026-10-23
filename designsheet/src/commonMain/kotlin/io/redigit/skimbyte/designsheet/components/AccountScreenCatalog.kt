package io.redigit.skimbyte.designsheet.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.screens.auth.AccountScreen
import io.redigit.skimbyte.ui.screens.auth.AccountState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AccountScreenCatalogContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AccountScreen(
            state = AccountState(isLoggedIn = true),
            onIntent = {},
            onSettingsClick = {},
            onAvatarClick = {},
            onAvatarEditClick = {},
            onBannerClick = {},
            onBannerEditClick = {},
            modifier = Modifier.height(320.dp),
        )
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
