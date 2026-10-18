package io.redigit.skimbyte.theme

import org.koin.dsl.module

val themeModule = module {
    single<ThemeModeStore> { SettingsThemeModeStore(get()) }
}
