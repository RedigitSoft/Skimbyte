package io.redigit.skimbyte.data.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class MeResponseDto(
    val userId: String,
    val tenantId: String? = null,
    val sessionId: String? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    // Profile fields the server does not return yet (see SERVER_API.md). Declared nullable with
    // defaults so the client keeps deserializing today's payloads and automatically picks these up
    // once the endpoint starts sending them.
    val displayName: String? = null,
    val handle: String? = null,
    val bio: String? = null,
    val isVerified: Boolean? = null,
    val readCount: Int? = null,
    val savedCount: Int? = null,
    val streakDays: Int? = null,
    val weeklyReads: List<Int>? = null,
)
