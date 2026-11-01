package io.redigit.skimbyte.ui.components

/**
 * The avatar/banner upload lifecycle, surfaced on the profile so each phase gets its own feedback:
 * a spinner while [Compressing] or [Uploading], an error indicator on [Error], and the normal edit
 * affordance when [Idle].
 */
enum class ImageUploadState { Idle, Compressing, Uploading, Error }

/** True while work is in flight — taps are blocked and a loading indicator is shown. */
val ImageUploadState.isBusy: Boolean
    get() = this == ImageUploadState.Compressing || this == ImageUploadState.Uploading

/** The short caption shown beside the spinner for the two in-flight phases; null otherwise. */
internal fun ImageUploadState.caption(): String? = when (this) {
    ImageUploadState.Compressing -> "Compressing…"
    ImageUploadState.Uploading -> "Uploading…"
    ImageUploadState.Idle, ImageUploadState.Error -> null
}
