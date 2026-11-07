package io.redigit.skimbyte.designsheet

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.redigit.skimbyte.designsheet.catalog.CatalogDetailPlaceholder
import io.redigit.skimbyte.designsheet.catalog.CatalogDetailScreen
import io.redigit.skimbyte.designsheet.catalog.CatalogListScreen
import io.redigit.skimbyte.designsheet.catalog.catalogItems
import io.redigit.skimbyte.di.appModule
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Serializable
private data object CatalogList : NavKey

@Serializable
private data class CatalogDetail(val itemId: String) : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(CatalogList::class, CatalogList.serializer())
            subclass(CatalogDetail::class, CatalogDetail.serializer())
        }
    }
}

/**
 * Entry point for the design sheet: a living catalog of every shared design token and component,
 * navigable as an adaptive list-detail layout that reflows across compact phones, foldables
 * (both folded and unfolded), tablets, and resizable desktop/web windows.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
@Preview
fun DesignSheetApp() {
    KoinApplication(configuration = koinConfiguration { modules(appModule) }) {
        SkimbyteTheme {
            val backStack = rememberNavBackStack(navConfig, CatalogList)
            val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategies = listOf(listDetailStrategy),
                entryProvider = entryProvider {
                    entry<CatalogList>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { CatalogDetailPlaceholder() },
                        ),
                    ) {
                        CatalogListScreen(
                            items = catalogItems,
                            onItemClick = { item -> backStack.add(CatalogDetail(item.id)) },
                        )
                    }
                    entry<CatalogDetail>(
                        metadata = ListDetailSceneStrategy.detailPane(),
                    ) { key ->
                        val item = catalogItems.first { it.id == key.itemId }
                        CatalogDetailScreen(
                            item = item,
                            onBack = { backStack.removeLastOrNull() },
                        )
                    }
                },
            )
        }
    }
}
