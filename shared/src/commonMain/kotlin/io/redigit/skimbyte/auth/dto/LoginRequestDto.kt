package io.redigit.skimbyte.auth.dto

import kotlinx.serialization.Serializable

// PLACEHOLDER-PENDING-BACKEND-CONFIRMATION
@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)
