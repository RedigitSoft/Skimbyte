package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.DailyProgressBar

@Composable
fun DailyProgressBarCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        DailyProgressBar(read = 0, total = 8)
        DailyProgressBar(read = 3, total = 8)
        DailyProgressBar(read = 8, total = 8)
        DailyProgressBar(read = 14, total = 40)
    }
}

@Preview
@Composable
private fun DailyProgressBarCatalogPreview() {
    SkimbyteTheme {
        DailyProgressBarCatalogContent()
    }
}
