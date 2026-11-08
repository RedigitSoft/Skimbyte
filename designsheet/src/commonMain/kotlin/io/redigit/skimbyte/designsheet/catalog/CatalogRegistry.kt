package io.redigit.skimbyte.designsheet.catalog

import io.redigit.skimbyte.designsheet.components.AccountScreenCatalogContent
import io.redigit.skimbyte.designsheet.components.BookmarkButtonCatalogContent
import io.redigit.skimbyte.designsheet.components.ButtonsCatalogContent
import io.redigit.skimbyte.designsheet.components.ImageEditSheetCatalogContent
import io.redigit.skimbyte.designsheet.components.ImageCropScreenCatalogContent
import io.redigit.skimbyte.designsheet.components.LoginSheetCatalogContent
import io.redigit.skimbyte.designsheet.components.NewTopicsDialogCatalogContent
import io.redigit.skimbyte.designsheet.components.PostCardCatalogContent
import io.redigit.skimbyte.designsheet.components.PostDetailScreenCatalogContent
import io.redigit.skimbyte.designsheet.components.ProfileImagesCatalogContent
import io.redigit.skimbyte.designsheet.components.SavedPostRowCatalogContent
import io.redigit.skimbyte.designsheet.components.SelectionControlsCatalogContent
import io.redigit.skimbyte.designsheet.components.AppearanceScreenCatalogContent
import io.redigit.skimbyte.designsheet.components.SettingsScreenCatalogContent
import io.redigit.skimbyte.designsheet.components.StatusPromptCatalogContent
import io.redigit.skimbyte.designsheet.components.SkimbyteBottomNavBarCatalogContent
import io.redigit.skimbyte.designsheet.components.SkimbyteNavigationRailCatalogContent
import io.redigit.skimbyte.designsheet.components.StackedCardBrowserCatalogContent
import io.redigit.skimbyte.designsheet.components.TextFieldsCatalogContent
import io.redigit.skimbyte.designsheet.components.TopicChipCatalogContent
import io.redigit.skimbyte.designsheet.components.TopicFilterCatalogContent
import io.redigit.skimbyte.designsheet.components.TopicImagePlaceholderCatalogContent
import io.redigit.skimbyte.designsheet.components.TopicsScreenCatalogContent
import io.redigit.skimbyte.designsheet.components.TutorialOverlayCatalogContent
import io.redigit.skimbyte.designsheet.tokens.ColorTokensContent
import io.redigit.skimbyte.designsheet.tokens.IconTokensContent
import io.redigit.skimbyte.designsheet.tokens.ShapeTokensContent
import io.redigit.skimbyte.designsheet.tokens.SpacingTokensContent
import io.redigit.skimbyte.designsheet.tokens.TypographyTokensContent

/**
 * Every shared design token and component, catalogued here. Whenever a token or component is
 * added or changed in `shared`, add/update its entry here in the same change (see CONTRIBUTING.md).
 */
