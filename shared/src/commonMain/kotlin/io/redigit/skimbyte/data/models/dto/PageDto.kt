package io.redigit.skimbyte.data.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageDto<T>(
    val items: List<T>,
    val nextCursor: String?,
)
