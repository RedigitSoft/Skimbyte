package io.redigit.skimbyte.designsheet.catalog

import androidx.compose.runtime.Composable

data class CatalogItem(
    val id: String,
    val title: String,
    val category: String,
    /** One sentence on what the entry is and where the app uses it — shown in the index and as the detail header. */
    val description: String,
    val content: @Composable () -> Unit,
)
