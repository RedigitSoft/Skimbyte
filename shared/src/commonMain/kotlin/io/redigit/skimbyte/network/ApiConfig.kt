package io.redigit.skimbyte.network

object ApiConfig {
    // Left blank intentionally: no live API exists yet. Set this once one is stood up, then
    // flip DEMO_MODE to false below.
    const val BASE_URL = ""
    const val ASSET_BASE_URL = BASE_URL

    // TODO: flip to false once a live API is reachable and the contract below is confirmed.
    const val DEMO_MODE = true

    fun resolveImageUrl(raw: String): String =
        if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "$ASSET_BASE_URL$raw"
}
