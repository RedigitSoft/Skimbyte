package io.redigit.skimbyte.designsheet.catalog

import androidx.compose.runtime.Composable

data class CatalogItem(
    val id: String,
    val title: String,
    val category: String,
    val content: @Composable () -> Unit,
)
