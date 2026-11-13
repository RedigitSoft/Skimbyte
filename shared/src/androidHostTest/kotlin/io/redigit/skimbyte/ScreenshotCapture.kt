package io.redigit.skimbyte

import androidx.compose.ui.test.SemanticsNodeInteraction
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage

/**
 * Captures a screenshot with a small changed-pixel tolerance instead of Roborazzi's exact-match
 * default. Goldens are recorded on macOS but verified on Linux CI, and the two platforms
 * anti-alias curved edges slightly differently — the measured divergence tops out at ~0.34% of
 * pixels, while a real UI change moves orders of magnitude more, so 1% absorbs the rendering
 * noise without masking regressions.
 */
fun SemanticsNodeInteraction.captureScreenshot() {
    captureRoboImage(
        roborazziOptions = RoborazziOptions(
            compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f),
        ),
    )
}
