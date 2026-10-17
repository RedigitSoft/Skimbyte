package io.redigit.skimbyte.auth.ui

data class LoginSheetUiState(
    val email: String = "",
    val password: String = "",
    val isRegisterMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)
