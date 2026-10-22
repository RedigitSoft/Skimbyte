package io.redigit.skimbyte.auth

import io.redigit.skimbyte.testing.FakeSessionStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RefreshCoordinatorTest {

    @Test
    fun refresh_rotatesAndPersistsWhenPresentedTokenMatchesStorage() = runTest {
        val sessionStorage = FakeSessionStorage(accessToken = "old-access", refreshToken = "refresh-0")
        val coordinator = RefreshCoordinator(sessionStorage)

        val result = coordinator.refresh("refresh-0") { "new-access" to "refresh-1" }

        assertEquals("new-access" to "refresh-1", result)
        assertEquals("new-access", sessionStorage.accessToken())
        assertEquals("refresh-1", sessionStorage.refreshToken())
    }

    @Test
    fun refresh_skipsNetworkAndReturnsStoredTokensWhenPresentedTokenAlreadyRotated() = runTest {
        // Simulates the other HttpClient having already won the race and rotated the token.
        val sessionStorage = FakeSessionStorage(accessToken = "winner-access", refreshToken = "refresh-1")
        val coordinator = RefreshCoordinator(sessionStorage)
        var rotateCalls = 0

        val result = coordinator.refresh("refresh-0") {
            rotateCalls++
            "should-not-be-used" to "should-not-be-used"
        }

        assertEquals(0, rotateCalls)
        assertEquals("winner-access" to "refresh-1", result)
    }

    @Test
    fun refresh_returnsNullWhenRotateFails() = runTest {
        val sessionStorage = FakeSessionStorage(accessToken = "old-access", refreshToken = "refresh-0")
        val coordinator = RefreshCoordinator(sessionStorage)

        val result = coordinator.refresh("refresh-0") { null }

        assertNull(result)
        assertEquals("old-access", sessionStorage.accessToken())
    }
}
