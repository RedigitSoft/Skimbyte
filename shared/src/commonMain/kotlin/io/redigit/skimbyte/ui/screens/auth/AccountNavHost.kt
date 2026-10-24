package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.redigit.skimbyte.feed.ui.SharedElementContext
import io.redigit.skimbyte.ui.navigation.AccountHome
import io.redigit.skimbyte.ui.navigation.AvatarViewer
import io.redigit.skimbyte.ui.navigation.BannerViewer
import io.redigit.skimbyte.ui.navigation.Settings
import io.redigit.skimbyte.ui.navigation.Topics
import io.redigit.skimbyte.ui.screens.onboarding.TopicsEffect
import io.redigit.skimbyte.ui.screens.onboarding.TopicsViewModel
import io.redigit.skimbyte.ui.screens.onboarding.TopicsScreen
import io.redigit.skimbyte.ui.screens.settings.SettingsViewModel
import io.redigit.skimbyte.ui.screens.settings.SettingsScreen
import io.redigit.skimbyte.ui.components.AVATAR_SHARED_ELEMENT_KEY
import io.redigit.skimbyte.ui.components.BANNER_SHARED_ELEMENT_KEY
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
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AccountNavHost(onRootChanged: (Boolean) -> Unit) {
    val backStack = rememberNavBackStack(navConfig, AccountHome)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    LaunchedEffect(backStack.size) { onRootChanged(backStack.size == 1) }
    val accountViewModel = koinViewModel<AccountViewModel>()
    val uiState by accountViewModel.state.collectAsState()

    SharedTransitionLayout {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            sceneStrategies = listOf(listDetailStrategy),
            sharedTransitionScope = this,
            entryProvider = entryProvider {
                entry<AccountHome>(
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = { AccountDetailPlaceholder() },
                    ),
                ) {
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
                entry<AvatarViewer>(metadata = ListDetailSceneStrategy.detailPane()) {
                    val sharedTransition = SharedElementContext(this@SharedTransitionLayout, LocalNavAnimatedContentScope.current)
                    val pickAvatar = rememberImagePickerLauncher { image -> image?.let { accountViewModel.onIntent(AccountIntent.AvatarPicked(it)) } }
                    ImageViewerScreen(
                        imageUrl = uiState.avatarUrl,
                        sharedElementKey = AVATAR_SHARED_ELEMENT_KEY,
                        onBack = { backStack.removeLastOrNull() },
                        onChangePhotoClick = pickAvatar,
                        sharedTransition = sharedTransition,
                    )
                }
                entry<BannerViewer>(metadata = ListDetailSceneStrategy.detailPane()) {
                    val sharedTransition = SharedElementContext(this@SharedTransitionLayout, LocalNavAnimatedContentScope.current)
                    val pickBanner = rememberImagePickerLauncher { image -> image?.let { accountViewModel.onIntent(AccountIntent.BannerPicked(it)) } }
                    ImageViewerScreen(
                        imageUrl = uiState.bannerUrl,
                        sharedElementKey = BANNER_SHARED_ELEMENT_KEY,
                        onBack = { backStack.removeLastOrNull() },
                        onChangePhotoClick = pickBanner,
                        sharedTransition = sharedTransition,
                    )
                }
                entry<Settings>(metadata = ListDetailSceneStrategy.detailPane()) {
                    val viewModel = koinViewModel<SettingsViewModel>()
                    val settingsState by viewModel.state.collectAsState()
                    SettingsScreen(
                        state = settingsState,
                        onIntent = viewModel::onIntent,
                        onEditInterestsClick = { backStack.add(Topics) },
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<Topics>(metadata = ListDetailSceneStrategy.detailPane()) {
                    val viewModel = koinViewModel<TopicsViewModel> { parametersOf(true) }
                    val topicsState by viewModel.state.collectAsState()
                    LaunchedEffect(viewModel) {
                        viewModel.effects.collect { if (it is TopicsEffect.Confirmed) backStack.removeLastOrNull() }
                    }
                    TopicsScreen(
                        state = topicsState,
                        onIntent = viewModel::onIntent,
                    )
                }
            },
        )
    }
}

@Composable
private fun AccountDetailPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select an option to see its details", style = MaterialTheme.typography.bodyLarge)
    }
}
