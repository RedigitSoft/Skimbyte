package io.redigit.skimbyte.onboarding

import org.koin.dsl.module

val onboardingModule = module {
    single<TopicPreferencesStore> { SettingsTopicPreferencesStore(get()) }
}
