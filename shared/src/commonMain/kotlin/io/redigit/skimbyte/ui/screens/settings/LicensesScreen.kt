package io.redigit.skimbyte.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import io.redigit.skimbyte.theme.SkimbyteTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import skimbyte.shared.generated.resources.Res

/**
 * Every open-source dependency the app ships with and its license, from the AboutLibraries
 * export committed at `composeResources/files/aboutlibraries.json` (regenerate with
 * `:shared:exportLibraryDefinitions` when dependencies change). Same top-bar treatment as
 * [SettingsScreen], which links here from its About section.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LicensesScreen(
    libraries: Libs?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Open source licenses") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("licensesBack")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        if (libraries == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            // LibrariesContainer is itself a LazyColumn — it owns this screen's scrolling.
            LibrariesContainer(
                libraries = libraries,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

/** Loads the committed license metadata off the main thread, then renders [LicensesScreen]. */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun LicensesRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val libraries by produceLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }
    LicensesScreen(libraries = libraries, onBack = onBack, modifier = modifier)
}

/** Two fake entries so previews (and screenshot tests) don't depend on resource IO. */
internal fun previewLibs(): Libs = Libs.Builder()
    .withJson(
        """
        {
          "libraries": [
            {
              "uniqueId": "org.example:library-one",
              "name": "Library One",
              "artifactVersion": "1.2.3",
              "description": "A fake library for previews.",
              "developers": [{"name": "Example Dev"}],
              "licenses": ["Apache-2.0"]
            },
            {
              "uniqueId": "org.example:library-two",
              "name": "Library Two",
              "artifactVersion": "4.5.6",
              "description": "Another fake library for previews.",
              "developers": [],
              "licenses": ["MIT"]
            }
          ],
          "licenses": {
            "Apache-2.0": {
              "name": "Apache License 2.0",
              "url": "https://www.apache.org/licenses/LICENSE-2.0",
              "spdxId": "Apache-2.0",
              "hash": "preview-apache"
            },
            "MIT": {
              "name": "MIT License",
              "url": "https://opensource.org/license/mit",
              "spdxId": "MIT",
              "hash": "preview-mit"
            }
          }
        }
        """.trimIndent(),
    )
    .build()

@Preview
@Composable
private fun LicensesScreenDarkPreview() {
    SkimbyteTheme {
        LicensesScreen(libraries = previewLibs(), onBack = {})
    }
}

@Preview
@Composable
private fun LicensesScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        LicensesScreen(libraries = previewLibs(), onBack = {})
    }
}

@Preview
@Composable
private fun LicensesScreenLoadingPreview() {
    SkimbyteTheme {
        LicensesScreen(libraries = null, onBack = {})
    }
}
