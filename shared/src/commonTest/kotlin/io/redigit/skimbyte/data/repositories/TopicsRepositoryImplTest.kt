package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.models.dto.TopicDto
import io.redigit.skimbyte.data.remote.TopicsApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

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

class TopicsRepositoryImplTest {

    @Test
    fun `seeds with the selectable defaults before the initial fetch completes`() = runTest {
        val repository = TopicsRepositoryImpl(FakeTopicsApi { serverTopics }, this)

        // The init-launched refresh hasn't run yet on the test scheduler.
        assertEquals(Topic.selectable, repository.topics.value)
        advanceUntilIdle() // let the init fetch finish so runTest doesn't flag a leaked job
    }

    @Test
    fun `initial fetch replaces the seeds, surfacing topics added server-side`() = runTest {
        val api = FakeTopicsApi { serverTopics }
        val repository = TopicsRepositoryImpl(api, this)
        advanceUntilIdle()

        assertEquals(1, api.fetchCount)
        assertEquals(
            listOf(
                Topic(id = "kotlin", displayLabel = "Kotlin", apiValue = "Kotlin"),
                Topic(id = "ai", displayLabel = "AI", apiValue = "AI"),
            ),
            repository.topics.value,
        )
    }

    @Test
    fun `failed fetch keeps the seeded defaults`() = runTest {
        val repository = TopicsRepositoryImpl(FakeTopicsApi { error("endpoint down") }, this)
        advanceUntilIdle()

        assertEquals(Topic.selectable, repository.topics.value)
    }

    @Test
    fun `empty fetch keeps the seeded defaults`() = runTest {
        val repository = TopicsRepositoryImpl(FakeTopicsApi { emptyList() }, this)
        advanceUntilIdle()

        assertEquals(Topic.selectable, repository.topics.value)
    }

    @Test
    fun `refresh picks up a topic added after the initial fetch`() = runTest {
        var topics = listOf(TopicDto(id = "kotlin", label = "Kotlin", apiValue = "Kotlin"))
        val api = FakeTopicsApi { topics }
        val repository = TopicsRepositoryImpl(api, this)
        advanceUntilIdle()

        topics = serverTopics // the backend gains a new topic between fetches
        repository.refresh()

        assertEquals(2, api.fetchCount)
        assertEquals(
            listOf(
                Topic(id = "kotlin", displayLabel = "Kotlin", apiValue = "Kotlin"),
                Topic(id = "ai", displayLabel = "AI", apiValue = "AI"),
            ),
            repository.topics.value,
        )
    }
}