val catalogItems: List<CatalogItem> = listOf(
    CatalogItem(
        "color-tokens", "Color", "Tokens",
        "Every role in both M3 schemes, paired with its on-color and hex value.",
    ) { ColorTokensContent() },
    CatalogItem(
        "typography-tokens", "Typography", "Tokens",
        "The M3 type scale as the app actually renders it, one specimen per style.",
    ) { TypographyTokensContent() },
    CatalogItem(
        "spacing-tokens", "Spacing", "Tokens",
        "The named spacing scale behind paddings and gaps, drawn to size.",
    ) { SpacingTokensContent() },
    CatalogItem(
        "shape-tokens", "Shape", "Tokens",
        "Corner radii from the shared shape scale, including the chip capsule.",
    ) { ShapeTokensContent() },
    CatalogItem(
        "icon-tokens", "Icons", "Tokens",
        "Every glyph in SkimbyteIcons, the single source of truth all UI icons are drawn from.",
    ) { IconTokensContent() },
    CatalogItem(
        "buttons", "Buttons", "Components",
        "All five Material button emphases, enabled and disabled.",
    ) { ButtonsCatalogContent() },
    CatalogItem(
        "selection-controls", "Selection controls", "Components",
        "Checkbox, switch, and radio buttons — live and toggleable.",
    ) { SelectionControlsCatalogContent() },
    CatalogItem(
        "text-fields", "Text fields", "Components",
        "Filled and outlined text fields with floating labels.",
    ) { TextFieldsCatalogContent() },
    CatalogItem(
        "status-prompt", "Status prompt", "Components",
        "The one composition behind every empty, error, and signed-out terminal state.",
    ) { StatusPromptCatalogContent() },
    CatalogItem(
        "topic-chip", "Topic chip", "Components",
        "Topic pill in its interactive (filtering) and read-only (listing) forms.",
    ) { TopicChipCatalogContent() },
    CatalogItem(
        "post-card", "Post card", "Feed",
        "The feed's story card across bookmark, headline-length, reading-time, and image variants.",
    ) { PostCardCatalogContent() },
    CatalogItem(
        "topic-image-placeholder", "Topic image placeholder", "Feed",
        "Branded stand-in under every story image: a stable per-topic tint plus the topic initial.",
    ) { TopicImagePlaceholderCatalogContent() },
    CatalogItem(
        "topic-filter", "Topic filter", "Feed",
        "The feed's horizontally scrolling topic selector row.",
    ) { TopicFilterCatalogContent() },
    CatalogItem(
        "bookmark-button", "Bookmark button", "Feed",
        "Bookmark toggle with idle, saved, and in-flight states.",
    ) { BookmarkButtonCatalogContent() },
    CatalogItem(
        "stacked-card-browser", "Stacked card browser", "Feed",
        "Swipeable stacked-deck container the feed uses to page through the daily brief.",
    ) { StackedCardBrowserCatalogContent() },
    CatalogItem(
        "post-detail", "Post detail", "Feed",
        "The full story screen: hero image, markdown summary, bookmark, and source link.",
    ) { PostDetailScreenCatalogContent() },
    CatalogItem(
        "bottom-nav-bar", "Bottom nav bar", "Navigation",
        "Floating pill navigation dock shown at compact width; the active item's pill expands.",
    ) { SkimbyteBottomNavBarCatalogContent() },
    CatalogItem(
        "navigation-rail", "Navigation rail", "Navigation",
        "Side rail that replaces the dock at medium and expanded widths.",
    ) { SkimbyteNavigationRailCatalogContent() },
    CatalogItem(
        "saved-post-row", "Saved post row", "Bookmarks",
        "Bookmarks list row with optional removal and two-line headline ellipsis.",
    ) { SavedPostRowCatalogContent() },
    CatalogItem(
        "login-sheet", "Login sheet", "Account",
        "OAuth sign-in sheet content with its provider buttons and error state.",
    ) { LoginSheetCatalogContent() },
    CatalogItem(
        "account-screen", "Account", "Account",
        "The signed-in profile: banner, avatar, bio, saved count, and followed topics.",
    ) { AccountScreenCatalogContent() },
    CatalogItem(
        "profile-images", "Avatar & banner", "Account",
        "Profile avatar and banner with edit badges, across every upload state.",
    ) { ProfileImagesCatalogContent() },
    CatalogItem(
        "image-cropper", "Image cropper", "Account",
        "Drag-and-pinch cropper chrome for circular avatar and wide banner crops.",
    ) { ImageCropScreenCatalogContent() },
    CatalogItem(
        "image-edit-sheet", "Image edit sheet", "Account",
        "Photo source picker; the remove action appears only when an image is already set.",
    ) { ImageEditSheetCatalogContent() },
    CatalogItem(
        "settings-screen", "Settings", "Account",
        "Settings with appearance, interests, legal links, and account deletion.",
    ) { SettingsScreenCatalogContent() },
    CatalogItem(
        "appearance-screen", "Appearance", "Account",
        "Light/dark theme toggle and the user-selectable accent swatches.",
    ) { AppearanceScreenCatalogContent() },
    CatalogItem(
        "topics-screen", "Topics", "Onboarding",
        "Onboarding topic picker with live selection.",
    ) { TopicsScreenCatalogContent() },
    CatalogItem(
        "new-topics-dialog", "New topics dialog", "Onboarding",
        "Announcement dialog for topics a catalogue sync discovers after onboarding.",
    ) { NewTopicsDialogCatalogContent() },
    CatalogItem(
        "tutorial-overlay", "Tutorial overlay", "Onboarding",
        "First-run gesture tutorial drawn over the feed.",
    ) { TutorialOverlayCatalogContent() },
)
