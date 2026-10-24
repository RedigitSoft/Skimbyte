package io.redigit.skimbyte.data.local

import com.russhwolf.settings.Settings

/** Remembers whether the first-run tutorial has been shown, so it only appears once. */
interface TutorialStore {
    fun hasSeenTutorial(): Boolean
    fun setTutorialSeen()
}

class SettingsTutorialStore(private val settings: Settings) : TutorialStore {
    override fun hasSeenTutorial(): Boolean = settings.getBoolean(KEY_TUTORIAL_SEEN, false)
    override fun setTutorialSeen() = settings.putBoolean(KEY_TUTORIAL_SEEN, true)

    private companion object {
        const val KEY_TUTORIAL_SEEN = "tutorial_seen"
    }
}
