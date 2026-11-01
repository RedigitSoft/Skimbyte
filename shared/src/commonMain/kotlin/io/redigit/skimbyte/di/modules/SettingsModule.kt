package io.redigit.skimbyte.di.modules

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

import io.redigit.skimbyte.ui.screens.settings.SettingsViewModel

val settingsModule = module {
    viewModel { SettingsViewModel(get()) }
}
