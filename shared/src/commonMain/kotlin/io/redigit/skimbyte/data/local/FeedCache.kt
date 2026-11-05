package io.redigit.skimbyte.data.local

import com.russhwolf.settings.Settings
import io.redigit.skimbyte.data.models.dto.PageDto
import io.redigit.skimbyte.data.models.dto.PostDto
import kotlinx.serialization.json.Json

/**
 * Persists the first page of each topic's feed so the app opens on the last-seen brief when it's
 * offline or still loading, instead of a blank error screen. Only the first page is cached — deep
 * pagination isn't worth persisting and always refetches online.
 */
interface FeedCache {
    fun save(topicKey: String, page: PageDto<PostDto>)
    fun load(topicKey: String): PageDto<PostDto>?
}

/**
 * Backed by multiplatform-settings (Web Storage on wasmJs, UserDefaults/SharedPreferences on
 * native), the same store the rest of the app's local state uses — see [TopicPreferencesStore].
 */
class SettingsFeedCache(private val settings: Settings) : FeedCache {
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = PageDto.serializer(PostDto.serializer())

    override fun save(topicKey: String, page: PageDto<PostDto>) {
        // Don't let a serialization hiccup take down a successful fetch.
        runCatching { settings.putString(key(topicKey), json.encodeToString(serializer, page)) }
    }

    override fun load(topicKey: String): PageDto<PostDto>? =
        settings.getStringOrNull(key(topicKey))?.let { stored ->
            runCatching { json.decodeFromString(serializer, stored) }.getOrNull()
        }

    private fun key(topicKey: String) = "$KEY_PREFIX$topicKey"

    private companion object {
        const val KEY_PREFIX = "feed_cache_"
    }
}
