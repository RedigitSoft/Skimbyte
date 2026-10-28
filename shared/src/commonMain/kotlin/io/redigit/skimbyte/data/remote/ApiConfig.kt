package io.redigit.skimbyte.data.remote

object ApiConfig {
    // skimbyte-server's base URL — sourced from local.properties (gitignored) via the generated
    // BackendConfig, same as AUTH_BASE_URL below. See :shared's generateAuthConfig Gradle task
    // and local.properties.example (key: skimbyteApiBaseUrl).
    const val BASE_URL = BackendConfig.API_BASE_URL

    // Prepended to relative imageUrl paths from the API ("/assets/placeholder-kotlin.png") by
    // resolveImageUrl below. Its own local.properties key (skimbyteAssetBaseUrl) so assets can
    // move to a CDN without touching the API base; defaults to BASE_URL when unset.
    const val ASSET_BASE_URL = BackendConfig.ASSET_BASE_URL

    // The auth server is live independent of DEMO_MODE — see AuthModule's dedicated HttpClient.
    const val AUTH_BASE_URL = BackendConfig.AUTH_BASE_URL

    // the auth server's product id for this app (provisioned via POST /admin/products) — sent on every
    // register/login call so the auth server knows which app the auth event is for.
    const val PRODUCT_ID = BackendConfig.PRODUCT_ID

    // GitHub OAuth start endpoint (/auth/oauth/github/start) isn't live on the auth server yet. The
    // client flow is fully scaffolded (start URL + shared redirect handling, see GoogleAuthCallback
    // which bridges any provider's token redirect); flip this to true once the endpoint exists and
    // the GitHub button will begin the real flow instead of showing "coming soon".
    const val GITHUB_SIGN_IN_ENABLED = false

    // Sign in with Apple follows the same provider-/start shape as Google/GitHub. Flip once the
    // auth server's /auth/oauth/apple/start endpoint exists (needs an Apple Services ID + key —
    // see the Apple notes in the auth server's docs); until then the button shows "coming soon".
    const val APPLE_SIGN_IN_ENABLED = false

    // Falls back to the mock network module automatically when no real API URL is configured,
    // so a fresh clone without local.properties still runs end to end on fake data.
    val DEMO_MODE = BASE_URL.isBlank()

    // DigitalOcean Spaces CDN domain for avatar/banner images — a different host than
    // ASSET_BASE_URL (skimbyte-server's own asset base, used by resolveImageUrl below).
    const val SPACES_CDN_BASE_URL = BackendConfig.SPACES_CDN_BASE_URL

    fun resolveImageUrl(raw: String): String =
        if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "$ASSET_BASE_URL$raw"

    fun resolveSpacesUrl(raw: String): String =
        if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "$SPACES_CDN_BASE_URL/$raw"
}
