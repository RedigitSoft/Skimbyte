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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.avatarError?.let { AccountErrorBanner(message = it) }
            state.bannerError?.let { AccountErrorBanner(message = it) }

            if (state.isLoggedIn) {
                if (state.displayName != null) {
                    ProfileIdentityHeader(
                        displayName = state.displayName,
                        handle = state.handle,
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

                EyebrowText("Account", modifier = Modifier.padding(start = 4.dp))
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column {
                        AccountActionRow(
                            icon = Icons.Filled.Settings,
                            label = "Settings",
                            supporting = "Theme, display & preferences",
                            onClick = onSettingsClick,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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

/** Display name (with optional verified badge), handle, and bio. */
@Composable
private fun ProfileIdentityHeader(
    displayName: String,
    handle: String?,
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
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatCell(value = readCount?.toString() ?: "–", label = "Read", modifier = Modifier.weight(1f))
            VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            StatCell(value = savedCount?.toString() ?: "–", label = "Saved", modifier = Modifier.weight(1f))
            VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EyebrowText("Weekly reading velocity")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                reads.forEachIndexed { index, value ->
                    val fraction = value.toFloat() / maxReads
                    val isBusiest = value == maxReads && value > 0
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height((4 + fraction * 44).dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = if (isBusiest) 1f else 0.35f),
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
    followedTopics = Topic.entries.filter { it != Topic.All }.take(5),
)

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
private fun AccountScreenLoggedInPreview() {
    SkimbyteTheme {
        AccountScreen(
            state = richAccountState,
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
            state = richAccountState,
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
            state = richAccountState.copy(
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
            state = richAccountState.copy(avatarError = "Could not update avatar"),
            onIntent = {},
            onSettingsClick = {},
            onAvatarClick = {},
            onAvatarEditClick = {},
            onBannerClick = {},
            onBannerEditClick = {},
        )
    }
}
