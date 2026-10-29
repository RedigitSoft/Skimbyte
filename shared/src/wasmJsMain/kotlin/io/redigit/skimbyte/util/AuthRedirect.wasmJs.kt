package io.redigit.skimbyte.util

import io.redigit.skimbyte.data.remote.BackendConfig

actual fun authRedirectUri(): String = BackendConfig.GOOGLE_REDIRECT_URI
