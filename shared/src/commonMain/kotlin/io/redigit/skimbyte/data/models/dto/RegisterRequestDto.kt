package io.redigit.skimbyte.data.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val productId: String,
)
