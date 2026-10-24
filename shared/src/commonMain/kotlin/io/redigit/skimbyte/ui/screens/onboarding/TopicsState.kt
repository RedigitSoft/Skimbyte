package io.redigit.skimbyte.ui.screens.onboarding

import io.redigit.skimbyte.data.models.Topic

data class TopicsState(
    val available: List<Topic> = Topic.entries.filter { it != Topic.All },
    val selected: Set<Topic> = emptySet(),
    val isEditing: Boolean = false,
) {
    val canConfirm: Boolean get() = selected.size in 3..4
}
