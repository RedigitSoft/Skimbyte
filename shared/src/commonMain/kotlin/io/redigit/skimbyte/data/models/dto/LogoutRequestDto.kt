package io.redigit.skimbyte.data.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class LogoutRequestDto(val refreshToken: String)
