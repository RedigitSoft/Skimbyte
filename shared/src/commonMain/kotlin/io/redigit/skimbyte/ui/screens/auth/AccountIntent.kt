package io.redigit.skimbyte.ui.screens.auth

import io.redigit.skimbyte.util.PickedImage

sealed interface AccountIntent {
    data object Logout : AccountIntent
    data class AvatarPicked(val image: PickedImage) : AccountIntent
    data class BannerPicked(val image: PickedImage) : AccountIntent
    data object AvatarRemoved : AccountIntent
    data object BannerRemoved : AccountIntent
    data object DigestTimeCycled : AccountIntent
    data object SummaryLengthCycled : AccountIntent
    data class NotificationsToggled(val enabled: Boolean) : AccountIntent
}
