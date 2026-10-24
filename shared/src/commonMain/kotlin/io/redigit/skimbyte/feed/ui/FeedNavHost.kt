package io.redigit.skimbyte.feed.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
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
import io.redigit.skimbyte.feed.FeedViewModel
import io.redigit.skimbyte.ui.navigation.FeedHome
import io.redigit.skimbyte.ui.navigation.PostDetail
import io.redigit.skimbyte.ui.screens.auth.LoginSheet
import io.redigit.skimbyte.util.openUrl
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.viewmodel.koinViewModel

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(FeedHome::class, FeedHome.serializer())
            subclass(PostDetail::class, PostDetail.serializer())
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
fun FeedNavHost(
    onRootChanged: (Boolean) -> Unit,
    viewModel: FeedViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val backStack = rememberNavBackStack(navConfig, FeedHome)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    LaunchedEffect(backStack.size) { onRootChanged(backStack.size == 1) }

    if (uiState.pendingBookmarkAction != null) {
        LoginSheet(onDismiss = viewModel::onLoginDismissed, onLoginSuccess = viewModel::onLoginSucceeded)
    }

    SharedTransitionLayout {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            sceneStrategies = listOf(listDetailStrategy),
            sharedTransitionScope = this,
            entryProvider = entryProvider {
                entry<FeedHome>(
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = { PostDetailPlaceholder() },
                    ),
                ) {
                    val sharedTransition = SharedElementContext(this@SharedTransitionLayout, LocalNavAnimatedContentScope.current)
                    FeedScreen(
                        uiState = uiState,
                        onTopicSelected = viewModel::onTopicSelected,
                        onPageChanged = viewModel::onPageChanged,
                        onBookmarkClick = viewModel::onBookmarkTapped,
                        onReadSourceClick = ::openUrl,
                        onCardClick = { backStack.add(PostDetail(it)) },
                        onViewModeToggled = viewModel::onViewModeToggled,
                        onLoginSucceeded = viewModel::onLoginSucceeded,
                        onLoginDismissed = viewModel::onLoginDismissed,
                        sharedTransition = sharedTransition,
                    )
                }
                entry<PostDetail>(
                    metadata = ListDetailSceneStrategy.detailPane(),
                ) { key ->
                    val post = uiState.posts.firstOrNull { it.id == key.postId }
                    if (post != null) {
                        val sharedTransition = SharedElementContext(this@SharedTransitionLayout, LocalNavAnimatedContentScope.current)
                        PostDetailScreen(
                            post = post,
                            isBookmarkLoading = post.id in uiState.pendingBookmarkToggles,
                            onBookmarkClick = { viewModel.onBookmarkTapped(key.postId) },
                            onReadSourceClick = ::openUrl,
                            onBack = { backStack.removeLastOrNull() },
                            sharedTransition = sharedTransition,
                        )
                    } else {
                        PostDetailPlaceholder()
                    }
                }
            },
        )
    }
}
