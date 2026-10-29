package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Sign-in provider marks, hand-built as [ImageVector]s (24x24 viewports, standard simple-icons
 * path data) so no raster assets or extra dependencies are needed. GitHub and Apple are
 * monochrome — render them with `Icon` so they tint to the button's content color. Google's "G"
 * is multi-color per its brand guidelines — render it with `Image` (untinted).
 */
internal object ProviderLogos {

    val GitHub: ImageVector by lazy {
        ImageVector.Builder(
            name = "GitHubLogo", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 0.297f)
                curveTo(5.37f, 0.297f, 0f, 5.67f, 0f, 12.297f)
                curveTo(0f, 17.6f, 3.438f, 22.097f, 8.205f, 23.682f)
                curveTo(8.805f, 23.795f, 9.025f, 23.424f, 9.025f, 23.105f)
                curveTo(9.025f, 22.82f, 9.015f, 22.065f, 9.01f, 21.065f)
                curveTo(5.672f, 21.789f, 4.968f, 19.455f, 4.968f, 19.455f)
                curveTo(4.422f, 18.07f, 3.633f, 17.7f, 3.633f, 17.7f)
                curveTo(2.546f, 16.956f, 3.717f, 16.971f, 3.717f, 16.971f)
                curveTo(4.922f, 17.055f, 5.555f, 18.207f, 5.555f, 18.207f)
                curveTo(6.625f, 20.042f, 8.364f, 19.512f, 9.05f, 19.205f)
                curveTo(9.158f, 18.429f, 9.467f, 17.9f, 9.81f, 17.6f)
                curveTo(7.145f, 17.3f, 4.344f, 16.268f, 4.344f, 11.67f)
                curveTo(4.344f, 10.36f, 4.809f, 9.29f, 5.579f, 8.45f)
                curveTo(5.444f, 8.147f, 5.039f, 6.927f, 5.684f, 5.274f)
                curveTo(5.684f, 5.274f, 6.689f, 4.952f, 8.984f, 6.504f)
                curveTo(9.944f, 6.237f, 10.964f, 6.105f, 11.984f, 6.099f)
                curveTo(13.004f, 6.105f, 14.024f, 6.237f, 14.984f, 6.504f)
                curveTo(17.264f, 4.952f, 18.269f, 5.274f, 18.269f, 5.274f)
                curveTo(18.914f, 6.927f, 18.509f, 8.147f, 18.389f, 8.45f)
                curveTo(19.154f, 9.29f, 19.619f, 10.36f, 19.619f, 11.67f)
                curveTo(19.619f, 16.28f, 16.814f, 17.295f, 14.144f, 17.59f)
                curveTo(14.564f, 17.95f, 14.954f, 18.686f, 14.954f, 19.81f)
                curveTo(14.954f, 21.416f, 14.939f, 22.706f, 14.939f, 23.096f)
                curveTo(14.939f, 23.411f, 15.149f, 23.786f, 15.764f, 23.666f)
                curveTo(20.565f, 22.092f, 24f, 17.592f, 24f, 12.297f)
                curveTo(24f, 5.67f, 18.627f, 0.297f, 12f, 0.297f)
                close()
            }
        }.build()
    }

    val Apple: ImageVector by lazy {
        ImageVector.Builder(
            name = "AppleLogo", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12.152f, 6.896f)
                curveTo(11.204f, 6.896f, 9.737f, 5.818f, 8.192f, 5.856f)
                curveTo(6.152f, 5.883f, 4.282f, 7.039f, 3.231f, 8.87f)
                curveTo(1.114f, 12.545f, 2.685f, 17.973f, 4.75f, 20.96f)
                curveTo(5.763f, 22.414f, 6.958f, 24.05f, 8.542f, 23.999f)
                curveTo(10.062f, 23.934f, 10.632f, 23.012f, 12.477f, 23.012f)
                curveTo(14.308f, 23.012f, 14.827f, 23.999f, 16.437f, 23.96f)
                curveTo(18.074f, 23.934f, 19.113f, 22.48f, 20.113f, 21.012f)
                curveTo(21.269f, 19.324f, 21.749f, 17.687f, 21.775f, 17.597f)
                curveTo(21.736f, 17.584f, 18.593f, 16.376f, 18.555f, 12.74f)
                curveTo(18.529f, 9.7f, 21.035f, 8.246f, 21.152f, 8.181f)
                curveTo(19.723f, 6.091f, 17.529f, 5.857f, 16.762f, 5.805f)
                curveTo(14.762f, 5.649f, 13.087f, 6.896f, 12.152f, 6.896f)
                close()
                moveTo(15.53f, 3.83f)
                curveTo(16.373f, 2.818f, 16.93f, 1.403f, 16.775f, 0f)
                curveTo(15.568f, 0.052f, 14.113f, 0.805f, 13.243f, 1.818f)
                curveTo(12.463f, 2.714f, 11.789f, 4.156f, 11.97f, 5.532f)
                curveTo(13.308f, 5.636f, 14.685f, 4.844f, 15.53f, 3.83f)
                close()
            }
        }.build()
    }

    val Google: ImageVector by lazy {
        ImageVector.Builder(
            name = "GoogleLogo", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF4285F4))) {
                moveTo(22.56f, 12.25f)
                curveTo(22.56f, 11.47f, 22.49f, 10.72f, 22.36f, 10f)
                lineTo(12f, 10f)
                lineTo(12f, 14.26f)
                lineTo(17.92f, 14.26f)
                curveTo(17.66f, 15.63f, 16.88f, 16.79f, 15.71f, 17.57f)
                lineTo(15.71f, 20.34f)
                lineTo(19.28f, 20.34f)
                curveTo(21.36f, 18.42f, 22.56f, 15.6f, 22.56f, 12.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFF34A853))) {
                moveTo(12f, 23f)
                curveTo(14.97f, 23f, 17.46f, 22.02f, 19.28f, 20.34f)
                lineTo(15.71f, 17.57f)
                curveTo(14.73f, 18.23f, 13.48f, 18.63f, 12f, 18.63f)
                curveTo(9.14f, 18.63f, 6.71f, 16.7f, 5.84f, 14.1f)
                lineTo(2.18f, 14.1f)
                lineTo(2.18f, 16.94f)
                curveTo(3.99f, 20.53f, 7.7f, 23f, 12f, 23f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFBBC05))) {
                moveTo(5.84f, 14.09f)
                curveTo(5.62f, 13.43f, 5.49f, 12.73f, 5.49f, 12f)
                curveTo(5.49f, 11.27f, 5.62f, 10.57f, 5.84f, 9.91f)
                lineTo(5.84f, 7.07f)
                lineTo(2.18f, 7.07f)
                curveTo(1.43f, 8.55f, 1f, 10.22f, 1f, 12f)
                curveTo(1f, 13.78f, 1.43f, 15.45f, 2.18f, 16.93f)
                lineTo(5.03f, 14.71f)
                lineTo(5.84f, 14.09f)
                close()
            }
            path(fill = SolidColor(Color(0xFFEA4335))) {
                moveTo(12f, 5.38f)
                curveTo(13.62f, 5.38f, 15.06f, 5.94f, 16.21f, 7.02f)
                lineTo(19.36f, 3.87f)
                curveTo(17.45f, 2.09f, 14.97f, 1f, 12f, 1f)
                curveTo(7.7f, 1f, 3.99f, 3.47f, 2.18f, 7.07f)
                lineTo(5.84f, 9.91f)
                curveTo(6.71f, 7.31f, 9.14f, 5.38f, 12f, 5.38f)
                close()
            }
        }.build()
    }
}
