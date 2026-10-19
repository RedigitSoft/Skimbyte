package io.redigit.skimbyte.onboarding

import androidx.lifecycle.ViewModel
import io.redigit.skimbyte.posts.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TopicsViewModel(
    private val store: TopicPreferencesStore,
    isEditing: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TopicsUiState(selected = if (isEditing) store.selectedTopics() else emptySet(), isEditing = isEditing),
    )
    val uiState: StateFlow<TopicsUiState> = _uiState.asStateFlow()

    fun onTopicToggled(topic: Topic) {
        val current = _uiState.value.selected
        val updated = if (topic in current) current - topic else current + topic
        _uiState.value = _uiState.value.copy(selected = updated)
    }

    fun onConfirm() {
        store.setSelectedTopics(_uiState.value.selected)
        store.setOnboardingCompleted()
    }
}
