package io.redigit.skimbyte.auth

import com.russhwolf.settings.Settings
import io.redigit.skimbyte.auth.ui.AccountViewModel
import io.redigit.skimbyte.auth.ui.LoginSheetViewModel
import io.redigit.skimbyte.data.local.SessionStorage
import io.redigit.skimbyte.data.remote.AuthApi
import io.redigit.skimbyte.data.remote.KtorAuthApi
import io.redigit.skimbyte.data.remote.RefreshCoordinator
import io.redigit.skimbyte.data.local.SettingsSessionStorage
import io.redigit.skimbyte.data.remote.createHttpClient
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
    // Shared with networkModule's HttpClient so the two never race rotating the same
    // single-use refresh token — see RefreshCoordinator's doc comment.
    single { RefreshCoordinator(get()) }
    // The auth server is live independent of ApiConfig.DEMO_MODE (which still gates the
    // not-yet-live posts/bookmarks API), so it gets its own HttpClient rather than sharing the
    // demo-mode-toggled one injected into PostsApi.
    single(authHttpClientQualifier) { createHttpClient(get(), get()) }
    single<AuthApi> { KtorAuthApi(get(authHttpClientQualifier)) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProfileRepository> {
        ProfileRepositoryImpl(get(), get<AuthRepository>().session, CoroutineScope(SupervisorJob() + Dispatchers.Default))
    }
    viewModel { LoginSheetViewModel(get()) }
    viewModel { AccountViewModel(get(), get()) }
}
