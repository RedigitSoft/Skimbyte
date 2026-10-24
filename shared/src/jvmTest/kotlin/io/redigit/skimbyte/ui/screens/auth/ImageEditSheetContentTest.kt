package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageEditSheetContentTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chooseFromGallery_invokesCallback() = runComposeUiTest {
        var chosen = false
        setContent {
            SkimbyteTheme {
                ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = { chosen = true }, onRemove = {})
            }
        }
        onNodeWithText("Choose from gallery").performClick()
        waitForIdle()
        assertEquals(true, chosen)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remove_invokesCallback() = runComposeUiTest {
        var removed = false
        setContent {
            SkimbyteTheme {
                ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = { removed = true })
            }
        }
        onNodeWithText("Remove current image").performClick()
        waitForIdle()
        assertEquals(true, removed)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun withoutRemove_hidesRemoveRow() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = null)
            }
        }
        onNodeWithText("Remove current image").assertDoesNotExist()
    }
}
