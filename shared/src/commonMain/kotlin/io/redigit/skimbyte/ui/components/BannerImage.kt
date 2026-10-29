package io.redigit.skimbyte.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
    isUploading: Boolean,
    onBannerClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    // False while signed out: the banner is purely decorative — no viewer tap target, no
    // camera badge (there is no image to view and nowhere to upload one to).
    editable: Boolean = true,
    sharedTransition: SharedElementContext? = null,
) {
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
                            .clickable(enabled = !isUploading, onClick = onBannerClick)
                            .semantics { contentDescription = "View banner" }
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
            if (isUploading) {
                CircularProgressIndicator()
            }
        }
        if (editable) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clickable(enabled = !isUploading, onClick = onEditClick)
                    .semantics { contentDescription = "Change banner" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BannerImageNoBannerPreview() {
    SkimbyteTheme { BannerImage(bannerUrl = null, isUploading = false, onBannerClick = {}, onEditClick = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BannerImageNoBannerLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        BannerImage(bannerUrl = null, isUploading = false, onBannerClick = {}, onEditClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BannerImageUploadingPreview() {
    SkimbyteTheme { BannerImage(bannerUrl = null, isUploading = true, onBannerClick = {}, onEditClick = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BannerImageWithImagePreview() {
    SkimbyteTheme {
        BannerImage(
            bannerUrl = "https://picsum.photos/seed/skimbyte-banner/800/300",
            isUploading = false,
            onBannerClick = {},
            onEditClick = {},
        )
    }
}
