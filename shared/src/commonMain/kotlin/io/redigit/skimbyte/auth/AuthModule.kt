package io.redigit.skimbyte.auth

import com.russhwolf.settings.Settings
import io.redigit.skimbyte.auth.ui.LoginSheetViewModel
import io.redigit.skimbyte.network.createHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val authHttpClientQualifier = named("authHttpClient")

val authModule = module {
    single<Settings> { Settings() }
    single<SessionStorage> { SettingsSessionStorage(get()) }
    // The auth server is live independent of ApiConfig.DEMO_MODE (which still gates the
    // not-yet-live posts/bookmarks API), so it gets its own HttpClient rather than sharing the
    // demo-mode-toggled one injected into PostsApi.
    single(authHttpClientQualifier) { createHttpClient(get()) }
    single<AuthApi> { KtorAuthApi(get(authHttpClientQualifier)) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProfileRepository> {
        ProfileRepositoryImpl(get(), get<AuthRepository>().session, CoroutineScope(SupervisorJob() + Dispatchers.Default))
    }
    viewModel { LoginSheetViewModel(get()) }
}
