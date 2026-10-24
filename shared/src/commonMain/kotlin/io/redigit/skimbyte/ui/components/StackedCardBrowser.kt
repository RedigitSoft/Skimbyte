package io.redigit.skimbyte.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme

enum class StackedCardOrientation { Vertical, Horizontal }

/**
 * A peeking-card browser: the current card is large and centered, with the next card's edge
 * peeking in from the direction of [orientation]'s scroll axis. The main Feed peeks vertically
 * (next card below); collection/source browsing peeks horizontally (next card to the side) —
 * both patterns are from the reference video.
 */
@Composable
fun <T> StackedCardBrowser(
    items: List<T>,
    onIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    orientation: StackedCardOrientation = StackedCardOrientation.Vertical,
    resetSignal: Any? = null,
    emptyContent: @Composable () -> Unit = {},
    cardContent: @Composable (item: T) -> Unit,
) {
    if (items.isEmpty()) {
        emptyContent()
        return
    }
    val pagerState = rememberPagerState(pageCount = { items.size })
    LaunchedEffect(pagerState.currentPage) { onIndexChanged(pagerState.currentPage) }
    LaunchedEffect(resetSignal) { if (resetSignal != null) pagerState.animateScrollToPage(0) }
    when (orientation) {
        StackedCardOrientation.Vertical -> VerticalPager(
            state = pagerState,
            contentPadding = PaddingValues(bottom = 64.dp),
            pageSpacing = 12.dp,
            modifier = modifier.fillMaxSize(),
        ) { page -> cardContent(items[page]) }
        StackedCardOrientation.Horizontal -> HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(end = 64.dp),
            pageSpacing = 12.dp,
            modifier = modifier.fillMaxSize(),
        ) { page -> cardContent(items[page]) }
    }
}

@Preview
@Composable
private fun StackedCardBrowserPreview() {
    SkimbyteTheme {
        StackedCardBrowser(
            items = listOf("Card one", "Card two", "Card three"),
            onIndexChanged = {},
        ) { item ->
            androidx.compose.material3.Card(modifier = Modifier.fillMaxSize()) {
                androidx.compose.material3.Text(item, modifier = Modifier)
            }
        }
    }
}