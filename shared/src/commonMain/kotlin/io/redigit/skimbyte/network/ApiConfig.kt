package io.redigit.skimbyte.network

import io.redigit.skimbyte.auth.BackendConfig

object ApiConfig {
    // skimbyte-server's base URL — sourced from local.properties (gitignored) via the generated
    // BackendConfig, same as AUTH_BASE_URL below. See :shared's generateAuthConfig Gradle task
    // and local.properties.example (key: skimbyteApiBaseUrl).
    const val BASE_URL = BackendConfig.API_BASE_URL
    const val ASSET_BASE_URL = BASE_URL

    // The auth server is live independent of DEMO_MODE — see AuthModule's dedicated HttpClient.
    const val AUTH_BASE_URL = BackendConfig.AUTH_BASE_URL

    // Falls back to the mock network module automatically when no real API URL is configured,
    // so a fresh clone without local.properties still runs end to end on fake data.
    val DEMO_MODE = BASE_URL.isBlank()

    fun resolveImageUrl(raw: String): String =
        if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "$ASSET_BASE_URL$raw"
}
