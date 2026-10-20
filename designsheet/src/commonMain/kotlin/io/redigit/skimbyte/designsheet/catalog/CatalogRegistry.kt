package io.redigit.skimbyte.designsheet.catalog

import io.redigit.skimbyte.designsheet.components.AccountScreenCatalogContent
import io.redigit.skimbyte.designsheet.components.BookmarkButtonCatalogContent
import io.redigit.skimbyte.designsheet.components.ButtonsCatalogContent
import io.redigit.skimbyte.designsheet.components.LoginSheetCatalogContent
import io.redigit.skimbyte.designsheet.components.PostCardCatalogContent
import io.redigit.skimbyte.designsheet.components.PostDetailScreenCatalogContent
import io.redigit.skimbyte.designsheet.components.SearchScreenCatalogContent
import io.redigit.skimbyte.designsheet.components.SelectionControlsCatalogContent
import io.redigit.skimbyte.designsheet.components.SettingsScreenCatalogContent
import io.redigit.skimbyte.designsheet.components.SkimbyteBottomNavBarCatalogContent
import io.redigit.skimbyte.designsheet.components.SkimbyteNavigationRailCatalogContent
import io.redigit.skimbyte.designsheet.components.StackedCardBrowserCatalogContent
import io.redigit.skimbyte.designsheet.components.TextFieldsCatalogContent
import io.redigit.skimbyte.designsheet.components.TopicFilterCatalogContent
import io.redigit.skimbyte.designsheet.components.TopicsScreenCatalogContent
import io.redigit.skimbyte.designsheet.tokens.ColorTokensContent
import io.redigit.skimbyte.designsheet.tokens.ShapeTokensContent
import io.redigit.skimbyte.designsheet.tokens.SpacingTokensContent
import io.redigit.skimbyte.designsheet.tokens.TypographyTokensContent

/**
 * Every shared design token and component, catalogued here. Whenever a token or component is
 * added or changed in `shared`, add/update its entry here in the same change (see CONTRIBUTING.md).
 */
val catalogItems: List<CatalogItem> = listOf(
    CatalogItem("color-tokens", "Color", "Tokens") { ColorTokensContent() },
    CatalogItem("typography-tokens", "Typography", "Tokens") { TypographyTokensContent() },
    CatalogItem("spacing-tokens", "Spacing", "Tokens") { SpacingTokensContent() },
    CatalogItem("shape-tokens", "Shape", "Tokens") { ShapeTokensContent() },
    CatalogItem("buttons", "Buttons", "Components") { ButtonsCatalogContent() },
    CatalogItem("selection-controls", "Selection controls", "Components") { SelectionControlsCatalogContent() },
    CatalogItem("text-fields", "Text fields", "Components") { TextFieldsCatalogContent() },
    CatalogItem("post-card", "Post card", "Feed") { PostCardCatalogContent() },
    CatalogItem("topic-filter", "Topic filter", "Feed") { TopicFilterCatalogContent() },
    CatalogItem("bookmark-button", "Bookmark button", "Feed") { BookmarkButtonCatalogContent() },
    CatalogItem("stacked-card-browser", "Stacked card browser", "Feed") { StackedCardBrowserCatalogContent() },
    CatalogItem("post-detail", "Post detail", "Feed") { PostDetailScreenCatalogContent() },
    CatalogItem("search-screen", "Search", "Feed") { SearchScreenCatalogContent() },
    CatalogItem("bottom-nav-bar", "Bottom nav bar", "Navigation") { SkimbyteBottomNavBarCatalogContent() },
    CatalogItem("navigation-rail", "Navigation rail", "Navigation") { SkimbyteNavigationRailCatalogContent() },
    CatalogItem("login-sheet", "Login sheet", "Bookmarks") { LoginSheetCatalogContent() },
    CatalogItem("account-screen", "Account", "Bookmarks") { AccountScreenCatalogContent() },
    CatalogItem("topics-screen", "Topics", "Onboarding") { TopicsScreenCatalogContent() },
    CatalogItem("settings-screen", "Settings", "Account") { SettingsScreenCatalogContent() },
)
