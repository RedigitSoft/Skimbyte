package io.redigit.skimbyte.util

import kotlin.test.Test
import kotlin.test.assertTrue

class WebsiteLinksTest {

    @Test
    fun siteBaseUrlIsHttpsWithoutTrailingSlash() {
        assertTrue(WebsiteLinks.SITE_BASE_URL.startsWith("https://"))
        assertTrue(!WebsiteLinks.SITE_BASE_URL.endsWith("/"))
    }

    @Test
    fun privacyPolicyUrlIsUnderTheSiteWithMkDocsDirectoryShape() {
        // MkDocs serves docs-site/privacy.md as <site>/privacy/ — the trailing slash avoids a
        // redirect hop on GitHub Pages.
        assertTrue(WebsiteLinks.PRIVACY_POLICY_URL.startsWith(WebsiteLinks.SITE_BASE_URL + "/"))
        assertTrue(WebsiteLinks.PRIVACY_POLICY_URL.endsWith("/privacy/"))
    }
}
