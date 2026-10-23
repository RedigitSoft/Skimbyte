package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.feed.ui.SharedElementContext
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.AvatarWithEditBadge
import io.redigit.skimbyte.ui.components.BannerImage
import io.redigit.skimbyte.util.rememberImagePickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AccountScreen(
    state: AccountState,
    onIntent: (AccountIntent) -> Unit,
    onSettingsClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onAvatarEditClick: () -> Unit,
    onBannerClick: () -> Unit,
    onBannerEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarSharedTransition: SharedElementContext? = null,
    bannerSharedTransition: SharedElementContext? = null,
) {
    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            BannerImage(
                bannerUrl = state.bannerUrl,
                isUploading = state.bannerUploadInProgress,
                onBannerClick = onBannerClick,
                onEditClick = onBannerEditClick,
                sharedTransition = bannerSharedTransition,
            )
            Box(modifier = Modifier.fillMaxWidth().offset(y = (-48).dp), contentAlignment = Alignment.CenterStart) {
                AvatarWithEditBadge(
                    avatarUrl = state.avatarUrl,
                    isUploading = state.avatarUploadInProgress,
                    onAvatarClick = onAvatarClick,
                    onEditClick = onAvatarEditClick,
                    modifier = Modifier.padding(start = 24.dp),
                    sharedTransition = avatarSharedTransition,
                )
            }
        }

        Column(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            state.avatarError?.let { AccountErrorBanner(message = it) }
            state.bannerError?.let { AccountErrorBanner(message = it) }

            if (state.isLoggedIn) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column {
                        AccountActionRow(icon = Icons.Filled.Settings, label = "Settings", onClick = onSettingsClick)
                        AccountActionRow(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            label = "Log out",
                            onClick = { showLogoutConfirm = true },
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("logoutRow"),
                        )
                    }
                }
            } else {
                AccountSignedOutState()
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("You'll need to sign in again to access your account.") },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("confirmLogout"),
                    onClick = {
                        showLogoutConfirm = false
                        onIntent(AccountIntent.Logout)
                    },
                ) { Text("Log out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun AccountActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Text(label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}

@Composable
private fun AccountErrorBanner(message: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AccountSignedOutState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Filled.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("You're not signed in", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Log in to manage your profile and settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
    val state by viewModel.state.collectAsState()
    val pickAvatar = rememberImagePickerLauncher { image -> image?.let { viewModel.onIntent(AccountIntent.AvatarPicked(it)) } }
    val pickBanner = rememberImagePickerLauncher { image -> image?.let { viewModel.onIntent(AccountIntent.BannerPicked(it)) } }
    if (!state.isLoggedIn) {
        LoginSheet(onDismiss = {}, onLoginSuccess = {})
    }
    AccountScreen(
        state = state,
        onIntent = viewModel::onIntent,
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
            state = AccountState(isLoggedIn = false),
            onIntent = {},
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
private fun AccountScreenLoggedOutLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        AccountScreen(
            state = AccountState(isLoggedIn = false),
            onIntent = {},
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
            state = AccountState(isLoggedIn = true),
            onIntent = {},
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
            state = AccountState(isLoggedIn = true),
            onIntent = {},
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
            state = AccountState(
                isLoggedIn = true,
                avatarUrl = "https://picsum.photos/seed/skimbyte-avatar/200",
                bannerUrl = "https://picsum.photos/seed/skimbyte-banner/800/300",
            ),
            onIntent = {},
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
private fun AccountScreenWithImagesLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        AccountScreen(
            state = AccountState(
                isLoggedIn = true,
                avatarUrl = "https://picsum.photos/seed/skimbyte-avatar/200",
                bannerUrl = "https://picsum.photos/seed/skimbyte-banner/800/300",
            ),
            onIntent = {},
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
private fun AccountScreenWithErrorPreview() {
    SkimbyteTheme {
        AccountScreen(
            state = AccountState(isLoggedIn = true, avatarError = "Could not update avatar"),
            onIntent = {},
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
private fun AccountScreenWithErrorLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        AccountScreen(
            state = AccountState(isLoggedIn = true, avatarError = "Could not update avatar"),
            onIntent = {},
            onSettingsClick = {},
            onAvatarClick = {},
            onAvatarEditClick = {},
            onBannerClick = {},
            onBannerEditClick = {},
        )
    }
}
