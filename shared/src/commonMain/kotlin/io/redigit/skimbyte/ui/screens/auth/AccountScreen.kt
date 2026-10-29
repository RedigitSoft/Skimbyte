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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.clip
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
import io.redigit.skimbyte.ui.components.SignedOutPrompt
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
    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }

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
                isUploading = state.bannerUploadInProgress,
                onBannerClick = onBannerClick,
                onEditClick = onBannerEditClick,
                // Signed out there's nothing to view or upload — the header is decorative.
                editable = state.isLoggedIn,
                sharedTransition = bannerSharedTransition,
            )
            Box(modifier = Modifier.fillMaxWidth().offset(y = (-60).dp), contentAlignment = Alignment.CenterStart) {
                AvatarWithEditBadge(
                    avatarUrl = state.avatarUrl,
                    isUploading = state.avatarUploadInProgress,
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
                if (state.readCount != null || state.savedCount != null || state.streakDays != null) {
                    ProfileStatsRow(
                        readCount = state.readCount,
                        savedCount = state.savedCount,
                        streakDays = state.streakDays,
                    )
                }
                if (state.weeklyReads.isNotEmpty()) {
                    WeeklyVelocityChart(weeklyReads = state.weeklyReads)
                }
                if (state.followedTopics.isNotEmpty()) {
                    FollowedTopicsSection(topics = state.followedTopics)
                }
            } else {
                SignedOutPrompt(
                    icon = Icons.Filled.AccountCircle,
                    title = "You're not signed in",
                    message = "Sign in to sync your profile, bookmarks, and reading stats across devices.",
                    onSignInClick = onSignInClick,
                )
            }

            // Feed preferences and Settings are device-local and stay available signed out —
            // only the profile itself needs an account.
            EyebrowText("Feed & app config", modifier = Modifier.padding(start = 4.dp))
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                Column {
                    AccountActionRow(
                        icon = Icons.Outlined.Schedule,
                        label = "Daily digest time",
                        supporting = "Scheduled brief delivery",
                        trailingValue = state.digestTime,
                        onClick = { onIntent(AccountIntent.DigestTimeCycled) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    AccountActionRow(
                        icon = Icons.Filled.Notes,
                        label = "Summary length",
                        supporting = "Reading depth",
                        trailingValue = state.summaryLength.label,
                        onClick = { onIntent(AccountIntent.SummaryLengthCycled) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    AccountActionRow(
                        icon = Icons.Filled.Notifications,
                        label = "Notifications",
                        supporting = "Breaking releases & CVEs",
                        trailingValue = if (state.notificationsEnabled) "On" else "Off",
                        onClick = { onIntent(AccountIntent.NotificationsToggled(!state.notificationsEnabled)) },
                    )
                }
            }

            EyebrowText("Account", modifier = Modifier.padding(start = 4.dp))
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                Column {
                    AccountActionRow(
                        icon = Icons.Filled.Settings,
                        label = "Settings",
                        supporting = "Theme, display & preferences",
                        onClick = onSettingsClick,
                    )
                    if (state.isLoggedIn) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        AccountActionRow(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            label = "Log out",
                            supporting = "Sign out of your account",
                            onClick = { showLogoutConfirm = true },
                            tint = MaterialTheme.colorScheme.error,
                            iconTint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("logoutRow"),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
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

/** Three-up reading stats: read / saved / streak. */
@Composable
private fun ProfileStatsRow(
    readCount: Int?,
    savedCount: Int?,
    streakDays: Int?,
    modifier: Modifier = Modifier,
) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatCell(value = readCount?.toString() ?: "–", label = "Read", modifier = Modifier.weight(1f))
            VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.outlineVariant)
            StatCell(value = savedCount?.toString() ?: "–", label = "Saved", modifier = Modifier.weight(1f))
            VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.outlineVariant)
            StatCell(value = streakDays?.let { "${it}d" } ?: "–", label = "Streak", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        EyebrowText(label)
    }
}

/** A seven-day reading-velocity bar chart; the busiest day is accented. */
@Composable
private fun WeeklyVelocityChart(weeklyReads: List<Int>, modifier: Modifier = Modifier) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val reads = weeklyReads.take(7)
    val maxReads = (reads.maxOrNull() ?: 0).coerceAtLeast(1)
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EyebrowText("Weekly reading velocity")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                // Only the first busiest day gets the count label, so ties don't double-label.
                val busiestIndex = reads.indexOfFirst { it == maxReads && it > 0 }
                reads.forEachIndexed { index, value ->
                    val fraction = value.toFloat() / maxReads
                    val isBusiest = index == busiestIndex
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (isBusiest) {
                            Text(
                                value.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                // Minimum height keeps low-count days legible instead of
                                // rendering as a near-invisible sliver.
                                .height((8 + fraction * 56).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = if (isBusiest) 1f else 0.55f),
                                ),
                        )
                        Text(
                            dayLabels.getOrElse(index) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
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
                Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        text = topic.displayLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
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
    readCount = 142,
    savedCount = 18,
    streakDays = 14,
    weeklyReads = listOf(8, 12, 6, 14, 20, 9, 11),
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
