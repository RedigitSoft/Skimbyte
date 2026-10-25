package io.redigit.skimbyte.ui.screens.onboarding

import io.redigit.skimbyte.data.local.TopicPreferencesStore
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.repositories.TopicsRepository
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

private class FakeTopicsRepository(topics: List<Topic> = Topic.selectable) : TopicsRepository {
    private val _topics = MutableStateFlow(topics)
    override val topics: StateFlow<List<Topic>> = _topics
    override suspend fun refresh() {}
    fun setTopics(topics: List<Topic>) {
        _topics.value = topics
    }
}

class TopicsViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest fun setupMain() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest fun tearDownMain() = Dispatchers.resetMain()

    private fun vm(
        store: TopicPreferencesStore = FakeTopicPreferencesStore(),
        topics: TopicsRepository = FakeTopicsRepository(),
        isEditing: Boolean = false,
    ) = TopicsViewModel(store, topics, isEditing)

    @Test
    fun `confirm disabled below 3 selections`() {
        val model = vm()
        model.onIntent(TopicsIntent.TopicToggled(Topic.Kotlin))
        model.onIntent(TopicsIntent.TopicToggled(Topic.Android))
        assertFalse(model.state.value.canConfirm)
    }

    @Test
    fun `confirm stays enabled beyond four selections`() {
        val many = (1..6).map { Topic(id = "t$it", displayLabel = "T$it", apiValue = "T$it") }
        val model = vm(topics = FakeTopicsRepository(many))
        many.forEach { model.onIntent(TopicsIntent.TopicToggled(it)) }
        assertEquals(6, model.state.value.selected.size)
        assertTrue(model.state.value.canConfirm) // no upper cap
    }

    @Test
    fun `confirm enabled at exactly 3 selections`() {
        val model = vm()
        listOf(Topic.Kotlin, Topic.Android, Topic.Kmp).forEach { model.onIntent(TopicsIntent.TopicToggled(it)) }
        assertTrue(model.state.value.canConfirm)
    }

    @Test
    fun `onConfirm persists selection and onboarding flag`() {
        val store = FakeTopicPreferencesStore()
        val model = vm(store = store)
        listOf(Topic.Kotlin, Topic.Android, Topic.Kmp).forEach { model.onIntent(TopicsIntent.TopicToggled(it)) }
        model.onIntent(TopicsIntent.Confirmed)
        assertEquals(setOf(Topic.Kotlin, Topic.Android, Topic.Kmp), store.selectedTopics())
        assertTrue(store.hasCompletedOnboarding())
    }

    @Test
    fun `editing mode pre-populates from the store`() {
        val store = FakeTopicPreferencesStore().apply { setSelectedTopics(setOf(Topic.Tooling)) }
        val model = vm(store = store, isEditing = true)
        assertEquals(setOf(Topic.Tooling), model.state.value.selected)
        assertTrue(model.state.value.isEditing)
    }

    @Test
    fun `available topics come from the repository`() {
        val repoTopics = listOf(Topic.Kotlin, Topic.Android)
        val model = vm(topics = FakeTopicsRepository(repoTopics))
        assertEquals(repoTopics, model.state.value.available)
    }

    @Test
    fun `a topic added server-side after the screen opens appears in available`() = runTest {
        val repository = FakeTopicsRepository(listOf(Topic.Kotlin))
        val model = vm(topics = repository)
        assertEquals(listOf(Topic.Kotlin), model.state.value.available)

        val ai = Topic(id = "ai", displayLabel = "AI", apiValue = "AI")
        repository.setTopics(listOf(Topic.Kotlin, ai)) // the API fetch lands after construction

        assertEquals(listOf(Topic.Kotlin, ai), model.state.value.available)
    }
}
