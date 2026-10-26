package io.redigit.skimbyte.di.modules

import io.redigit.skimbyte.data.local.SettingsTopicPreferencesStore
import io.redigit.skimbyte.data.local.SettingsTutorialStore
import io.redigit.skimbyte.data.local.TopicPreferencesStore
import io.redigit.skimbyte.data.local.TutorialStore
import io.redigit.skimbyte.data.remote.KtorTopicsApi
import io.redigit.skimbyte.data.remote.TopicsApi
import io.redigit.skimbyte.data.repositories.TopicsRepository
import io.redigit.skimbyte.data.repositories.TopicsRepositoryImpl
import io.redigit.skimbyte.ui.screens.onboarding.TopicsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    single<TopicPreferencesStore> { SettingsTopicPreferencesStore(get()) }
    single<TutorialStore> { SettingsTutorialStore(get()) }
    single<TopicsApi> { KtorTopicsApi(get()) }
    single<TopicsRepository> { TopicsRepositoryImpl(get(), get(), CoroutineScope(SupervisorJob() + Dispatchers.Default)) }
    viewModel { (isEditing: Boolean) -> TopicsViewModel(get(), get(), isEditing) }
}
