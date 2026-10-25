package io.redigit.skimbyte.ui.screens.onboarding

import io.redigit.skimbyte.data.models.Topic

data class TopicsState(
    val available: List<Topic> = Topic.selectable,
    val selected: Set<Topic> = emptySet(),
    val isEditing: Boolean = false,
) {
    // At least 3 so the feed has enough to work with; no upper bound - users can follow as many
    // topics as they like, and every followed topic shows up as a feed filter and on the profile.
    val canConfirm: Boolean get() = selected.size >= 3
}
