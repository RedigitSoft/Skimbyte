package io.redigit.skimbyte.ui.screens.auth

data class LoginSheetState(
    val email: String = "",
    val password: String = "",
    val isRegisterMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)
