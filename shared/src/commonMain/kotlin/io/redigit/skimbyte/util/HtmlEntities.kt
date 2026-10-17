package io.redigit.skimbyte.util

// The Markdown renderer does not decode HTML character references on its own (confirmed by
// testing), but real feed content contains them (e.g. "&#8217;" from blog RSS summaries), so this
// decodes them once at the data layer before the text ever reaches the renderer.
private val namedEntities = mapOf(
    "amp" to "&",
    "lt" to "<",
    "gt" to ">",
    "quot" to "\"",
    "apos" to "'",
    "nbsp" to " ",
)

private val entityRegex = Regex("&(#x[0-9a-fA-F]+|#[0-9]+|[a-zA-Z]+);")

fun String.decodeHtmlEntities(): String = entityRegex.replace(this) { match ->
    val body = match.groupValues[1]
    when {
        body.startsWith("#x", ignoreCase = true) -> body.drop(2).toIntOrNull(16)?.let(::codePointToString) ?: match.value
        body.startsWith("#") -> body.drop(1).toIntOrNull()?.let(::codePointToString) ?: match.value
        else -> namedEntities[body] ?: match.value
    }
}

private fun codePointToString(codePoint: Int): String = if (codePoint <= 0xFFFF) {
    Char(codePoint).toString()
} else {
    val adjusted = codePoint - 0x10000
    val high = (adjusted shr 10) + 0xD800
    val low = (adjusted and 0x3FF) + 0xDC00
    charArrayOf(high.toChar(), low.toChar()).concatToString()
}
