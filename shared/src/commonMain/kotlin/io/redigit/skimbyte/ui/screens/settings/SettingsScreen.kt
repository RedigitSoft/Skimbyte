package io.redigit.skimbyte.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeMode

@Composable
fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onEditInterestsClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                expandedHeight = 48.dp,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxWidth()) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            ThemeMode.entries.forEach { mode ->
                ListItem(
                    headlineContent = { Text(mode.displayLabel()) },
                    leadingContent = {
                        RadioButton(
                            selected = mode == state.themeMode,
                            onClick = { onIntent(SettingsIntent.ThemeModeSelected(mode)) },
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = mode == state.themeMode,
                            onClick = { onIntent(SettingsIntent.ThemeModeSelected(mode)) },
                        ),
                )
            }
            ListItem(
                headlineContent = { Text("Edit interests") },
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = false, onClick = onEditInterestsClick),
            )
        }
    }
}

private fun ThemeMode.displayLabel(): String = when (this) {
    ThemeMode.Light -> "Light"
    ThemeMode.Dark -> "Dark"
    ThemeMode.System -> "System default"
}

@Preview
@Composable
private fun SettingsScreenLightThemePreview() {
    SkimbyteTheme {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.Light),
            onIntent = {},
            onEditInterestsClick = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun SettingsScreenLightThemeLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.Light),
            onIntent = {},
            onEditInterestsClick = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun SettingsScreenDarkThemePreview() {
    SkimbyteTheme {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.Dark),
            onIntent = {},
            onEditInterestsClick = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun SettingsScreenDarkThemeLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.Dark),
            onIntent = {},
            onEditInterestsClick = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun SettingsScreenSystemThemePreview() {
    SkimbyteTheme {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.System),
            onIntent = {},
            onEditInterestsClick = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun SettingsScreenSystemThemeLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.System),
            onIntent = {},
            onEditInterestsClick = {},
            onBack = {},
        )
    }
}
