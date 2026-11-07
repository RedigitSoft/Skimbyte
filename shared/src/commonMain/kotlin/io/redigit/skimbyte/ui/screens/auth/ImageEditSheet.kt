package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteIcons
import io.redigit.skimbyte.theme.SkimbyteTheme

/**
 * Bottom sheet of actions for the avatar/banner edit badge: take a photo, choose from the gallery,
 * or remove the current image. [onRemove] is null when there's nothing to remove (no current
 * image), in which case that row is hidden. Each action is expected to dismiss the sheet via its
 * callback at the call site.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditSheet(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseFromGallery: () -> Unit,
    onRemove: (() -> Unit)?,
    cameraAvailable: Boolean = true,
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        ImageEditSheetContent(
            onTakePhoto = onTakePhoto,
            onChooseFromGallery = onChooseFromGallery,
            onRemove = onRemove,
            cameraAvailable = cameraAvailable,
        )
    }
}

// Factored out of the ModalBottomSheet container so it can be rendered standalone (e.g. in the
// designsheet catalog and UI tests) without sheet/scrim machinery.
@Composable
fun ImageEditSheetContent(
    onTakePhoto: () -> Unit,
    onChooseFromGallery: () -> Unit,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier,
    cameraAvailable: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            "Edit photo",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
        )
        // Only offered where a camera exists — hidden on desktops without a webcam (see
        // isCameraAvailable). Gallery/file picking is always available.
        if (cameraAvailable) {
            OptionRow(
                icon = SkimbyteIcons.Camera,
                title = "Take photo",
                subtitle = "Use your camera",
                onClick = onTakePhoto,
            )
        }
        OptionRow(
            icon = SkimbyteIcons.Gallery,
            title = "Choose from gallery",
            subtitle = "JPEG, PNG or WebP",
            onClick = onChooseFromGallery,
        )
        if (onRemove != null) {
            OptionRow(
                icon = SkimbyteIcons.Delete,
                title = "Remove current image",
                subtitle = "Reset to the default",
                onClick = onRemove,
                tint = MaterialTheme.colorScheme.error,
                iconTint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = iconTint.copy(alpha = 0.14f)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.padding(8.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = tint)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview
@Composable
private fun ImageEditSheetContentPreview() {
    SkimbyteTheme {
        ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = {})
    }
}

@Preview
@Composable
private fun ImageEditSheetContentNoRemovePreview() {
    SkimbyteTheme {
        ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = null)
    }
}

/** No camera (e.g. a desktop without a webcam): the "Take photo" row is omitted. */
@Preview
@Composable
private fun ImageEditSheetContentNoCameraPreview() {
    SkimbyteTheme {
        ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = {}, cameraAvailable = false)
    }
}

@Preview
@Composable
private fun ImageEditSheetContentLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = {})
    }
}
