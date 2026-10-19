package io.redigit.skimbyte.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.redigit.skimbyte.nav.AccountHome
import io.redigit.skimbyte.nav.Settings
import io.redigit.skimbyte.nav.Topics
import io.redigit.skimbyte.nav.TopLevelDestination
import io.redigit.skimbyte.nav.ui.SkimbyteBottomNavBar
import io.redigit.skimbyte.onboarding.TopicsViewModel
import io.redigit.skimbyte.onboarding.ui.TopicsScreen
import io.redigit.skimbyte.settings.SettingsViewModel
import io.redigit.skimbyte.settings.ui.SettingsScreen
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
        }
    }
}

@Composable
fun AccountNavHost(
    selected: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    isCompact: Boolean,
) {
    val backStack = rememberNavBackStack(navConfig, AccountHome)

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
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<AccountHome> {
                        AccountRoute(onSettingsClick = { backStack.add(Settings) })
                    }
                    entry<Settings> {
                        val viewModel = koinViewModel<SettingsViewModel>()
                        val uiState by viewModel.uiState.collectAsState()
                        SettingsScreen(
                            uiState = uiState,
                            onThemeModeSelected = viewModel::onThemeModeSelected,
                            onEditInterestsClick = { backStack.add(Topics) },
                            onBack = { backStack.removeLastOrNull() },
                        )
                    }
                    entry<Topics> {
                        val viewModel = koinViewModel<TopicsViewModel> { parametersOf(true) }
                        val uiState by viewModel.uiState.collectAsState()
                        TopicsScreen(
                            uiState = uiState,
                            onTopicToggled = viewModel::onTopicToggled,
                            onConfirm = {
                                viewModel.onConfirm()
                                backStack.removeLastOrNull()
                            },
                        )
                    }
                },
            )
        }
    }
}
