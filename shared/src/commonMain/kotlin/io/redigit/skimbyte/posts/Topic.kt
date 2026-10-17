package io.redigit.skimbyte.posts

// apiValue strings are a best-guess default pending a live API to confirm against.
enum class Topic(val displayLabel: String, val apiValue: String?) {
    All("All", apiValue = null),
    Kotlin("Kotlin", "Kotlin"),
    Android("Android", "Android"),
    Kmp("KMP", "KMP / Compose Multiplatform"),
    Tooling("Tooling", "Tooling & Products"),
}
