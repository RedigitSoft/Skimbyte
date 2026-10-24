package io.redigit.skimbyte.ui.screens.onboarding

import io.redigit.skimbyte.data.models.Topic

sealed interface TopicsIntent {
    data class TopicToggled(val topic: Topic) : TopicsIntent
    data object Confirmed : TopicsIntent
}
