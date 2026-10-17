package io.redigit.skimbyte.bookmarks.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.redigit.skimbyte.bookmarks.BookmarksViewModel
import io.redigit.skimbyte.nav.BookmarkDetail
import io.redigit.skimbyte.nav.BookmarksList
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.viewmodel.koinViewModel

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(BookmarksList::class, BookmarksList.serializer())
            subclass(BookmarkDetail::class, BookmarkDetail.serializer())
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BookmarksNavHost(viewModel: BookmarksViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val backStack = rememberNavBackStack(navConfig, BookmarksList)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(listDetailStrategy),
        entryProvider = entryProvider {
            entry<BookmarksList>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = { BookmarkDetailPlaceholder() },
                ),
            ) {
                BookmarksListScreen(
                    uiState = uiState,
                    onPostClick = { post -> backStack.add(BookmarkDetail(post.id)) },
                    onLoginSucceeded = viewModel::onLoginSucceeded,
                )
            }
            entry<BookmarkDetail>(
                metadata = ListDetailSceneStrategy.detailPane(),
            ) { key ->
                val post = uiState.posts.firstOrNull { it.id == key.postId }
                if (post != null) {
                    BookmarkDetailScreen(
                        post = post,
                        onUnbookmark = {
                            viewModel.onUnbookmark(it)
                            backStack.removeLastOrNull()
                        },
                    )
                } else {
                    BookmarkDetailPlaceholder()
                }
            }
        },
    )
}
