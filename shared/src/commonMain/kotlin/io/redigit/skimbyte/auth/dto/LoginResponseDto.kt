package io.redigit.skimbyte.auth.dto

import kotlinx.serialization.Serializable

// PLACEHOLDER-PENDING-BACKEND-CONFIRMATION
@Serializable
data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)
