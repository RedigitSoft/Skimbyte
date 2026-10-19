package io.redigit.skimbyte.onboarding

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    single<TopicPreferencesStore> { SettingsTopicPreferencesStore(get()) }
    viewModel { (isEditing: Boolean) -> TopicsViewModel(get(), isEditing) }
}
