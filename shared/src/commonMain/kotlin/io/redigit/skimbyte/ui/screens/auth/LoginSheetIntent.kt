package io.redigit.skimbyte.ui.screens.auth

sealed interface LoginSheetIntent {
    data object AppleSignInStarted : LoginSheetIntent
    data object GithubSignInStarted : LoginSheetIntent
    data object GoogleSignInStarted : LoginSheetIntent
    data class GoogleRedirect(val redirectUri: String) : LoginSheetIntent
    data object Dismissed : LoginSheetIntent
}
