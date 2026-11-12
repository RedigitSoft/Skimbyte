package io.redigit.skimbyte.util

/**
 * Public URLs on the Skimbyte project website (the MkDocs site deployed to GitHub Pages by
 * `.github/workflows/deploy-site.yml`). These ship in release builds, so they live in a committed
 * constant rather than the `local.properties`-backed `BackendConfig`.
 */
object WebsiteLinks {
    // TODO: replace once the GitHub remote exists and Pages is live
    //  (e.g. https://<owner>.github.io/Skimbyte).
    const val SITE_BASE_URL: String = "https://redigitsoft.github.io/Skimbyte"

    /** The hosted copy of PRIVACY.md (`docs-site/privacy.md`, served by MkDocs as a directory). */
    const val PRIVACY_POLICY_URL: String = "$SITE_BASE_URL/docs/privacy/"
}
