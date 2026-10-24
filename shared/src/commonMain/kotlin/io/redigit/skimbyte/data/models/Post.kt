package io.redigit.skimbyte.data.models

import io.redigit.skimbyte.data.remote.ApiConfig
import io.redigit.skimbyte.data.models.dto.PostDto
import io.redigit.skimbyte.util.decodeHtmlEntities
import kotlin.time.Instant

data class Post(
    val id: String,
    val topic: String,
    val title: String,
    val summaryMarkdown: String,
    val sourceName: String,
    val sourceUrl: String,
    val imageUrl: String?,
    val digestDate: String?,
    val publishedAt: Instant,
    val isBookmarked: Boolean,
)

fun PostDto.toDomain(isBookmarked: Boolean): Post = Post(
    id = id,
    topic = topic,
    title = title.decodeHtmlEntities(),
    summaryMarkdown = summaryMarkdown.decodeHtmlEntities(),
    sourceName = sourceName,
    sourceUrl = sourceUrl,
    imageUrl = imageUrl?.let(ApiConfig::resolveImageUrl),
    digestDate = digestDate,
    publishedAt = publishedAt,
    isBookmarked = isBookmarked,
)
