package io.redigit.skimbyte.designsheet.catalog

import io.redigit.skimbyte.designsheet.components.ButtonsCatalogContent
import io.redigit.skimbyte.designsheet.components.SelectionControlsCatalogContent
import io.redigit.skimbyte.designsheet.components.TextFieldsCatalogContent
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
)
