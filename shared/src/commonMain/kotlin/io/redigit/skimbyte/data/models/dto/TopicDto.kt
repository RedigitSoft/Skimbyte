package io.redigit.skimbyte.data.models.dto

import io.redigit.skimbyte.data.models.Topic
import kotlinx.serialization.Serializable

@Serializable
data class TopicDto(
    val id: String,
    val label: String,
    val apiValue: String? = null,
)
fun TopicDto.toDomain(): Topic = Topic(id = id, displayLabel = label, apiValue = apiValue)
