package io.redigit.skimbyte.ui.screens.auth

sealed interface AccountEffect {
    /** "Delete my data" completed: the account is gone, so the account area resets and the app
     * returns to the feed rather than leaving the user on a screen about data that no longer exists. */
    data object DataDeleted : AccountEffect
}
