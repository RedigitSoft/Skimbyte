package io.redigit.skimbyte.data.local

import com.russhwolf.settings.Settings

interface SessionStorage {
    fun accessToken(): String?
    fun refreshToken(): String?
    fun save(accessToken: String, refreshToken: String)
    fun clear()
}

/**
 * Backed by multiplatform-settings rather than androidx.datastore: DataStore's non-Android/web
 * targets are not yet shipped, while this library has full wasmJs support (Web Storage-backed),
 * which this app's target list requires.
 */
class SettingsSessionStorage(private val settings: Settings) : SessionStorage {

    override fun accessToken(): String? = settings.getStringOrNull(KEY_ACCESS_TOKEN)

    override fun refreshToken(): String? = settings.getStringOrNull(KEY_REFRESH_TOKEN)

    override fun save(accessToken: String, refreshToken: String) {
        settings.putString(KEY_ACCESS_TOKEN, accessToken)
        settings.putString(KEY_REFRESH_TOKEN, refreshToken)
    }

    override fun clear() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "session_access_token"
        const val KEY_REFRESH_TOKEN = "session_refresh_token"
    }
}
