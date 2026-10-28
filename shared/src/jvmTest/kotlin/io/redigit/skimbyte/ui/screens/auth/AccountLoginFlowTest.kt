package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.navigation.SkimbyteBottomNavBar
import io.redigit.skimbyte.ui.navigation.TopLevelDestination
import io.redigit.skimbyte.ui.screens.feed.FeedScreen
import io.redigit.skimbyte.ui.screens.feed.FeedState
import io.redigit.skimbyte.ui.screens.feed.TopicFeed
import kotlin.test.Test
import kotlin.time.Instant

/**
 * Drives the signed-out profile flow a user actually walks: feed → Account tab → "Not now" on the
 * login sheet → back to the feed.
 *
 * Like [io.redigit.skimbyte.AppNavigationTest] (androidHostTest), this doesn't drive `App()` end
 * to end — `MainShell` is private and the real `AccountRoute`/`LoginSheet` resolve their
 * ViewModels (and their repositories) from Koin. Instead the harness reproduces `MainShell`'s tab
 * switching (`SaveableStateProvider` keyed on the selected destination) and `AccountRoute`'s
 * login-sheet gating (`!isLoggedIn && !loginSheetDismissed`, see AccountScreen.kt) with the real
 * prop-driven components: [FeedScreen], [AccountScreen], [LoginSheetContent], and
 * [SkimbyteBottomNavBar].
 */
class AccountLoginFlowTest {

    private val posts = listOf(
        Post(
            id = "post-1",
            topic = "Kotlin",
            title = "Kotlin 2.4.21 released",
            summaryMarkdown = "Faster Wasm compilation and reflection fixes.",
            sourceName = "Kotlin Releases",
            sourceUrl = "https://example.com",
            imageUrl = null,
            digestDate = "2026-10-13",
            publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
            isBookmarked = false,
        ),
    )

    private val feedState = FeedState(
        availableTopics = listOf(Topic.All, Topic.Kotlin),
        selectedTopic = Topic.All,
        feeds = mapOf(Topic.All to TopicFeed(posts = posts)),
    )

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    private fun FlowHarness() {
        var selected by remember { mutableStateOf(TopLevelDestination.Feed) }
        // Mirrors AccountRoute's gating: the sheet shows while signed out and not yet dismissed.
        var loginSheetDismissed by rememberSaveable { mutableStateOf(false) }
        val saveableStateHolder = rememberSaveableStateHolder()
        SkimbyteTheme {
            Box(modifier = Modifier.fillMaxSize()) {
                saveableStateHolder.SaveableStateProvider(selected) {
                    when (selected) {
                        TopLevelDestination.Feed -> FeedScreen(
                            state = feedState,
                            onIntent = {},
                            onReadSourceClick = {},
                            onCardClick = {},
                        )
                        TopLevelDestination.Account -> {
                            AccountScreen(
                                state = AccountState(isLoggedIn = false),
                                onIntent = {},
                                onSettingsClick = {},
                                onAvatarClick = {},
                                onAvatarEditClick = {},
                                onBannerClick = {},
                                onBannerEditClick = {},
                            )
                            // LoginSheetContent stands in for LoginSheet: the ModalBottomSheet
                            // wrapper only adds scrim/animation machinery and a Koin ViewModel;
                            // the "Not now" behavior under test lives in the content + gating.
                            if (!loginSheetDismissed) {
                                LoginSheetContent(
                                    state = LoginSheetState(),
                                    onAppleSignInClick = {},
                                    onGithubSignInClick = {},
                                    onGoogleSignInClick = {},
                                    onNotNowClick = { loginSheetDismissed = true },
                                )
                            }
                        }
                        else -> {}
                    }
                }
                SkimbyteBottomNavBar(
                    selected = selected,
                    onDestinationSelected = { selected = it },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun profileTab_notNowDismissesLoginSheet_andFeedIsIntactOnReturn() = runComposeUiTest {
        setContent { FlowHarness() }

        // Home: the feed is showing.
        onNodeWithText("For You").assertExists()
        onNodeWithText("Kotlin 2.4.21 released").assertExists()

        // Go to the profile tab — signed out, so the login sheet is up.
        onNodeWithContentDescription("Account").performClick()
        waitForIdle()
        onNodeWithText("Sign in to save stories").assertExists()

        // "Not now" closes the sheet and leaves the signed-out profile visible.
        onNodeWithText("Not now").performClick()
        waitForIdle()
        onNodeWithText("Sign in to save stories").assertDoesNotExist()
        onNodeWithText("Not now").assertDoesNotExist()
        onNodeWithText("You're not signed in").assertExists()

        // Back to home: the feed is still there.
        onNodeWithContentDescription("Feed").performClick()
        waitForIdle()
        onNodeWithText("For You").assertExists()
        onNodeWithText("Kotlin 2.4.21 released").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dismissedLoginSheet_staysDismissed_whenReturningToProfileTab() = runComposeUiTest {
        setContent { FlowHarness() }

        // Dismiss the sheet on the profile tab.
        onNodeWithContentDescription("Account").performClick()
        waitForIdle()
        onNodeWithText("Not now").performClick()
        waitForIdle()

        // Leave for home and come back: the sheet must not reappear (rememberSaveable gating).
        onNodeWithContentDescription("Feed").performClick()
        waitForIdle()
        onNodeWithText("For You").assertExists()
        onNodeWithContentDescription("Account").performClick()
        waitForIdle()
        onNodeWithText("You're not signed in").assertExists()
        onNodeWithText("Sign in to save stories").assertDoesNotExist()
    }
}