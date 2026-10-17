package io.redigit.skimbyte.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun EyebrowText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun EyebrowTextPreview() {
    SkimbyteTheme {
        EyebrowText("Sunday, September 7")
    }
}

@Preview
@Composable
private fun EyebrowTextLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        EyebrowText("Sunday, September 7")
    }
}