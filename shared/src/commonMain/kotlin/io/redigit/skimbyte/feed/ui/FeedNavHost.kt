package io.redigit.skimbyte.feed.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.redigit.skimbyte.auth.ui.LoginSheet
import io.redigit.skimbyte.feed.FeedViewModel
import io.redigit.skimbyte.nav.FeedHome
import io.redigit.skimbyte.nav.SearchHome
import io.redigit.skimbyte.nav.SourceDetail
import io.redigit.skimbyte.nav.TopLevelDestination
import io.redigit.skimbyte.nav.ui.SkimbyteBottomNavBar
import io.redigit.skimbyte.nav.ui.SkimbyteFabMenu
import io.redigit.skimbyte.util.openUrl
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.viewmodel.koinViewModel

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(FeedHome::class, FeedHome.serializer())
            subclass(SearchHome::class, SearchHome.serializer())
            subclass(SourceDetail::class, SourceDetail.serializer())
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun FeedNavHost(
    selected: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    viewModel: FeedViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val backStack = rememberNavBackStack(navConfig, FeedHome)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

    if (uiState.pendingBookmarkAction != null) {
        LoginSheet(onDismiss = viewModel::onLoginDismissed, onLoginSuccess = viewModel::onLoginSucceeded)
    }

    Scaffold(
        bottomBar = {
            SkimbyteBottomNavBar(
                selected = selected,
                onDestinationSelected = onDestinationSelected,
                onSearchClick = { backStack.add(SearchHome) },
                fabMenu = {
                    val focused = uiState.posts.getOrNull(uiState.currentIndex)
                    if (focused != null) {
                        var expanded by remember { mutableStateOf(false) }
                        var lockedPostId by remember { mutableStateOf<String?>(null) }
                        val targetId = if (expanded) lockedPostId ?: focused.id else focused.id
                        val target = uiState.posts.firstOrNull { it.id == targetId } ?: focused
                        SkimbyteFabMenu(
                            expanded = expanded,
                            onExpandedChange = { newExpanded ->
                                expanded = newExpanded
                                lockedPostId = if (newExpanded) target.id else null
                            },
                            isBookmarked = target.isBookmarked,
                            isBookmarkLoading = target.id in uiState.pendingBookmarkToggles,
                            onBookmarkClick = { viewModel.onBookmarkTapped(target.id) },
                            onShareClick = { openUrl(target.sourceUrl) },
                            onOpenSourceClick = { openUrl(target.sourceUrl) },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategies = listOf(listDetailStrategy),
                entryProvider = entryProvider {
                    entry<FeedHome>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { SourceDetailPlaceholder() },
                        ),
                    ) {
                        FeedScreen(
                            uiState = uiState,
                            onTopicSelected = viewModel::onTopicSelected,
                            onPageChanged = viewModel::onPageChanged,
                            onBookmarkClick = viewModel::onBookmarkTapped,
                            onReadSourceClick = ::openUrl,
                            onSourceClick = { backStack.add(SourceDetail(it)) },
                            onLoginSucceeded = viewModel::onLoginSucceeded,
                            onLoginDismissed = viewModel::onLoginDismissed,
                        )
                    }
                    entry<SearchHome>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { SourceDetailPlaceholder() },
                        ),
                    ) {
                        SearchScreen(
                            posts = uiState.posts,
                            pendingBookmarkToggles = uiState.pendingBookmarkToggles,
                            onBookmarkClick = viewModel::onBookmarkTapped,
                            onReadSourceClick = ::openUrl,
                            onSourceClick = { backStack.add(SourceDetail(it)) },
                            onBack = { backStack.removeLastOrNull() },
                        )
                    }
                    entry<SourceDetail>(
                        metadata = ListDetailSceneStrategy.detailPane(),
                    ) { key ->
                        SourceDetailScreen(
                            sourceName = key.sourceName,
                            posts = uiState.posts,
                            pendingBookmarkToggles = uiState.pendingBookmarkToggles,
                            onBookmarkClick = viewModel::onBookmarkTapped,
                            onReadSourceClick = ::openUrl,
                            onBack = { backStack.removeLastOrNull() },
                        )
                    }
                },
            )
        }
    }
}
