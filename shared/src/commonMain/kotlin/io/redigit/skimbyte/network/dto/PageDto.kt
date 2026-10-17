package io.redigit.skimbyte.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageDto<T>(
    val items: List<T>,
    val nextCursor: String?,
)
