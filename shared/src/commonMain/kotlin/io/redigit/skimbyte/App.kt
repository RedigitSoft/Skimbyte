package io.redigit.skimbyte

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass
import io.redigit.skimbyte.bookmarks.ui.BookmarksNavHost
import io.redigit.skimbyte.di.appModule
import io.redigit.skimbyte.feed.ui.FeedNavHost
import io.redigit.skimbyte.feed.ui.SearchNavHost
import io.redigit.skimbyte.ui.navigation.TopLevelDestination
import io.redigit.skimbyte.ui.navigation.SkimbyteBottomNavBar
import io.redigit.skimbyte.ui.navigation.SkimbyteNavigationRail
import io.redigit.skimbyte.onboarding.TopicPreferencesStore
import io.redigit.skimbyte.onboarding.TopicsViewModel
import io.redigit.skimbyte.onboarding.ui.SplashScreen
import io.redigit.skimbyte.onboarding.ui.TopicsScreen
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeModeStore
import io.redigit.skimbyte.ui.screens.auth.AccountNavHost
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinConfiguration

sealed interface AppStage {
    data object Splash : AppStage
    data object Onboarding : AppStage
    data object Main : AppStage
}

@Composable
@Preview
fun App() {
    KoinApplication(configuration = koinConfiguration { modules(appModule) }) {
        val themeModeStore = koinInject<ThemeModeStore>()
        val themeMode by themeModeStore.themeModeFlow.collectAsState()
        SkimbyteTheme(themeMode = themeMode) {
            val topicPreferencesStore = koinInject<TopicPreferencesStore>()
            var stage by remember { mutableStateOf<AppStage>(AppStage.Splash) }
            LaunchedEffect(Unit) {
                stage = if (topicPreferencesStore.hasCompletedOnboarding()) AppStage.Main else AppStage.Onboarding
            }
            when (stage) {
                AppStage.Splash -> SplashScreen()
                AppStage.Onboarding -> {
                    val topicsViewModel = koinViewModel<TopicsViewModel> { parametersOf(false) }
                    val uiState by topicsViewModel.uiState.collectAsState()
                    TopicsScreen(
                        uiState = uiState,
                        onTopicToggled = topicsViewModel::onTopicToggled,
                        onConfirm = { topicsViewModel.onConfirm(); stage = AppStage.Main },
                    )
                }
                AppStage.Main -> MainShell()
            }
        }
    }
}

@Composable
private fun MainShell() {
    var selected by remember { mutableStateOf(TopLevelDestination.Feed) }
    // Whether the CURRENTLY SHOWN tab is at its own backstack root - reported by whichever
    // NavHost is composed below, so the shared bottom bar hides while any tab shows a pushed
    // detail screen. Only one NavHost is ever composed at a time (SaveableStateProvider swaps
    // the single mounted child on tab switch), and it reports its own root-ness immediately on
    // (re)composition, so this one flag always reflects the visible tab, not a stale one.
    var isShowingRoot by remember { mutableStateOf(true) }
    var navBarExpanded by rememberSaveable { mutableStateOf(true) }
    val saveableStateHolder = rememberSaveableStateHolder()
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val isCompact = !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val onDestinationSelected: (TopLevelDestination) -> Unit = { selected = it }
    Row {
        // The rail is never hidden: at medium/expanded width, ListDetailSceneStrategy always
        // shows list+detail as two simultaneous panes, so there is never an "immersive
        // single-pane detail" state to hide chrome for - unlike the compact bottom bar below.
        if (!isCompact) SkimbyteNavigationRail(selected, onDestinationSelected)
        Scaffold(
            modifier = Modifier.weight(1f),
            floatingActionButton = {
                if (isCompact && isShowingRoot) {
                    SkimbyteBottomNavBar(
                        selected = selected,
                        onDestinationSelected = onDestinationSelected,
                        expanded = navBarExpanded,
                        onExpandedChange = { navBarExpanded = it },
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End,
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .floatingToolbarVerticalNestedScroll(
                        expanded = navBarExpanded,
                        onExpand = { navBarExpanded = true },
                        onCollapse = { navBarExpanded = false },
                    ),
            ) {
                saveableStateHolder.SaveableStateProvider(selected) {
                    when (selected) {
                        TopLevelDestination.Feed -> FeedNavHost(onRootChanged = { isShowingRoot = it })
                        TopLevelDestination.Bookmarks -> BookmarksNavHost(onRootChanged = { isShowingRoot = it })
                        TopLevelDestination.Search -> SearchNavHost(onRootChanged = { isShowingRoot = it })
                        TopLevelDestination.Account -> AccountNavHost(onRootChanged = { isShowingRoot = it })
                    }
                }
            }
        }
    }
}
