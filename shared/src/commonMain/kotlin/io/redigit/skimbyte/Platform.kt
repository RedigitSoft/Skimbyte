package io.redigit.skimbyte

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform