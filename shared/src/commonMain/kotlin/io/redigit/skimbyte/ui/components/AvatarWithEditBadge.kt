package io.redigit.skimbyte.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
    isUploading: Boolean,
    onAvatarClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransition: SharedElementContext? = null,
) {
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
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = !isUploading, onClick = onAvatarClick)
                .semantics { contentDescription = "View avatar" },
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
                    Icons.Filled.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .clickable(enabled = !isUploading, onClick = onEditClick)
                .semantics { contentDescription = "Change avatar" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AvatarWithEditBadgeNoAvatarPreview() {
    SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, isUploading = false, onAvatarClick = {}, onEditClick = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AvatarWithEditBadgeNoAvatarLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        AvatarWithEditBadge(avatarUrl = null, isUploading = false, onAvatarClick = {}, onEditClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AvatarWithEditBadgeUploadingPreview() {
    SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, isUploading = true, onAvatarClick = {}, onEditClick = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AvatarWithEditBadgeWithImagePreview() {
    SkimbyteTheme {
        AvatarWithEditBadge(
            avatarUrl = "https://picsum.photos/seed/skimbyte-avatar/200",
            isUploading = false,
            onAvatarClick = {},
            onEditClick = {},
        )
    }
}
