package io.redigit.skimbyte.data.models

/**
 * A feed topic. Previously a fixed `enum`; now a value type so the set of topics can be served by
 * the backend (see `TopicsRepository`) instead of being hardcoded. [All] is a client-only
 * pseudo-topic (no [apiValue] = "no filter"). The named constants and [defaults] are the
 * seed/placeholder set used until `GET /topics` is live, and also back previews and tests.
 */
data class Topic(val id: String, val displayLabel: String, val apiValue: String?) {
    companion object {
        val All = Topic(id = "all", displayLabel = "All", apiValue = null)
        val Kotlin = Topic(id = "kotlin", displayLabel = "Kotlin", apiValue = "Kotlin")
        val Android = Topic(id = "android", displayLabel = "Android", apiValue = "Android")
        val Kmp = Topic(id = "kmp", displayLabel = "KMP", apiValue = "KMP / Compose Multiplatform")
        val Tooling = Topic(id = "tooling", displayLabel = "Tooling", apiValue = "Tooling & Products")

        /** The full seed list (All first), used as the placeholder until the topics API is live. */
        val defaults: List<Topic> = listOf(All, Kotlin, Android, Kmp, Tooling)

        /** Selectable interests — everything except the client-only [All] pseudo-topic. */
        val selectable: List<Topic> = defaults.filter { it != All }
    }
}
