package io.redigit.skimbyte.di.modules

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

import io.redigit.skimbyte.data.local.AppConfigStore
import io.redigit.skimbyte.data.local.SettingsAppConfigStore
import io.redigit.skimbyte.ui.screens.settings.SettingsViewModel

val settingsModule = module {
    single<AppConfigStore> { SettingsAppConfigStore(get()) }
    viewModel { SettingsViewModel(get()) }
}
