package io.redigit.skimbyte.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteIcons
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeMode
import io.redigit.skimbyte.ui.SkimbyteStrings

/**
 * M3 Expressive settings: a large flexible top bar that collapses on scroll, and the theme choice
 * as a connected toggle-button group (the Expressive segmented pattern) instead of a radio list.
 * Sections follow the Account screen's house style — eyebrow label + tonal surface card.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onEditInterestsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    // Account actions live here rather than on the profile so the profile stays about *you*; shown
    // only when signed in. The logout/delete logic is the AccountViewModel's — these are callbacks.
    isLoggedIn: Boolean = false,
    onLogout: () -> Unit = {},
    onDeleteData: () -> Unit = {},
    deleteInProgress: Boolean = false,
    deleteError: String? = null,
) {
    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(SkimbyteStrings.Settings.TITLE) },
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
                EyebrowText(SkimbyteStrings.Settings.SECTION_APPEARANCE, modifier = Modifier.padding(start = 4.dp))
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(SkimbyteStrings.Settings.THEME, style = MaterialTheme.typography.titleMedium)
                        ThemeModeToggleGroup(
                            selected = state.themeMode,
                            onSelected = { onIntent(SettingsIntent.ThemeModeSelected(it)) },
                        )
                    }
                }

                EyebrowText(SkimbyteStrings.Settings.SECTION_CONTENT, modifier = Modifier.padding(start = 4.dp))
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                    SettingsActionRow(
                        icon = SkimbyteIcons.Interests,
                        label = SkimbyteStrings.Settings.EDIT_INTERESTS,
                        supporting = SkimbyteStrings.Settings.EDIT_INTERESTS_SUPPORTING,
                        onClick = onEditInterestsClick,
                    )
                }

                EyebrowText(SkimbyteStrings.Settings.SECTION_ABOUT, modifier = Modifier.padding(start = 4.dp))
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column {
                        SettingsActionRow(
                            icon = SkimbyteIcons.Privacy,
                            label = SkimbyteStrings.Settings.PRIVACY_POLICY,
                            supporting = SkimbyteStrings.Settings.PRIVACY_POLICY_SUPPORTING,
                            onClick = onPrivacyPolicyClick,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsActionRow(
                            icon = SkimbyteIcons.Document,
                            label = SkimbyteStrings.Settings.LICENSES,
                            supporting = SkimbyteStrings.Settings.LICENSES_SUPPORTING,
                            onClick = onLicensesClick,
                        )
                    }
                }

                if (isLoggedIn) {
                    EyebrowText(SkimbyteStrings.Settings.SECTION_ACCOUNT, modifier = Modifier.padding(start = 4.dp))
                    deleteError?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column {
                            SettingsActionRow(
                                icon = SkimbyteIcons.SignOut,
                                label = SkimbyteStrings.Settings.LOG_OUT,
                                supporting = SkimbyteStrings.Settings.LOG_OUT_SUPPORTING,
                                onClick = { showLogoutConfirm = true },
                                tint = MaterialTheme.colorScheme.error,
                                showChevron = false,
                                modifier = Modifier.testTag("logoutRow"),
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            SettingsActionRow(
                                icon = SkimbyteIcons.DeleteForever,
                                label = SkimbyteStrings.Settings.DELETE_DATA,
                                supporting = SkimbyteStrings.Settings.DELETE_DATA_SUPPORTING,
                                onClick = { showDeleteConfirm = true },
                                tint = MaterialTheme.colorScheme.error,
                                showChevron = false,
                                modifier = Modifier.testTag("deleteDataRow"),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text(SkimbyteStrings.Settings.LOG_OUT_DIALOG_TITLE) },
            text = { Text(SkimbyteStrings.Settings.LOG_OUT_DIALOG_TEXT) },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("confirmLogout"),
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                ) { Text(SkimbyteStrings.Settings.LOG_OUT) }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text(SkimbyteStrings.Common.CANCEL) } },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!deleteInProgress) showDeleteConfirm = false },
            title = { Text(SkimbyteStrings.Settings.DELETE_DATA_DIALOG_TITLE) },
            text = {
                Text(
                    SkimbyteStrings.Settings.DELETE_DATA_DIALOG_TEXT,
                )
            },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("confirmDeleteData"),
                    enabled = !deleteInProgress,
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteData()
                    },
                ) { Text(SkimbyteStrings.Common.DELETE, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(enabled = !deleteInProgress, onClick = { showDeleteConfirm = false }) { Text(SkimbyteStrings.Common.CANCEL) }
            },
        )
    }
}

/**
 * The Expressive connected button group: three equal-width [ToggleButton]s whose shapes morph
 * between the squared connected-middle form and a fully rounded pill when checked.
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
 * Icon-tile row matching the Account screen's action rows (label, supporting text, chevron).
 * [tint] accents the icon (and the label, when not the default) — error for destructive actions;
 * [showChevron] is dropped for actions that open a dialog rather than navigate.
 */
@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    label: String,
    supporting: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    showChevron: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surface) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = tint)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (tint == MaterialTheme.colorScheme.primary) MaterialTheme.colorScheme.onSurface else tint,
            )
            Text(
                supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showChevron) {
            Icon(
                SkimbyteIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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

@Preview
@Composable
private fun SettingsScreenDarkThemePreview() {
    SkimbyteTheme {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.Dark),
            onIntent = {},
            onEditInterestsClick = {},
            onPrivacyPolicyClick = {},
            onLicensesClick = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun SettingsScreenLightThemePreview() {
    SkimbyteTheme(darkTheme = false) {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.Light),
            onIntent = {},
            onEditInterestsClick = {},
            onPrivacyPolicyClick = {},
            onLicensesClick = {},
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
            onPrivacyPolicyClick = {},
            onLicensesClick = {},
            onBack = {},
        )
    }
}
