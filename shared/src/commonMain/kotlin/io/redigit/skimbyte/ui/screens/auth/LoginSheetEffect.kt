package io.redigit.skimbyte.ui.screens.auth

sealed interface LoginSheetEffect {
    data object LoginSucceeded : LoginSheetEffect
}
