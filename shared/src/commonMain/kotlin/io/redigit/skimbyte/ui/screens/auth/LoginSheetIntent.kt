package io.redigit.skimbyte.ui.screens.auth

sealed interface LoginSheetIntent {
    data class EmailChanged(val value: String) : LoginSheetIntent
    data class PasswordChanged(val value: String) : LoginSheetIntent
    data object ModeToggled : LoginSheetIntent
    data object Submit : LoginSheetIntent
    data object GoogleSignInStarted : LoginSheetIntent
    data class GoogleRedirect(val redirectUri: String) : LoginSheetIntent
    data object Dismissed : LoginSheetIntent
}
