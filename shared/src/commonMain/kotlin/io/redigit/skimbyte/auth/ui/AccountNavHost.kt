package io.redigit.skimbyte.auth.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.redigit.skimbyte.feed.ui.SharedElementContext
import io.redigit.skimbyte.nav.AccountHome
import io.redigit.skimbyte.nav.AvatarViewer
import io.redigit.skimbyte.nav.BannerViewer
import io.redigit.skimbyte.nav.Settings
import io.redigit.skimbyte.nav.Topics
import io.redigit.skimbyte.onboarding.TopicsViewModel
import io.redigit.skimbyte.onboarding.ui.TopicsScreen
import io.redigit.skimbyte.settings.SettingsViewModel
import io.redigit.skimbyte.settings.ui.SettingsScreen
import io.redigit.skimbyte.util.rememberImagePickerLauncher
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AccountHome::class, AccountHome.serializer())
            subclass(Settings::class, Settings.serializer())
            subclass(Topics::class, Topics.serializer())
            subclass(AvatarViewer::class, AvatarViewer.serializer())
            subclass(BannerViewer::class, BannerViewer.serializer())
        }
    }
}

/**
 * Owns only this tab's Navigation 3 backstack and content - the bottom chrome (bottom nav bar /
 * rail) lives one level up in `MainShell`, which decides whether to show it from [onRootChanged]:
 * each NavHost reports whether it's currently showing its own root screen (backstack depth 1),
 * so `MainShell` can hide the shared bottom bar while any tab is showing a pushed detail screen.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AccountNavHost(onRootChanged: (Boolean) -> Unit) {
    val backStack = rememberNavBackStack(navConfig, AccountHome)
    LaunchedEffect(backStack.size) { onRootChanged(backStack.size == 1) }
    val accountViewModel = koinViewModel<AccountViewModel>()
    val uiState by accountViewModel.uiState.collectAsState()

    SharedTransitionLayout {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<AccountHome> {
                    val sharedTransition = SharedElementContext(this@SharedTransitionLayout, LocalNavAnimatedContentScope.current)
                    AccountRoute(
                        onSettingsClick = { backStack.add(Settings) },
                        onAvatarClick = { backStack.add(AvatarViewer) },
                        onBannerClick = { backStack.add(BannerViewer) },
                        viewModel = accountViewModel,
                        avatarSharedTransition = sharedTransition,
                        bannerSharedTransition = sharedTransition,
                    )
                }
                entry<AvatarViewer> {
                    val sharedTransition = SharedElementContext(this@SharedTransitionLayout, LocalNavAnimatedContentScope.current)
                    val pickAvatar = rememberImagePickerLauncher { image -> image?.let(accountViewModel::onAvatarPicked) }
                    ImageViewerScreen(
                        imageUrl = uiState.avatarUrl,
                        sharedElementKey = AVATAR_SHARED_ELEMENT_KEY,
                        onBack = { backStack.removeLastOrNull() },
                        onChangePhotoClick = pickAvatar,
                        sharedTransition = sharedTransition,
                    )
                }
                entry<BannerViewer> {
                    val sharedTransition = SharedElementContext(this@SharedTransitionLayout, LocalNavAnimatedContentScope.current)
                    val pickBanner = rememberImagePickerLauncher { image -> image?.let(accountViewModel::onBannerPicked) }
                    ImageViewerScreen(
                        imageUrl = uiState.bannerUrl,
                        sharedElementKey = BANNER_SHARED_ELEMENT_KEY,
                        onBack = { backStack.removeLastOrNull() },
                        onChangePhotoClick = pickBanner,
                        sharedTransition = sharedTransition,
                    )
                }
                entry<Settings> {
                    val viewModel = koinViewModel<SettingsViewModel>()
                    val settingsUiState by viewModel.uiState.collectAsState()
                    SettingsScreen(
                        uiState = settingsUiState,
                        onThemeModeSelected = viewModel::onThemeModeSelected,
                        onEditInterestsClick = { backStack.add(Topics) },
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<Topics> {
                    val viewModel = koinViewModel<TopicsViewModel> { parametersOf(true) }
                    val topicsUiState by viewModel.uiState.collectAsState()
                    TopicsScreen(
                        uiState = topicsUiState,
                        onTopicToggled = viewModel::onTopicToggled,
                        onConfirm = {
                            viewModel.onConfirm()
                            backStack.removeLastOrNull()
                        },
                    )
                }
            },
        )
    }
}
