package io.redigit.skimbyte.posts.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

// PLACEHOLDER-PENDING-BACKEND-CONFIRMATION
@Serializable
data class PostDto(
    val id: String,
    val topic: String,
    val title: String,
    val summaryMarkdown: String,
    val sourceName: String,
    val sourceUrl: String,
    val imageUrl: String? = null,
    val digestDate: String? = null,
    val publishedAt: Instant,
)
