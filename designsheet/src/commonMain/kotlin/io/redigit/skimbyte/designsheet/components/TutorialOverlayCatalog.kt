package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.screens.onboarding.TutorialOverlay

@Composable
fun TutorialOverlayCatalogContent() {
    Box(modifier = Modifier.fillMaxWidth().height(520.dp)) {
        TutorialOverlay(onDismiss = {})
    }
}

@Preview
@Composable
private fun TutorialOverlayCatalogPreview() {
    SkimbyteTheme {
        TutorialOverlayCatalogContent()
    }
}
