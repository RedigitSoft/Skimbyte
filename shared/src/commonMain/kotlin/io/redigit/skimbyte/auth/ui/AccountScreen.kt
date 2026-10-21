package io.redigit.skimbyte.auth.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.feed.ui.SharedElementContext
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.util.rememberImagePickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AccountScreen(
    uiState: AccountUiState,
    onLogout: () -> Unit,
    onLoginSucceeded: () -> Unit,
    onSettingsClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onAvatarEditClick: () -> Unit,
    onBannerClick: () -> Unit,
    onBannerEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarSharedTransition: SharedElementContext? = null,
    bannerSharedTransition: SharedElementContext? = null,
) {
    if (!uiState.isLoggedIn) {
        LoginSheet(onDismiss = {}, onLoginSuccess = onLoginSucceeded)
    }
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            BannerImage(
                bannerUrl = uiState.bannerUrl,
                isUploading = uiState.bannerUploadInProgress,
                onBannerClick = onBannerClick,
                onEditClick = onBannerEditClick,
                sharedTransition = bannerSharedTransition,
            )
            Box(modifier = Modifier.fillMaxWidth().offset(y = (-48).dp), contentAlignment = Alignment.CenterStart) {
                AvatarWithEditBadge(
                    avatarUrl = uiState.avatarUrl,
                    isUploading = uiState.avatarUploadInProgress,
                    onAvatarClick = onAvatarClick,
                    onEditClick = onAvatarEditClick,
                    modifier = Modifier.padding(start = 24.dp),
                    sharedTransition = avatarSharedTransition,
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (uiState.isLoggedIn) {
                        Text("You're logged in", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = onLogout) { Text("Log out") }
                    } else {
                        Text("Log in to manage your account", style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AccountRoute(
    onSettingsClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onBannerClick: () -> Unit,
    viewModel: AccountViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
    avatarSharedTransition: SharedElementContext? = null,
    bannerSharedTransition: SharedElementContext? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    val pickAvatar = rememberImagePickerLauncher { image -> image?.let(viewModel::onAvatarPicked) }
    val pickBanner = rememberImagePickerLauncher { image -> image?.let(viewModel::onBannerPicked) }
    AccountScreen(
        uiState = uiState,
        onLogout = viewModel::onLogout,
        onLoginSucceeded = {},
        onSettingsClick = onSettingsClick,
        onAvatarClick = onAvatarClick,
        onAvatarEditClick = pickAvatar,
        onBannerClick = onBannerClick,
        onBannerEditClick = pickBanner,
        modifier = modifier,
        avatarSharedTransition = avatarSharedTransition,
        bannerSharedTransition = bannerSharedTransition,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AccountScreenLoggedOutPreview() {
    SkimbyteTheme {
        AccountScreen(
            uiState = AccountUiState(isLoggedIn = false),
            onLogout = {},
            onLoginSucceeded = {},
            onSettingsClick = {},
            onAvatarClick = {},
            onAvatarEditClick = {},
            onBannerClick = {},
            onBannerEditClick = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AccountScreenLoggedInPreview() {
    SkimbyteTheme {
        AccountScreen(
            uiState = AccountUiState(isLoggedIn = true),
            onLogout = {},
            onLoginSucceeded = {},
            onSettingsClick = {},
            onAvatarClick = {},
            onAvatarEditClick = {},
            onBannerClick = {},
            onBannerEditClick = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AccountScreenLoggedInLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        AccountScreen(
            uiState = AccountUiState(isLoggedIn = true),
            onLogout = {},
            onLoginSucceeded = {},
            onSettingsClick = {},
            onAvatarClick = {},
            onAvatarEditClick = {},
            onBannerClick = {},
            onBannerEditClick = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AccountScreenWithImagesPreview() {
    SkimbyteTheme {
        AccountScreen(
            uiState = AccountUiState(
                isLoggedIn = true,
                avatarUrl = "https://picsum.photos/seed/skimbyte-avatar/200",
                bannerUrl = "https://picsum.photos/seed/skimbyte-banner/800/300",
            ),
            onLogout = {},
            onLoginSucceeded = {},
            onSettingsClick = {},
            onAvatarClick = {},
            onAvatarEditClick = {},
            onBannerClick = {},
            onBannerEditClick = {},
        )
    }
}
