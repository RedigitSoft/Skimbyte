package io.redigit.skimbyte.auth

import com.russhwolf.settings.Settings
import io.redigit.skimbyte.auth.ui.LoginSheetViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single<Settings> { Settings() }
    single<SessionStorage> { SettingsSessionStorage(get()) }
    single<AuthApi> { KtorAuthApi(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    viewModel { LoginSheetViewModel(get()) }
}
