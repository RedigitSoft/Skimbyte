package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.ui.components.SharedElementContext
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.AvatarWithEditBadge
import io.redigit.skimbyte.ui.components.BannerImage
import io.redigit.skimbyte.ui.components.StatusPrompt
import io.redigit.skimbyte.ui.components.StatusPromptAction
import io.redigit.skimbyte.ui.components.TopicChip
import io.redigit.skimbyte.util.rememberImagePickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    state: AccountState,
    onIntent: (AccountIntent) -> Unit,
    onSignInClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onAvatarEditClick: () -> Unit,
    onBannerClick: () -> Unit,
    onBannerEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarSharedTransition: SharedElementContext? = null,
    bannerSharedTransition: SharedElementContext? = null,
) {

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onIntent(AccountIntent.Refresh) },
        modifier = modifier.fillMaxSize(),
    ) {
    Column(
        // The shell no longer consumes the top inset (see MainShell); keep this screen's banner
        // below the status bar.
        modifier = Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            BannerImage(
                bannerUrl = state.bannerUrl,
                uploadState = state.bannerUpload,
                onBannerClick = onBannerClick,
                onEditClick = onBannerEditClick,
                // Signed out there's nothing to view or upload — the header is decorative.
                editable = state.isLoggedIn,
                sharedTransition = bannerSharedTransition,
            )
            Box(modifier = Modifier.fillMaxWidth().offset(y = (-60).dp), contentAlignment = Alignment.CenterStart) {
                AvatarWithEditBadge(
                    avatarUrl = state.avatarUrl,
                    uploadState = state.avatarUpload,
                    onAvatarClick = onAvatarClick,
                    onEditClick = onAvatarEditClick,
                    modifier = Modifier.padding(start = 24.dp),
                    editable = state.isLoggedIn,
                    sharedTransition = avatarSharedTransition,
                )
            }
        }

        Column(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.avatarError?.let { AccountErrorBanner(message = it) }
            state.bannerError?.let { AccountErrorBanner(message = it) }

            if (state.isLoggedIn) {
                // Until the server returns a display name, the (real, userId-derived) handle
                // stands in as the identity line rather than showing a fake name.
                (state.displayName ?: state.handle)?.let { identityName ->
                    ProfileIdentityHeader(
                        displayName = identityName,
                        handle = state.handle.takeIf { state.displayName != null },
                        email = state.email,
                        bio = state.bio,
                        isVerified = state.isVerified,
                    )
                }
                state.savedCount?.let { SavedStat(savedCount = it) }
                if (state.followedTopics.isNotEmpty()) {
                    FollowedTopicsSection(topics = state.followedTopics)
                }
            } else {
                StatusPrompt(
                    icon = Icons.Filled.AccountCircle,
                    title = "You're not signed in",
                    message = "Sign in to sync your profile, bookmarks, and reading stats across devices.",
                    action = StatusPromptAction("Sign in", onSignInClick),
                )
            }

            // Settings (theme, account actions, privacy) are reachable here; they stay available
            // signed out since they're device-local — only the profile itself needs an account.
            EyebrowText("Account", modifier = Modifier.padding(start = 4.dp))
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                AccountActionRow(
                    icon = Icons.Filled.Settings,
                    label = "Settings",
                    supporting = "Theme, account, privacy & more",
                    onClick = onSettingsClick,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    }
}

/** Display name (with optional verified badge), handle, email, and bio. */
@Composable
private fun ProfileIdentityHeader(
    displayName: String,
    handle: String?,
    email: String?,
    bio: String?,
    isVerified: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            if (isVerified) {
                Icon(
                    Icons.Filled.Verified,
                    contentDescription = "Verified",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        if (handle != null) {
            Text(handle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        if (email != null) {
            Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (bio != null) {
            Text(
                bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** The user's saved-post count — the one profile stat backed by real data (their bookmarks). */
@Composable
private fun SavedStat(savedCount: Int, modifier: Modifier = Modifier) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(savedCount.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            EyebrowText("Saved")
        }
    }
}

/** The user's followed topics, rendered as chips. Real data from topic preferences. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FollowedTopicsSection(topics: List<Topic>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EyebrowText("Followed topics", modifier = Modifier.padding(start = 4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            topics.forEach { topic ->
                // Read-only: this section lists what the user follows, it is not a filter. Shares
                // TopicChip with the feed's filter row so a topic reads the same in both places.
                TopicChip(label = topic.displayLabel)
            }
        }
    }
}

@Composable
private fun AccountActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    trailingValue: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = iconTint.copy(alpha = 0.14f)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.padding(8.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = tint)
            if (supporting != null) {
                Text(
                    supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailingValue != null) {
            Text(
                trailingValue,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
    var editing by remember { mutableStateOf<ImageEditTarget?>(null) }
    // Gate the sheet on dismissal (mirrors FeedScreen's pendingBookmarkAction gating) — with a
    // no-op onDismiss the sheet stays composed and "Not now" can't close it. rememberSaveable so
    // one dismissal holds across tab switches (MainShell keeps tabs in a SaveableStateProvider).
    var loginSheetDismissed by rememberSaveable { mutableStateOf(false) }
    if (!state.isLoggedIn && !loginSheetDismissed) {
        LoginSheet(onDismiss = { loginSheetDismissed = true }, onLoginSuccess = {})
    }
    AccountScreen(
        state = state,
        onIntent = viewModel::onIntent,
        // Re-arm the login sheet: the signed-out prompt must always be able to reopen it after
        // an earlier dismissal, or signing in becomes unreachable from this tab.
        onSignInClick = { loginSheetDismissed = false },
        onSettingsClick = onSettingsClick,
        onAvatarClick = onAvatarClick,
        onAvatarEditClick = { editing = ImageEditTarget.Avatar },
        onBannerClick = onBannerClick,
        onBannerEditClick = { editing = ImageEditTarget.Banner },
        modifier = modifier,
        avatarSharedTransition = avatarSharedTransition,
        bannerSharedTransition = bannerSharedTransition,
    )
    editing?.let { target ->
        val hasImage = when (target) {
            ImageEditTarget.Avatar -> state.avatarUrl != null
            ImageEditTarget.Banner -> state.bannerUrl != null
        }
        val pick = when (target) {
            ImageEditTarget.Avatar -> pickAvatar
            ImageEditTarget.Banner -> pickBanner
        }
        ImageEditSheet(
            onDismiss = { editing = null },
            // Camera capture isn't available yet (the ImagePicker is gallery-only), so "Take photo"
            // falls back to the gallery as a placeholder.
            onTakePhoto = { editing = null; pick() },
            onChooseFromGallery = { editing = null; pick() },
            onRemove = if (hasImage) {
                {
                    editing = null
                    viewModel.onIntent(
                        if (target == ImageEditTarget.Avatar) AccountIntent.AvatarRemoved else AccountIntent.BannerRemoved,
                    )
                }
            } else {
                null
            },
        )
    }
}

private enum class ImageEditTarget { Avatar, Banner }

private val richAccountState = AccountState(
    isLoggedIn = true,
    displayName = "Alex Rivera",
    handle = "@alexrivera",
    bio = "Staff Android & Systems Engineer. Writing KMP, Rust microservices, and local LLMs.",
    isVerified = true,
    savedCount = 18,
    followedTopics = Topic.selectable.take(5),
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AccountScreenLoggedOutPreview() {
    SkimbyteTheme {
        AccountScreen(
            state = AccountState(isLoggedIn = false),
            onIntent = {},
            onSignInClick = {},
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
            state = richAccountState,
            onIntent = {},
            onSignInClick = {},
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
            state = richAccountState,
            onIntent = {},
            onSignInClick = {},
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
            state = richAccountState.copy(
                avatarUrl = "https://picsum.photos/seed/skimbyte-avatar/200",
                bannerUrl = "https://picsum.photos/seed/skimbyte-banner/800/300",
            ),
            onIntent = {},
            onSignInClick = {},
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
            state = richAccountState.copy(avatarError = "Could not update avatar"),
            onIntent = {},
            onSignInClick = {},
            onSettingsClick = {},
            onAvatarClick = {},
            onAvatarEditClick = {},
            onBannerClick = {},
            onBannerEditClick = {},
        )
    }
}
