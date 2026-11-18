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
        // MkDocs serves docs-site/privacy.md as <site>/docs/privacy/ — the trailing slash avoids a
        // redirect hop on GitHub Pages.
        assertTrue(WebsiteLinks.PRIVACY_POLICY_URL.startsWith(WebsiteLinks.SITE_BASE_URL + "/"))
        assertTrue(WebsiteLinks.PRIVACY_POLICY_URL.endsWith("/privacy/"))
    }

    @Test
    fun contactUrlIsUnderTheSiteWithMkDocsDirectoryShape() {
        // docs-site/contact.md is served as <site>/docs/contact/ — the in-app ContactScreen links here.
        assertTrue(WebsiteLinks.CONTACT_URL.startsWith(WebsiteLinks.SITE_BASE_URL + "/"))
        assertTrue(WebsiteLinks.CONTACT_URL.endsWith("/contact/"))
    }

    @Test
    fun contactEmailLooksLikeAnAddress() {
        // Play's News & Magazines policy requires a real contact email; a bare @-less string would
        // silently produce a broken mailto:.
        assertTrue(WebsiteLinks.CONTACT_EMAIL.contains("@"))
        assertTrue(WebsiteLinks.CONTACT_EMAIL.substringAfter("@").contains("."))
    }
}
