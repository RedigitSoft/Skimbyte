package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.local.SettingsTopicPreferencesStore
import io.redigit.skimbyte.data.local.TopicPreferencesStore
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.models.dto.TopicDto
import io.redigit.skimbyte.data.remote.TopicsApi
import io.redigit.skimbyte.testing.FakeSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeTopicsApi(var result: () -> List<TopicDto>) : TopicsApi {
    var fetchCount = 0
    override suspend fun fetchTopics(): List<TopicDto> {
        fetchCount++
        return result()
    }
}

private val serverTopics = listOf(
    TopicDto(id = "kotlin", label = "Kotlin", apiValue = "Kotlin"),
    TopicDto(id = "ai", label = "AI", apiValue = "AI"), // a topic the client has no built-in constant for
)

private val aiTopic = Topic(id = "ai", displayLabel = "AI", apiValue = "AI")

class TopicsRepositoryImplTest {

    // The repository's catalogue StateFlow is shared eagerly on its scope, which never completes;
    // a detached, cancellable scope keeps runTest from reporting it as a leaked job.
    private fun TestScope.repositoryScope() = CoroutineScope(coroutineContext + Job())

    private fun repository(
        api: TopicsApi,
        scope: CoroutineScope,
        store: TopicPreferencesStore = SettingsTopicPreferencesStore(FakeSettings()),
    ) = TopicsRepositoryImpl(api, store, scope)

    @Test
    fun `seeds with the selectable defaults before the initial fetch completes`() = runTest {
        val scope = repositoryScope()
        val repository = repository(FakeTopicsApi { serverTopics }, scope)

        // The init-launched refresh hasn't run yet on the test scheduler.
        assertEquals(Topic.selectable, repository.topics.value)
        scope.cancel()
    }

    @Test
    fun `initial fetch reconciles the catalogue and surfaces topics added server-side`() = runTest {
        val scope = repositoryScope()
        val api = FakeTopicsApi { serverTopics }
        val repository = repository(api, scope)
        advanceUntilIdle()

        assertEquals(1, api.fetchCount)
        // The server list wins: its two topics replace the seeded defaults...
        assertEquals(listOf(Topic.Kotlin, aiTopic), repository.topics.value)
        // ...and the one the client had never seen is surfaced for announcement.
        assertEquals(listOf(aiTopic), repository.pendingNewTopics.value)
        scope.cancel()
    }

    @Test
    fun `acknowledgeNewTopics clears the pending announcement`() = runTest {
        val scope = repositoryScope()
        val repository = repository(FakeTopicsApi { serverTopics }, scope)
        advanceUntilIdle()

        repository.acknowledgeNewTopics()

        assertTrue(repository.pendingNewTopics.value.isEmpty())
        scope.cancel()
    }

    @Test
    fun `failed fetch keeps the stored catalogue and announces nothing`() = runTest {
        val scope = repositoryScope()
        val repository = repository(FakeTopicsApi { error("endpoint down") }, scope)
        advanceUntilIdle()

        assertEquals(Topic.selectable, repository.topics.value)
        assertTrue(repository.pendingNewTopics.value.isEmpty())
        scope.cancel()
    }

    @Test
    fun `empty fetch keeps the stored catalogue`() = runTest {
        val scope = repositoryScope()
        val repository = repository(FakeTopicsApi { emptyList() }, scope)
        advanceUntilIdle()

        assertEquals(Topic.selectable, repository.topics.value)
        scope.cancel()
    }

    @Test
    fun `refresh picks up a topic added after the initial fetch`() = runTest {
        val scope = repositoryScope()
        var topics = listOf(TopicDto(id = "kotlin", label = "Kotlin", apiValue = "Kotlin"))
        val api = FakeTopicsApi { topics }
        val repository = repository(api, scope)
        advanceUntilIdle()
        repository.acknowledgeNewTopics()

        topics = serverTopics // the backend gains a new topic between fetches
        repository.refresh()
        advanceUntilIdle()

        assertEquals(2, api.fetchCount)
        assertEquals(listOf(Topic.Kotlin, aiTopic), repository.topics.value)
        assertEquals(listOf(aiTopic), repository.pendingNewTopics.value)
        scope.cancel()
    }

    @Test
    fun `a synced catalogue survives relaunch without network and is not re-announced`() = runTest {
        val settings = FakeSettings()
        val firstScope = repositoryScope()
        repository(FakeTopicsApi { serverTopics }, firstScope, SettingsTopicPreferencesStore(settings))
        advanceUntilIdle()
        firstScope.cancel()

        // "Relaunch": fresh store and repository over the same persisted settings, server down.
        val secondScope = repositoryScope()
        val relaunched = repository(FakeTopicsApi { error("offline") }, secondScope, SettingsTopicPreferencesStore(settings))
        advanceUntilIdle()

        assertEquals(listOf(Topic.Kotlin, aiTopic), relaunched.topics.value)
        assertTrue(relaunched.pendingNewTopics.value.isEmpty())
        secondScope.cancel()
    }
}
