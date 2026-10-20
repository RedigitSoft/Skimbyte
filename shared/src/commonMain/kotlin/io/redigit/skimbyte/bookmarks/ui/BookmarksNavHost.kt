package io.redigit.skimbyte.bookmarks.ui

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
import io.redigit.skimbyte.bookmarks.BookmarksViewModel
import io.redigit.skimbyte.feed.ui.PostDetailPlaceholder
import io.redigit.skimbyte.feed.ui.PostDetailScreen
import io.redigit.skimbyte.feed.ui.SharedElementContext
import io.redigit.skimbyte.nav.BookmarksList
import io.redigit.skimbyte.nav.PostDetail
import io.redigit.skimbyte.util.openUrl
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.viewmodel.koinViewModel

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(BookmarksList::class, BookmarksList.serializer())
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
fun BookmarksNavHost(
    onRootChanged: (Boolean) -> Unit,
    viewModel: BookmarksViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val backStack = rememberNavBackStack(navConfig, BookmarksList)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    LaunchedEffect(backStack.size) { onRootChanged(backStack.size == 1) }

    SharedTransitionLayout {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            sceneStrategies = listOf(listDetailStrategy),
            sharedTransitionScope = this,
            entryProvider = entryProvider {
                entry<BookmarksList>(
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = { PostDetailPlaceholder() },
                    ),
                ) {
                    BookmarksListScreen(
                        uiState = uiState,
                        onPostClick = { post -> backStack.add(PostDetail(post.id)) },
                        onLoginSucceeded = viewModel::onLoginSucceeded,
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
                            isBookmarkLoading = false,
                            onBookmarkClick = {
                                viewModel.onUnbookmark(key.postId)
                                backStack.removeLastOrNull()
                            },
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
