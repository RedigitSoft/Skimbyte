package io.redigit.skimbyte.ui.screens.onboarding

sealed interface TopicsEffect {
    /** Selection persisted; the caller decides where to navigate (main app vs. pop back). */
    data object Confirmed : TopicsEffect
}
