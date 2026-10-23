package io.redigit.skimbyte.ui.screens.auth

data class AccountState(
    val isLoggedIn: Boolean = false,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val avatarUploadInProgress: Boolean = false,
    val bannerUploadInProgress: Boolean = false,
    val avatarError: String? = null,
    val bannerError: String? = null,
)
