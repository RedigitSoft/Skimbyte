package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.util.AVATAR_ASPECT
import io.redigit.skimbyte.util.BANNER_ASPECT
import io.redigit.skimbyte.util.PickedImage
import io.redigit.skimbyte.util.computeCropRect
import io.redigit.skimbyte.util.coverBaseScale
import io.redigit.skimbyte.util.cropToRect
import io.redigit.skimbyte.util.decodeImageSize
import io.redigit.skimbyte.util.maxPan
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Full-screen interactive cropper: the user pans and pinch-zooms [image] within a fixed [aspect]
 * frame, and on confirm the framed region is cropped to source pixels and returned via [onConfirm].
 * [circularMask] draws the frame as a circle (avatars); the produced crop is still the frame's
 * rectangle. If the image's dimensions can't be decoded, confirming passes the original through
 * unchanged rather than blocking the user.
 */
@Composable
fun ImageCropScreen(
    image: PickedImage,
    aspect: Float,
    circularMask: Boolean,
    onCancel: () -> Unit,
    onConfirm: (PickedImage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sourceSize = remember(image) { decodeImageSize(image.bytes) }
    val scope = rememberCoroutineScope()
    var processing by remember { mutableStateOf(false) }

    var userScale by remember(image) { mutableFloatStateOf(1f) }
    var panX by remember(image) { mutableFloatStateOf(0f) }
    var panY by remember(image) { mutableFloatStateOf(0f) }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val density = LocalDensity.current
            // Largest frame of the target aspect that fits the available area, with a small margin.
            val marginPx = with(density) { 24.dp.toPx() }
            val availW = constraints.maxWidth - marginPx * 2
            val availH = constraints.maxHeight - marginPx * 2
            val frameW: Float
            val frameH: Float
            if (availW / availH > aspect) {
                frameH = availH
                frameW = availH * aspect
            } else {
                frameW = availW
                frameH = availW / aspect
            }

            val frameShape = if (circularMask) CircleShape else RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .size(with(density) { frameW.toDp() }, with(density) { frameH.toDp() })
                    .clip(frameShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (sourceSize != null) {
                    val base = coverBaseScale(frameW, frameH, sourceSize.width, sourceSize.height)
                    val k = base * userScale
                    val dispW = sourceSize.width * k
                    val dispH = sourceSize.height * k
                    AsyncImage(
                        model = image.bytes,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            // requiredSize (not size) so the image keeps its true pixel aspect and is
                            // allowed to overflow the frame - the frame clips it. With plain size() the
                            // parent frame's constraints squash a rectangular image into the frame's
                            // aspect and cap zoom/pan at the frame bounds.
                            .requiredSize(with(density) { dispW.toDp() }, with(density) { dispH.toDp() })
                            .offset { IntOffset(panX.roundToInt(), panY.roundToInt()) }
                            .pointerInput(sourceSize, frameW, frameH) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    userScale = (userScale * zoom).coerceIn(1f, 6f)
                                    val totalScale = base * userScale
                                    val maxX = maxPan(frameW, sourceSize.width, totalScale)
                                    val maxY = maxPan(frameH, sourceSize.height, totalScale)
                                    panX = (panX + pan.x).coerceIn(-maxX, maxX)
                                    panY = (panY + pan.y).coerceIn(-maxY, maxY)
                                }
                            },
                    )
                } else {
                    // Can't decode dimensions: show the image fitted, no gestures (confirm passes through).
                    AsyncImage(
                        model = image.bytes,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            // Frame outline on top of the image.
            Box(
                modifier = Modifier
                    .size(with(density) { frameW.toDp() }, with(density) { frameH.toDp() })
                    .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), frameShape),
            )

            IconButton(
                onClick = onCancel,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = Color.White)
            }

            Text(
                text = "Drag to reposition · pinch to zoom",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp),
            )

            FloatingActionButton(
                onClick = {
                    if (processing) return@FloatingActionButton
                    processing = true
                    scope.launch {
                        val cropped = if (sourceSize != null) {
                            val rect = computeCropRect(
                                frameW, frameH, sourceSize.width, sourceSize.height, userScale, panX, panY,
                            )
                            cropToRect(image, rect)
                        } else {
                            image
                        }
                        onConfirm(cropped)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            ) {
                if (processing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                } else {
                    Icon(Icons.Filled.Check, contentDescription = "Use photo")
                }
            }
        }
    }
}

// Previews render the cropper chrome (frame, controls); there are no decodable image bytes in a
// preview, so the frame shows its placeholder surface rather than a photo.
@Preview
@Composable
private fun ImageCropScreenAvatarPreview() {
    SkimbyteTheme {
        ImageCropScreen(
            image = PickedImage(bytes = ByteArray(0), mimeType = "image/png"),
            aspect = AVATAR_ASPECT,
            circularMask = true,
            onCancel = {},
            onConfirm = {},
        )
    }
}

@Preview
@Composable
private fun ImageCropScreenBannerPreview() {
    SkimbyteTheme {
        ImageCropScreen(
            image = PickedImage(bytes = ByteArray(0), mimeType = "image/png"),
            aspect = BANNER_ASPECT,
            circularMask = false,
            onCancel = {},
            onConfirm = {},
        )
    }
}
