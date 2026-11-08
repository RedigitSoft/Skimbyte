package io.redigit.skimbyte.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.AccentColor
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteIcons
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeMode
import io.redigit.skimbyte.ui.SkimbyteStrings
import org.koin.compose.viewmodel.koinViewModel

/**
 * The appearance sub-screen: light/dark mode and the accent colour, split off [SettingsScreen] so
 * the settings list stays short and these two colour controls live behind one row. Shares
 * [SettingsViewModel] (and thus [SettingsState]) with Settings, so a change here is reflected there
 * and persisted the same way.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearanceScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(SkimbyteStrings.Settings.SECTION_APPEARANCE) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(SkimbyteIcons.Back, contentDescription = SkimbyteStrings.Common.BACK)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                EyebrowText(SkimbyteStrings.Settings.THEME, modifier = Modifier.padding(start = 4.dp))
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ThemeModeToggleGroup(
                            selected = state.themeMode,
                            onSelected = { onIntent(SettingsIntent.ThemeModeSelected(it)) },
                        )
                    }
                }

                EyebrowText(SkimbyteStrings.Settings.ACCENT, modifier = Modifier.padding(start = 4.dp))
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AccentColorPicker(
                            selected = state.accentColor,
                            onSelected = { onIntent(SettingsIntent.AccentColorSelected(it)) },
                        )
                    }
                }
            }
        }
    }
}

/** Nav wrapper: shares the Settings view model so theme/accent stay in sync with the settings list. */
@Composable
fun AppearanceRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.state.collectAsState()
    AppearanceScreen(state = state, onIntent = viewModel::onIntent, onBack = onBack, modifier = modifier)
}

/**
 * Theme choice as a connected toggle-button group (the Expressive segmented pattern) — each button
 * morphs between the squared connected-middle form and a rounded pill when checked.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeModeToggleGroup(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        ThemeMode.entries.forEachIndexed { index, mode ->
            ToggleButton(
                checked = mode == selected,
                onCheckedChange = { if (mode != selected) onSelected(mode) },
                modifier = Modifier.weight(1f),
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    ThemeMode.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Icon(mode.icon(), contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(mode.displayLabel())
            }
        }
    }
}

/**
 * The user's accent picker: one swatch per [AccentColor], wrapping to the available width. The
 * selected swatch carries an onSurface ring and a check drawn in the accent's own on-color — so
 * selection reads on a light *and* a dark swatch, and the swatch doubles as a live preview of the
 * accent/on-accent pairing the rest of the app uses.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccentColorPicker(
    selected: AccentColor,
    onSelected: (AccentColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AccentColor.entries.forEach { accent ->
            val isSelected = accent == selected
            val label = accent.displayLabel()
            Surface(
                onClick = { onSelected(accent) },
                shape = CircleShape,
                color = accent.accent,
                border = if (isSelected) {
                    BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                } else {
                    null
                },
                modifier = Modifier
                    .size(44.dp)
                    .semantics {
                        contentDescription = label
                        this.selected = isSelected
                    },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSelected) {
                        Icon(
                            SkimbyteIcons.Check,
                            contentDescription = null,
                            tint = accent.onAccent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun ThemeMode.displayLabel(): String = when (this) {
    ThemeMode.Light -> SkimbyteStrings.Settings.THEME_LIGHT
    ThemeMode.Dark -> SkimbyteStrings.Settings.THEME_DARK
    ThemeMode.System -> SkimbyteStrings.Settings.THEME_SYSTEM
}

private fun ThemeMode.icon(): ImageVector = when (this) {
    ThemeMode.Light -> SkimbyteIcons.ThemeLight
    ThemeMode.Dark -> SkimbyteIcons.ThemeDark
    ThemeMode.System -> SkimbyteIcons.ThemeSystem
}

private fun AccentColor.displayLabel(): String = when (this) {
    AccentColor.Flame -> SkimbyteStrings.Settings.ACCENT_FLAME
    AccentColor.Amber -> SkimbyteStrings.Settings.ACCENT_AMBER
    AccentColor.Crimson -> SkimbyteStrings.Settings.ACCENT_CRIMSON
    AccentColor.Magenta -> SkimbyteStrings.Settings.ACCENT_MAGENTA
    AccentColor.Violet -> SkimbyteStrings.Settings.ACCENT_VIOLET
    AccentColor.Blue -> SkimbyteStrings.Settings.ACCENT_BLUE
    AccentColor.Cyan -> SkimbyteStrings.Settings.ACCENT_CYAN
    AccentColor.Emerald -> SkimbyteStrings.Settings.ACCENT_EMERALD
    AccentColor.Lime -> SkimbyteStrings.Settings.ACCENT_LIME
    AccentColor.Oxblood -> SkimbyteStrings.Settings.ACCENT_OXBLOOD
}

@Preview
@Composable
private fun AppearanceScreenDarkPreview() {
    SkimbyteTheme(darkTheme = true) {
        AppearanceScreen(
            state = SettingsState(themeMode = ThemeMode.Dark, accentColor = AccentColor.Emerald),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun AppearanceScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        AppearanceScreen(
            state = SettingsState(themeMode = ThemeMode.Light, accentColor = AccentColor.Violet),
            onIntent = {},
            onBack = {},
        )
    }
}
