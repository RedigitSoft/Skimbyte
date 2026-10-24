package io.redigit.skimbyte.di.modules

import io.redigit.skimbyte.data.local.SettingsTopicPreferencesStore
import io.redigit.skimbyte.data.local.TopicPreferencesStore
import io.redigit.skimbyte.ui.screens.onboarding.TopicsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    single<TopicPreferencesStore> { SettingsTopicPreferencesStore(get()) }
    viewModel { (isEditing: Boolean) -> TopicsViewModel(get(), isEditing) }
}
