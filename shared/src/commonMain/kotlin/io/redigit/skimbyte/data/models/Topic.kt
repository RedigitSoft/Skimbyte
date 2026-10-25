package io.redigit.skimbyte.data.models

/**
 * A feed topic. Previously a fixed `enum`; now a value type so the set of topics can be served by
 * the backend (see `TopicsRepository`, which calls the now-live `GET /v1/topics`) instead of being
 * hardcoded. [All] is a client-only pseudo-topic (no [apiValue] = "no filter"). The named constants
 * and [defaults] are the seed set shown until that fetch completes, and also back previews and tests.
 */
data class Topic(val id: String, val displayLabel: String, val apiValue: String?) {
    companion object {
        val All = Topic(id = "all", displayLabel = "All", apiValue = null)
        val Kotlin = Topic(id = "kotlin", displayLabel = "Kotlin", apiValue = "Kotlin")
        val Android = Topic(id = "android", displayLabel = "Android", apiValue = "Android")
        // Ids must match the server's (slugified api value, see skimbyte-server's slugifyTopic and
        // V1__init.sql) so a topic picked from the fetched list equals its built-in counterpart.
        val Kmp = Topic(id = "kmp-compose-multiplatform", displayLabel = "KMP", apiValue = "KMP / Compose Multiplatform")
        val Tooling = Topic(id = "tooling-products", displayLabel = "Tooling", apiValue = "Tooling & Products")

        /** The full seed list (All first), shown until the real `GET /v1/topics` fetch completes. */
        val defaults: List<Topic> = listOf(All, Kotlin, Android, Kmp, Tooling)

        /** Selectable interests — everything except the client-only [All] pseudo-topic. */
        val selectable: List<Topic> = defaults.filter { it != All }
    }
}
