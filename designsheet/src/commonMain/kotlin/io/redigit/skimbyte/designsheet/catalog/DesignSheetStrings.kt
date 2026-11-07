package io.redigit.skimbyte.designsheet.catalog

/**
 * The designsheet's own chrome copy, single-sourced like the app's `SkimbyteStrings`. Copy that
 * *duplicates* app strings (the status-prompt demos) references `SkimbyteStrings` directly instead,
 * so the catalog can't drift from what the app actually says; catalog entry titles and
 * descriptions live on their `CatalogItem`s in the registry, which is already their one home.
 */
object DesignSheetStrings {
    const val TITLE = "Design sheet"
    const val SELECT_ITEM = "Select an item from the list"
    const val PREVIEW_LIGHT = "Preview in light theme"
    const val PREVIEW_DARK = "Preview in dark theme"
}
