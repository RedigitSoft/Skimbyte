package io.redigit.skimbyte.util

/**
 * Public URLs on the Skimbyte project website (the MkDocs site deployed to GitHub Pages by
 * `.github/workflows/deploy-site.yml`). These ship in release builds, so they live in a committed
 * constant rather than the `local.properties`-backed `BackendConfig`.
 */
object WebsiteLinks {
    // TODO: replace once the GitHub remote exists and Pages is live
    //  (the Pages custom domain; see website/CNAME).
    const val SITE_BASE_URL: String = "https://skimbyte.com"

    /** The hosted copy of PRIVACY.md (`docs-site/privacy.md`, served by MkDocs as a directory). */
    const val PRIVACY_POLICY_URL: String = "$SITE_BASE_URL/docs/privacy/"

    /** The hosted contact page (`docs-site/contact.md`), mirrored in-app by `ContactScreen`. */
    const val CONTACT_URL: String = "$SITE_BASE_URL/docs/contact/"

    /** Point of contact for the app and the developer — shown in-app and on the contact page. */
    const val CONTACT_EMAIL: String = "akshay@redigit.io"

    /** Public issue tracker for bug reports and feature requests. */
    const val ISSUES_URL: String = "https://github.com/RedigitSoft/Skimbyte/issues"
}
