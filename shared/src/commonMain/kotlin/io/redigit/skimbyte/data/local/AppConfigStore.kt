package io.redigit.skimbyte.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** How long each story summary should be, shown on the profile's app-config section. */
enum class SummaryLength(val label: String) {
    Short("40–50w"),
    Medium("60–80w"),
    Long("100–120w"),
}

/**
 * User-tunable feed/app preferences surfaced on the profile screen (daily digest time, summary
 * length, notifications). Backed by multiplatform-settings, same as the other local stores.
 */
interface AppConfigStore {
    val digestTime: StateFlow<String>
    val summaryLength: StateFlow<SummaryLength>
    val notificationsEnabled: StateFlow<Boolean>
    fun cycleDigestTime()
    fun cycleSummaryLength()
    fun setNotificationsEnabled(enabled: Boolean)
}

class SettingsAppConfigStore(private val settings: Settings) : AppConfigStore {

    private val _digestTime = MutableStateFlow(settings.getString(KEY_DIGEST, DEFAULT_DIGEST))
    override val digestTime: StateFlow<String> = _digestTime.asStateFlow()

    private val _summaryLength = MutableStateFlow(readSummaryLength())
    override val summaryLength: StateFlow<SummaryLength> = _summaryLength.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(settings.getBoolean(KEY_NOTIFICATIONS, true))
    override val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    override fun cycleDigestTime() {
        val currentIndex = DIGEST_TIMES.indexOf(_digestTime.value).coerceAtLeast(0)
        val next = DIGEST_TIMES[(currentIndex + 1) % DIGEST_TIMES.size]
        settings.putString(KEY_DIGEST, next)
        _digestTime.value = next
    }

    override fun cycleSummaryLength() {
        val next = SummaryLength.entries[(_summaryLength.value.ordinal + 1) % SummaryLength.entries.size]
        settings.putString(KEY_SUMMARY, next.name)
        _summaryLength.value = next
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_NOTIFICATIONS, enabled)
        _notificationsEnabled.value = enabled
    }

    private fun readSummaryLength(): SummaryLength {
        val stored = settings.getStringOrNull(KEY_SUMMARY)
        return SummaryLength.entries.firstOrNull { it.name == stored } ?: SummaryLength.Medium
    }

    private companion object {
        const val KEY_DIGEST = "config_digest_time"
        const val KEY_SUMMARY = "config_summary_length"
        const val KEY_NOTIFICATIONS = "config_notifications_enabled"
        const val DEFAULT_DIGEST = "08:30"
        val DIGEST_TIMES = listOf("07:00", "08:30", "12:00", "18:00", "21:00")
    }
}
