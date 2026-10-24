package io.redigit.skimbyte.di

import io.redigit.skimbyte.di.modules.authModule
import io.redigit.skimbyte.bookmarks.bookmarksModule
import io.redigit.skimbyte.feed.feedModule
import io.redigit.skimbyte.data.remote.ApiConfig
import io.redigit.skimbyte.di.modules.networkModule
import io.redigit.skimbyte.network.demoNetworkModule
import io.redigit.skimbyte.onboarding.onboardingModule
import io.redigit.skimbyte.di.modules.postsModule
import io.redigit.skimbyte.di.modules.settingsModule
import io.redigit.skimbyte.theme.themeModule
import org.koin.dsl.module

/**
 * Root Koin module for the shared app code. Started per-composition via
 * [org.koin.compose.KoinApplication] rather than a global `startKoin()` call, so it works
 * identically at runtime and inside @Preview functions on every target.
 */
val appModule = module {
    includes(
        if (ApiConfig.DEMO_MODE) demoNetworkModule else networkModule,
        authModule,
        postsModule,
        feedModule,
        bookmarksModule,
        themeModule,
        onboardingModule,
        settingsModule,
    )
}
