package io.redigit.skimbyte.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
    val productId: String? = null,
)
