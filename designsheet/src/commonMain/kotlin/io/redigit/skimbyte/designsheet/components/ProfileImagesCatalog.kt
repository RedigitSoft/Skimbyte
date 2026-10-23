package io.redigit.skimbyte.designsheet.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import io.redigit.skimbyte.ui.components.AvatarWithEditBadge
import io.redigit.skimbyte.ui.components.BannerImage

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProfileImagesCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        AvatarWithEditBadge(avatarUrl = null, isUploading = false, onAvatarClick = {}, onEditClick = {})
        AvatarWithEditBadge(
            avatarUrl = "https://picsum.photos/seed/skimbyte-avatar/200",
            isUploading = false,
            onAvatarClick = {},
            onEditClick = {},
        )
        AvatarWithEditBadge(avatarUrl = null, isUploading = true, onAvatarClick = {}, onEditClick = {})
        BannerImage(bannerUrl = null, isUploading = false, onBannerClick = {}, onEditClick = {})
        BannerImage(
            bannerUrl = "https://picsum.photos/seed/skimbyte-banner/800/300",
            isUploading = false,
            onBannerClick = {},
            onEditClick = {},
        )
        BannerImage(bannerUrl = null, isUploading = true, onBannerClick = {}, onEditClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun ProfileImagesCatalogPreview() {
    SkimbyteTheme {
        ProfileImagesCatalogContent()
    }
}
