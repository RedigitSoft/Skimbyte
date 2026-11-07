package io.redigit.skimbyte.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.redigit.skimbyte.theme.SkimbyteIcons
import io.redigit.skimbyte.ui.SkimbyteStrings
import io.redigit.skimbyte.ui.components.SharedElementContext
import io.redigit.skimbyte.theme.SkimbyteTheme

/** Fixed shared-element key: only ever one current-user banner on screen at a time. */
const val BANNER_SHARED_ELEMENT_KEY = "banner-image"

/**
 * Full-width banner. Tapping the banner itself (when not uploading) opens a full-screen viewer
 * via [onBannerClick]; a small photo-camera badge opens the picker directly via [onEditClick].
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BannerImage(
    bannerUrl: String?,
    uploadState: ImageUploadState,
    onBannerClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    // False while signed out: the banner is purely decorative — no viewer tap target, no
    // camera badge (there is no image to view and nowhere to upload one to).
    editable: Boolean = true,
    sharedTransition: SharedElementContext? = null,
) {
    val busy = uploadState.isBusy
    val bannerModifier = if (sharedTransition != null) {
        with(sharedTransition.scope) {
            Modifier.sharedElement(
                rememberSharedContentState(key = BANNER_SHARED_ELEMENT_KEY),
                sharedTransition.animatedVisibilityScope,
            )
        }
    } else {
        Modifier
    }

    val bannerShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
    Box(modifier = modifier.fillMaxWidth().height(180.dp)) {
        Box(
            modifier = bannerModifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(bannerShape)
                // Brand-wash gradient instead of a flat void while there's no banner image.
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                            MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ),
                )
                .then(
                    if (editable) {
                        Modifier
                            .clickable(enabled = !busy, onClick = onBannerClick)
                            .semantics { contentDescription = SkimbyteStrings.Account.VIEW_BANNER }
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (bannerUrl != null) {
                AsyncImage(
                    model = bannerUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                )
            }
            if (busy) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator()
                    uploadState.caption()?.let {
                        Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        if (editable) {
            val isError = uploadState == ImageUploadState.Error
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(36.dp)
                    .background(if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clickable(enabled = !busy, onClick = onEditClick)
                    .semantics { contentDescription = SkimbyteStrings.Account.CHANGE_BANNER },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    busy -> CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    isError -> Icon(
                        SkimbyteIcons.Error,
                        contentDescription = SkimbyteStrings.Common.UPLOAD_FAILED,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onError,
                    )
                    else -> Icon(
                        SkimbyteIcons.Camera,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BannerImageNoBannerPreview() {
    SkimbyteTheme { BannerImage(bannerUrl = null, uploadState = ImageUploadState.Idle, onBannerClick = {}, onEditClick = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BannerImageNoBannerLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        BannerImage(bannerUrl = null, uploadState = ImageUploadState.Idle, onBannerClick = {}, onEditClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BannerImageUploadingPreview() {
    SkimbyteTheme { BannerImage(bannerUrl = null, uploadState = ImageUploadState.Uploading, onBannerClick = {}, onEditClick = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BannerImageErrorPreview() {
    SkimbyteTheme { BannerImage(bannerUrl = null, uploadState = ImageUploadState.Error, onBannerClick = {}, onEditClick = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BannerImageWithImagePreview() {
    SkimbyteTheme {
        BannerImage(
            bannerUrl = "https://picsum.photos/seed/skimbyte-banner/800/300",
            uploadState = ImageUploadState.Idle,
            onBannerClick = {},
            onEditClick = {},
        )
    }
}
