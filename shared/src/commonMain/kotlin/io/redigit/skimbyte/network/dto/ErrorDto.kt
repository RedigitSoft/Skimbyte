package io.redigit.skimbyte.network.dto

import kotlinx.serialization.Serializable

// PLACEHOLDER-PENDING-BACKEND-CONFIRMATION
@Serializable
data class ErrorEnvelopeDto(val error: ErrorDto)

@Serializable
data class ErrorDto(val code: String, val message: String)
