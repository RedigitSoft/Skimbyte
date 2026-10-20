package io.redigit.skimbyte.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class MeResponseDto(
    val userId: String,
    val tenantId: String? = null,
    val sessionId: String? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
)
