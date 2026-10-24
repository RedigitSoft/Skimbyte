package io.redigit.skimbyte.data.models

import io.redigit.skimbyte.data.models.dto.MeResponseDto
import io.redigit.skimbyte.data.remote.ApiConfig

/**
 * The logged-in user's profile. [userId], [avatarUrl] and [bannerUrl] come from the live `/me`
 * endpoint today; the remaining fields back the profile screen's identity header, stats, and
 * reading-velocity chart and are not yet returned by the server (see SERVER_API.md) - they stay
 * null/empty until it does, at which point they flow through [toDomain] unchanged.
 */
data class Profile(
    val userId: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val displayName: String? = null,
    val handle: String? = null,
    val bio: String? = null,
    val isVerified: Boolean = false,
    val readCount: Int? = null,
    val savedCount: Int? = null,
    val streakDays: Int? = null,
    val weeklyReads: List<Int> = emptyList(),
)

fun MeResponseDto.toDomain(): Profile = Profile(
    userId = userId,
    avatarUrl = avatarUrl?.let(ApiConfig::resolveSpacesUrl),
    bannerUrl = bannerUrl?.let(ApiConfig::resolveSpacesUrl),
    displayName = displayName,
    handle = handle,
    bio = bio,
    isVerified = isVerified ?: false,
    readCount = readCount,
    savedCount = savedCount,
    streakDays = streakDays,
    weeklyReads = weeklyReads ?: emptyList(),
)
