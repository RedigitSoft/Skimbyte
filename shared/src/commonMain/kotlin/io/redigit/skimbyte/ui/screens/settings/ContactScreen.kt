package io.redigit.skimbyte.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteIcons
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.SkimbyteStrings
import io.redigit.skimbyte.util.WebsiteLinks

/**
 * In-app contact page: the developer's email shown as plain, selectable text (so it's visible and
 * copyable without leaving the app), a one-tap "Send email" action, and links out to the hosted
 * contact page and the public issue tracker. Linked from [SettingsScreen]'s About section.
 *
 * The visible email address — not just a mailto link — is deliberate: it satisfies the Play
 * "News & Magazines" requirement for clearly shown, in-app contact information, and works even on a
 * device with no mail app configured. Same top-bar/card treatment as the rest of Settings.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactScreen(
    onBack: () -> Unit,
    onSendEmail: () -> Unit,
    onWebsiteClick: () -> Unit,
    onIssuesClick: () -> Unit,
    modifier: Modifier = Modifier,
    email: String = WebsiteLinks.CONTACT_EMAIL,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(SkimbyteStrings.Contact.TITLE) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("contactBack")) {
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
                Text(
                    SkimbyteStrings.Contact.INTRO,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                EyebrowText(SkimbyteStrings.Contact.EMAIL_SECTION, modifier = Modifier.padding(start = 4.dp))
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surface) {
                                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                    Icon(
                                        SkimbyteIcons.Mail,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                // Selectable so the address can be copied even where no mail app is set up.
                                SelectionContainer {
                                    Text(
                                        email,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.testTag("contactEmail"),
                                    )
                                }
                                Text(
                                    SkimbyteStrings.Contact.EMAIL_SUPPORTING,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = onSendEmail,
                            modifier = Modifier.fillMaxWidth().testTag("sendEmailButton"),
                        ) {
                            Icon(SkimbyteIcons.Mail, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(
                                SkimbyteStrings.Contact.SEND_EMAIL,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }

                EyebrowText(SkimbyteStrings.Contact.MORE_SECTION, modifier = Modifier.padding(start = 4.dp))
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column {
                        ContactLinkRow(
                            icon = SkimbyteIcons.OpenInNew,
                            label = SkimbyteStrings.Contact.WEBSITE,
                            supporting = SkimbyteStrings.Contact.WEBSITE_SUPPORTING,
                            onClick = onWebsiteClick,
                            modifier = Modifier.testTag("contactWebsiteRow"),
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        ContactLinkRow(
                            icon = SkimbyteIcons.Document,
                            label = SkimbyteStrings.Contact.ISSUES,
                            supporting = SkimbyteStrings.Contact.ISSUES_SUPPORTING,
                            onClick = onIssuesClick,
                            modifier = Modifier.testTag("contactIssuesRow"),
                        )
                    }
                }

                Text(
                    SkimbyteStrings.Contact.SOURCES_NOTE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

/** Icon-tile link row matching Settings' action rows — label, supporting text, external-link chevron. */
@Composable
private fun ContactLinkRow(
    icon: ImageVector,
    label: String,
    supporting: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surface) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(supporting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(SkimbyteIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview
@Composable
private fun ContactScreenDarkPreview() {
    SkimbyteTheme {
        ContactScreen(onBack = {}, onSendEmail = {}, onWebsiteClick = {}, onIssuesClick = {})
    }
}

@Preview
@Composable
private fun ContactScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        ContactScreen(onBack = {}, onSendEmail = {}, onWebsiteClick = {}, onIssuesClick = {})
    }
}