package io.redigit.skimbyte.data.models

import io.redigit.skimbyte.data.models.dto.MeResponseDto
import io.redigit.skimbyte.data.remote.ApiConfig

/**
 * The logged-in user's profile. [userId], [avatarUrl] and [bannerUrl] come from the live `/me`
 * endpoint today; the identity fields ([displayName], [handle], [bio], [isVerified]) and
 * [savedCount] back the profile screen and are not all returned by the server yet - they stay
 * null until it does, at which point they flow through [toDomain] unchanged.
 */
data class Profile(
    val userId: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val email: String? = null,
    val displayName: String? = null,
    val handle: String? = null,
    val bio: String? = null,
    val isVerified: Boolean = false,
    val savedCount: Int? = null,
)

fun MeResponseDto.toDomain(): Profile = Profile(
    userId = userId,
    avatarUrl = avatarUrl?.let(ApiConfig::resolveSpacesUrl),
    bannerUrl = bannerUrl?.let(ApiConfig::resolveSpacesUrl),
    email = email,
    displayName = displayName,
    handle = handle,
    bio = bio,
    isVerified = isVerified ?: false,
    savedCount = savedCount,
)
