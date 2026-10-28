package io.redigit.skimbyte

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import kotlinx.coroutines.delay
import androidx.window.core.layout.WindowSizeClass
import io.redigit.skimbyte.ui.screens.bookmarks.BookmarksNavHost
import io.redigit.skimbyte.di.appModule
import io.redigit.skimbyte.ui.screens.feed.FeedNavHost
import io.redigit.skimbyte.ui.navigation.TopLevelDestination
import io.redigit.skimbyte.ui.navigation.SkimbyteBottomNavBar
import io.redigit.skimbyte.ui.navigation.SkimbyteNavigationRail
import io.redigit.skimbyte.data.local.TopicPreferencesStore
import io.redigit.skimbyte.data.local.TutorialStore
import io.redigit.skimbyte.data.repositories.TopicsRepository
import io.redigit.skimbyte.ui.components.NewTopicsDialog
import io.redigit.skimbyte.ui.screens.onboarding.TopicsEffect
import io.redigit.skimbyte.ui.screens.onboarding.TopicsViewModel
import io.redigit.skimbyte.ui.screens.onboarding.TopicsScreen
import io.redigit.skimbyte.ui.screens.onboarding.TutorialOverlay
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeModeStore
import io.redigit.skimbyte.ui.screens.auth.AccountNavHost
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinConfiguration
import kotlin.time.Duration.Companion.milliseconds

sealed interface AppStage {
    data object Onboarding : AppStage
    data object Main : AppStage
}

@Composable
@Preview
fun App() {
    // Coil's Ktor network fetcher is not auto-registered (unlike the OkHttp artifact), so without
    // this factory AsyncImage has no network support at all and every remote image silently shows
    // its placeholder. A plain HttpClient (default engine per platform) is used on purpose: the
    // app's authenticated client would attach bearer tokens to third-party image hosts.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }
    KoinApplication(configuration = koinConfiguration { modules(appModule) }) {
        // Eagerly create the topics repository: its init fetches GET /v1/topics on every app open
        // and reconciles the result with the persisted catalogue, so new server-side topics are
        // discovered (and announced below) without any polling.
        val topicsRepository = koinInject<TopicsRepository>()
        val themeModeStore = koinInject<ThemeModeStore>()
        val themeMode by themeModeStore.themeModeFlow.collectAsState()
        SkimbyteTheme(themeMode = themeMode) {
            val topicPreferencesStore = koinInject<TopicPreferencesStore>()
            // The onboarding check is a synchronous settings read, so the first stage is decided
            // before the first frame - no in-app splash. (On Android the system splash screen
            // already covers startup; a Compose splash after it showed as a second splash.)
            var stage by remember {
                mutableStateOf<AppStage>(
                    if (topicPreferencesStore.hasCompletedOnboarding()) AppStage.Main else AppStage.Onboarding,
                )
            }
            AnimatedContent(
                targetState = stage,
                transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(350)) },
                label = "appStage",
            ) { current ->
                when (current) {
                    AppStage.Onboarding -> {
                        val topicsViewModel = koinViewModel<TopicsViewModel> { parametersOf(false) }
                        val uiState by topicsViewModel.state.collectAsState()
                        LaunchedEffect(topicsViewModel) {
                            topicsViewModel.effects.collect { if (it is TopicsEffect.Confirmed) stage = AppStage.Main }
                        }
                        TopicsScreen(
                            state = uiState,
                            onIntent = topicsViewModel::onIntent,
                        )
                    }
                    AppStage.Main -> {
                        val tutorialStore = koinInject<TutorialStore>()
                        var showTutorial by remember { mutableStateOf(!tutorialStore.hasSeenTutorial()) }
                        Box {
                            MainShell()
                            if (showTutorial) {
                                TutorialOverlay(
                                    onDismiss = {
                                        tutorialStore.setTutorialSeen()
                                        showTutorial = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Announce topics the catalogue sync discovered. During onboarding the picker already
            // shows the whole catalogue, so pending discoveries are acknowledged silently there.
            val newTopics by topicsRepository.pendingNewTopics.collectAsState()
            if (newTopics.isNotEmpty()) {
                when (stage) {
                    AppStage.Main -> NewTopicsDialog(
                        topics = newTopics,
                        onDismiss = topicsRepository::acknowledgeNewTopics,
                    )
                    AppStage.Onboarding -> LaunchedEffect(newTopics) { topicsRepository.acknowledgeNewTopics() }
                }
            }
        }
    }
}

@Composable
private fun MainShell() {
    var selected by remember { mutableStateOf(TopLevelDestination.Feed) }
    var isShowingRoot by remember { mutableStateOf(true) }
    var navVisible by remember { mutableStateOf(true) }
    var scrollTick by remember { mutableStateOf(0) }
    val saveableStateHolder = rememberSaveableStateHolder()
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val isCompact = !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val onDestinationSelected: (TopLevelDestination) -> Unit = { selected = it }

    // Hide the floating nav while content scrolls vertically; each scroll bumps scrollTick and the
    // effect below brings the bar back after a short idle gap.
    val hideNavOnScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y != 0f) {
                    navVisible = false
                    scrollTick++
                }
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(scrollTick) {
        if (!navVisible) {
            delay(600.milliseconds)
            navVisible = true
        }
    }
    // Always show the bar again when switching tabs or returning to a tab's root.
    LaunchedEffect(selected, isShowingRoot) { navVisible = true }

    Row {
        // The rail is never hidden: at medium/expanded width, ListDetailSceneStrategy always
        // shows list+detail as two simultaneous panes, so there is never an "immersive
        // single-pane detail" state to hide chrome for - unlike the compact bottom bar below.
        if (!isCompact) SkimbyteNavigationRail(selected, onDestinationSelected)
        Scaffold(
            modifier = Modifier.weight(1f),
            // The top inset is deliberately left unconsumed so detail pages can draw edge-to-edge
            // under the status bar; tab root screens apply statusBarsPadding (or a TopAppBar,
            // which insets itself) individually.
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            floatingActionButton = {
                AnimatedVisibility(
                    visible = isCompact && isShowingRoot && navVisible,
                    enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut(),
                ) {
                    SkimbyteBottomNavBar(
                        selected = selected,
                        onDestinationSelected = onDestinationSelected,
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .nestedScroll(hideNavOnScroll)
            ) {
                saveableStateHolder.SaveableStateProvider(selected) {
                    when (selected) {
                        TopLevelDestination.Feed -> FeedNavHost(onRootChanged = { isShowingRoot = it })
                        TopLevelDestination.Bookmarks -> BookmarksNavHost(onRootChanged = { isShowingRoot = it })
                        TopLevelDestination.Account -> AccountNavHost(onRootChanged = { isShowingRoot = it })
                    }
                }
            }
        }
    }
}
