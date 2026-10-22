package io.redigit.skimbyte.data.models.dto

import kotlinx.serialization.Serializable

// PLACEHOLDER-PENDING-BACKEND-CONFIRMATION
@Serializable
data class ErrorEnvelopeDto(val error: ErrorDto)

@Serializable
data class ErrorDto(val code: String, val message: String)
