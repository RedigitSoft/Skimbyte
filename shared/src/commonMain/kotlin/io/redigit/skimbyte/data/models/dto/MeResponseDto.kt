package io.redigit.skimbyte.data.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class MeResponseDto(
    val userId: String,
    val tenantId: String? = null,
    val sessionId: String? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
)
