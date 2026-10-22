package io.redigit.skimbyte.data.models

import io.redigit.skimbyte.data.models.dto.MeResponseDto
import io.redigit.skimbyte.network.ApiConfig

data class Profile(val userId: String, val avatarUrl: String?, val bannerUrl: String?)

fun MeResponseDto.toDomain(): Profile = Profile(
    userId = userId,
    avatarUrl = avatarUrl?.let(ApiConfig::resolveSpacesUrl),
    bannerUrl = bannerUrl?.let(ApiConfig::resolveSpacesUrl),
)
