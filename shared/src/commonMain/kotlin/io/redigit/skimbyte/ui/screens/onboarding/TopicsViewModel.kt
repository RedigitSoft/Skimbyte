package io.redigit.skimbyte.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.local.TopicPreferencesStore
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.repositories.TopicsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TopicsViewModel(
    private val store: TopicPreferencesStore,
    topicsRepository: TopicsRepository,
    isEditing: Boolean,
) : ViewModel() {

    private val _state = MutableStateFlow(
        TopicsState(
            available = topicsRepository.topics.value,
            selected = if (isEditing) store.selectedTopics() else emptySet(),
            isEditing = isEditing,
        ),
    )
    val state: StateFlow<TopicsState> = _state.asStateFlow()

    init {
        // Keep the selectable topics in sync with the repository (served by the backend once the
        // topics API is live; the seeded defaults until then).
        viewModelScope.launch {
            topicsRepository.topics.collect { topics -> _state.value = _state.value.copy(available = topics) }
        }
    }

    private val _effects = MutableSharedFlow<TopicsEffect>()
    val effects: SharedFlow<TopicsEffect> = _effects.asSharedFlow()

    fun onIntent(intent: TopicsIntent) {
        when (intent) {
            is TopicsIntent.TopicToggled -> onTopicToggled(intent.topic)
            TopicsIntent.Confirmed -> onConfirm()
        }
    }

    private fun onTopicToggled(topic: Topic) {
        val current = _state.value.selected
        val updated = if (topic in current) current - topic else current + topic
        _state.value = _state.value.copy(selected = updated)
    }

    private fun onConfirm() {
        store.setSelectedTopics(_state.value.selected)
        store.setOnboardingCompleted()
        viewModelScope.launch { _effects.emit(TopicsEffect.Confirmed) }
    }
}
