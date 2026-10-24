package io.redigit.skimbyte.ui.screens.onboarding

import io.redigit.skimbyte.data.local.TopicPreferencesStore
import io.redigit.skimbyte.data.models.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
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
    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest fun setupMain() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest fun tearDownMain() = Dispatchers.resetMain()

    @Test
    fun `confirm disabled below 3 selections`() {
        val vm = TopicsViewModel(FakeTopicPreferencesStore(), isEditing = false)
        vm.onIntent(TopicsIntent.TopicToggled(Topic.Kotlin))
        vm.onIntent(TopicsIntent.TopicToggled(Topic.Android))
        assertFalse(vm.state.value.canConfirm)
    }

    @Test
    fun `confirm enabled at exactly 3 selections`() {
        val vm = TopicsViewModel(FakeTopicPreferencesStore(), isEditing = false)
        listOf(Topic.Kotlin, Topic.Android, Topic.Kmp).forEach { vm.onIntent(TopicsIntent.TopicToggled(it)) }
        assertTrue(vm.state.value.canConfirm)
    }

    @Test
    fun `onConfirm persists selection and onboarding flag`() {
        val store = FakeTopicPreferencesStore()
        val vm = TopicsViewModel(store, isEditing = false)
        listOf(Topic.Kotlin, Topic.Android, Topic.Kmp).forEach { vm.onIntent(TopicsIntent.TopicToggled(it)) }
        vm.onIntent(TopicsIntent.Confirmed)
        assertEquals(setOf(Topic.Kotlin, Topic.Android, Topic.Kmp), store.selectedTopics())
        assertTrue(store.hasCompletedOnboarding())
    }

    @Test
    fun `editing mode pre-populates from the store`() {
        val store = FakeTopicPreferencesStore().apply { setSelectedTopics(setOf(Topic.Tooling)) }
        val vm = TopicsViewModel(store, isEditing = true)
        assertEquals(setOf(Topic.Tooling), vm.state.value.selected)
        assertTrue(vm.state.value.isEditing)
    }
}
