package io.redigit.skimbyte.data.remote

import io.redigit.skimbyte.data.models.dto.PageDto

data class Page<T>(
    val items: List<T>,
    val nextCursor: String?,
)

fun <T, R> PageDto<T>.toDomain(mapItem: (T) -> R): Page<R> =
    Page(items = items.map(mapItem), nextCursor = nextCursor)
