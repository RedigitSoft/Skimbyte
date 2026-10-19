package io.redigit.skimbyte.bookmarks.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import io.redigit.skimbyte.nav.TopLevelDestination
import io.redigit.skimbyte.nav.ui.SkimbyteBottomNavBar
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun BookmarksNavHost(
    selected: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    isCompact: Boolean,
    viewModel: BookmarksViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val backStack = rememberNavBackStack(navConfig, BookmarksList)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

    Scaffold(
        bottomBar = {
            if (isCompact && backStack.size == 1) {
                SkimbyteBottomNavBar(
                    selected = selected,
                    onDestinationSelected = onDestinationSelected,
                    fabMenu = null,
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
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
    }
}
