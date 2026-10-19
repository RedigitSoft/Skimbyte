package io.redigit.skimbyte.onboarding

import io.redigit.skimbyte.posts.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeTopicPreferencesStore : TopicPreferencesStore {
    private var topics: Set<Topic> = emptySet()
    private var onboardingCompleted: Boolean = false
    private val _selectedTopicsFlow = MutableStateFlow(topics)
    override val selectedTopicsFlow: StateFlow<Set<Topic>> = _selectedTopicsFlow

    override fun selectedTopics(): Set<Topic> = topics

    override fun setSelectedTopics(topics: Set<Topic>) {
        this.topics = topics
        _selectedTopicsFlow.value = topics
    }

    override fun hasCompletedOnboarding(): Boolean = onboardingCompleted

    override fun setOnboardingCompleted() {
        onboardingCompleted = true
    }
}

class TopicsViewModelTest {
    @Test
    fun `confirm disabled below 3 selections`() {
        val vm = TopicsViewModel(FakeTopicPreferencesStore(), isEditing = false)
        vm.onTopicToggled(Topic.Kotlin)
        vm.onTopicToggled(Topic.Android)
        assertFalse(vm.uiState.value.canConfirm)
    }

    @Test
    fun `confirm enabled at exactly 3 selections`() {
        val vm = TopicsViewModel(FakeTopicPreferencesStore(), isEditing = false)
        listOf(Topic.Kotlin, Topic.Android, Topic.Kmp).forEach(vm::onTopicToggled)
        assertTrue(vm.uiState.value.canConfirm)
    }

    @Test
    fun `onConfirm persists selection and onboarding flag`() {
        val store = FakeTopicPreferencesStore()
        val vm = TopicsViewModel(store, isEditing = false)
        listOf(Topic.Kotlin, Topic.Android, Topic.Kmp).forEach(vm::onTopicToggled)
        vm.onConfirm()
        assertEquals(setOf(Topic.Kotlin, Topic.Android, Topic.Kmp), store.selectedTopics())
        assertTrue(store.hasCompletedOnboarding())
    }

    @Test
    fun `editing mode pre-populates from the store`() {
        val store = FakeTopicPreferencesStore().apply { setSelectedTopics(setOf(Topic.Tooling)) }
        val vm = TopicsViewModel(store, isEditing = true)
        assertEquals(setOf(Topic.Tooling), vm.uiState.value.selected)
        assertTrue(vm.uiState.value.isEditing)
    }
}
