package io.redigit.skimbyte.designsheet.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.AvatarWithEditBadge
import io.redigit.skimbyte.ui.components.BannerImage
import io.redigit.skimbyte.ui.components.ImageUploadState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun StateLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProfileImagesCatalogContent() {
    CatalogPage {
        CatalogSection(
            title = "Avatar",
            description = "The edit badge overlays a progress or error indicator while an upload is in flight.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StateLabel("Idle, no image")
                AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Idle, onAvatarClick = {}, onEditClick = {})
                StateLabel("Idle, with image")
                AvatarWithEditBadge(
                    avatarUrl = "https://picsum.photos/seed/skimbyte-avatar/200",
                    uploadState = ImageUploadState.Idle,
                    onAvatarClick = {},
                    onEditClick = {},
                )
                StateLabel("Compressing")
                AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Compressing, onAvatarClick = {}, onEditClick = {})
                StateLabel("Uploading")
                AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Uploading, onAvatarClick = {}, onEditClick = {})
                StateLabel("Error")
                AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Error, onAvatarClick = {}, onEditClick = {})
            }
        }

        CatalogSection(title = "Banner") {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StateLabel("Idle, no image")
                BannerImage(bannerUrl = null, uploadState = ImageUploadState.Idle, onBannerClick = {}, onEditClick = {})
                StateLabel("Idle, with image")
                BannerImage(
                    bannerUrl = "https://picsum.photos/seed/skimbyte-banner/800/300",
                    uploadState = ImageUploadState.Idle,
                    onBannerClick = {},
                    onEditClick = {},
                )
                StateLabel("Uploading")
                BannerImage(bannerUrl = null, uploadState = ImageUploadState.Uploading, onBannerClick = {}, onEditClick = {})
                StateLabel("Error")
                BannerImage(bannerUrl = null, uploadState = ImageUploadState.Error, onBannerClick = {}, onEditClick = {})
            }
        }
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun ProfileImagesCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        ProfileImagesCatalogContent()
    }
}
