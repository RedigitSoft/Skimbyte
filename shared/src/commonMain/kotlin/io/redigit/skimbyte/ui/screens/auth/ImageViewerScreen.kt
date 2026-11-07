package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.redigit.skimbyte.theme.SkimbyteIcons
import io.redigit.skimbyte.ui.SkimbyteStrings
import io.redigit.skimbyte.ui.components.SharedElementContext
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.AVATAR_SHARED_ELEMENT_KEY

/**
 * Full-screen view of the current avatar or banner, reached by tapping either on [AccountScreen].
 * Shares a [sharedTransition] element keyed by [sharedElementKey] with the small thumbnail it
 * expanded from (see [AVATAR_SHARED_ELEMENT_KEY]/[BANNER_SHARED_ELEMENT_KEY]).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ImageViewerScreen(
    imageUrl: String?,
    sharedElementKey: String,
    onBack: () -> Unit,
    onChangePhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransition: SharedElementContext? = null,
) {
    val imageModifier = if (sharedTransition != null) {
        with(sharedTransition.scope) {
            Modifier.fillMaxSize().sharedElement(
                rememberSharedContentState(key = sharedElementKey),
                sharedTransition.animatedVisibilityScope,
            )
        }
    } else {
        Modifier.fillMaxSize()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(SkimbyteIcons.Back, contentDescription = SkimbyteStrings.Common.BACK)
                    }
                },
                expandedHeight = 48.dp,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onChangePhotoClick) {
                Icon(SkimbyteIcons.Camera, contentDescription = null)
                Text("  " + SkimbyteStrings.Account.CHANGE_PHOTO)
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            if (imageUrl != null) {
                AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Fit, modifier = imageModifier)
            } else {
                Icon(
                    SkimbyteIcons.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun ImageViewerScreenWithImagePreview() {
    SkimbyteTheme {
        ImageViewerScreen(
            imageUrl = "https://picsum.photos/seed/skimbyte-avatar/400",
            sharedElementKey = AVATAR_SHARED_ELEMENT_KEY,
            onBack = {},
            onChangePhotoClick = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun ImageViewerScreenNoImagePreview() {
    SkimbyteTheme {
        ImageViewerScreen(
            imageUrl = null,
            sharedElementKey = AVATAR_SHARED_ELEMENT_KEY,
            onBack = {},
            onChangePhotoClick = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun ImageViewerScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        ImageViewerScreen(
            imageUrl = "https://picsum.photos/seed/skimbyte-avatar/400",
            sharedElementKey = AVATAR_SHARED_ELEMENT_KEY,
            onBack = {},
            onChangePhotoClick = {},
        )
    }
}
