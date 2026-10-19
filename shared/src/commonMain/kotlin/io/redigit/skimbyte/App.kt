package io.redigit.skimbyte

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.auth.ui.AccountRoute
import io.redigit.skimbyte.bookmarks.ui.BookmarksNavHost
import io.redigit.skimbyte.di.appModule
import io.redigit.skimbyte.feed.ui.FeedNavHost
import io.redigit.skimbyte.nav.TopLevelDestination
import io.redigit.skimbyte.nav.ui.SkimbyteBottomNavBar
import io.redigit.skimbyte.theme.SkimbyteTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(configuration = koinConfiguration { modules(appModule) }) {
        SkimbyteTheme {
            var selectedTopLevel by remember { mutableStateOf(TopLevelDestination.Feed) }
            when (selectedTopLevel) {
                TopLevelDestination.Feed -> FeedNavHost(
                    selected = selectedTopLevel,
                    onDestinationSelected = { selectedTopLevel = it },
                )
                TopLevelDestination.Bookmarks -> Scaffold(
                    bottomBar = {
                        SkimbyteBottomNavBar(
                            selected = selectedTopLevel,
                            onDestinationSelected = { selectedTopLevel = it },
                            fabMenu = null,
                        )
                    },
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        BookmarksNavHost()
                    }
                }
                TopLevelDestination.Account -> Scaffold(
                    bottomBar = {
                        SkimbyteBottomNavBar(
                            selected = selectedTopLevel,
                            onDestinationSelected = { selectedTopLevel = it },
                            fabMenu = null,
                        )
                    },
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        AccountRoute()
                    }
                }
            }
        }
    }
}
