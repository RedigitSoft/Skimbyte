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
import io.redigit.skimbyte.ui.components.ImageUploadState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProfileImagesCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Idle, onAvatarClick = {}, onEditClick = {})
        AvatarWithEditBadge(
            avatarUrl = "https://picsum.photos/seed/skimbyte-avatar/200",
            uploadState = ImageUploadState.Idle,
            onAvatarClick = {},
            onEditClick = {},
        )
        AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Compressing, onAvatarClick = {}, onEditClick = {})
        AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Uploading, onAvatarClick = {}, onEditClick = {})
        AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Error, onAvatarClick = {}, onEditClick = {})
        BannerImage(bannerUrl = null, uploadState = ImageUploadState.Idle, onBannerClick = {}, onEditClick = {})
        BannerImage(
            bannerUrl = "https://picsum.photos/seed/skimbyte-banner/800/300",
            uploadState = ImageUploadState.Idle,
            onBannerClick = {},
            onEditClick = {},
        )
        BannerImage(bannerUrl = null, uploadState = ImageUploadState.Uploading, onBannerClick = {}, onEditClick = {})
        BannerImage(bannerUrl = null, uploadState = ImageUploadState.Error, onBannerClick = {}, onEditClick = {})
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
