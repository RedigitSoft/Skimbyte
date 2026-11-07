package io.redigit.skimbyte.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.redigit.skimbyte.theme.SkimbyteIcons
import io.redigit.skimbyte.ui.components.SharedElementContext
import io.redigit.skimbyte.theme.SkimbyteTheme

/** Fixed shared-element key: only ever one current-user avatar on screen at a time. */
const val AVATAR_SHARED_ELEMENT_KEY = "avatar-image"

/**
 * Circular avatar with a small photo-camera badge overlaid bottom-end. Tapping the avatar itself
 * (when not uploading) opens a full-screen viewer via [onAvatarClick]; tapping the badge opens the
 * image picker directly via [onEditClick] - two separate tap targets, matching the common
 * "tap to view, tap the pencil/camera to change" pattern.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AvatarWithEditBadge(
    avatarUrl: String?,
    uploadState: ImageUploadState,
    onAvatarClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    // False while signed out: the avatar is purely decorative — no viewer tap target, no
    // camera badge (there is no image to view and nowhere to upload one to).
    editable: Boolean = true,
    sharedTransition: SharedElementContext? = null,
) {
    val busy = uploadState.isBusy
    val avatarModifier = if (sharedTransition != null) {
        with(sharedTransition.scope) {
            Modifier.sharedElement(
                rememberSharedContentState(key = AVATAR_SHARED_ELEMENT_KEY),
                sharedTransition.animatedVisibilityScope,
            )
        }
    } else {
        Modifier
    }

    Box(modifier = modifier.size(124.dp)) {
        Box(
            modifier = avatarModifier
                .size(120.dp)
                // Surface ring separates the avatar from whatever banner it overlaps.
                .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (editable) {
                        Modifier
                            .clickable(enabled = !busy, onClick = onAvatarClick)
                            .semantics { contentDescription = "View avatar" }
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(120.dp).clip(CircleShape),
                )
            } else {
                Icon(
                    SkimbyteIcons.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (busy) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    uploadState.caption()?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        if (editable) {
            val isError = uploadState == ImageUploadState.Error
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clickable(enabled = !busy, onClick = onEditClick)
                    .semantics { contentDescription = "Change avatar" },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    busy -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    isError -> Icon(
                        SkimbyteIcons.Error,
                        contentDescription = "Upload failed",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onError,
                    )
                    else -> Icon(
                        SkimbyteIcons.Camera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
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
private fun AvatarWithEditBadgeNoAvatarPreview() {
    SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Idle, onAvatarClick = {}, onEditClick = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AvatarWithEditBadgeNoAvatarLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Idle, onAvatarClick = {}, onEditClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AvatarWithEditBadgeCompressingPreview() {
    SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Compressing, onAvatarClick = {}, onEditClick = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AvatarWithEditBadgeUploadingPreview() {
    SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Uploading, onAvatarClick = {}, onEditClick = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AvatarWithEditBadgeErrorPreview() {
    SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Error, onAvatarClick = {}, onEditClick = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AvatarWithEditBadgeWithImagePreview() {
    SkimbyteTheme {
        AvatarWithEditBadge(
            avatarUrl = "https://picsum.photos/seed/skimbyte-avatar/200",
            uploadState = ImageUploadState.Idle,
            onAvatarClick = {},
            onEditClick = {},
        )
    }
}
