package io.redigit.skimbyte.di

import io.redigit.skimbyte.Greeting
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Root Koin module for the shared design system and app code. Started per-composition via
 * [org.koin.compose.KoinApplication] rather than a global `startKoin()` call, so it works
 * identically at runtime and inside @Preview functions on every target.
 */
val appModule = module {
    factoryOf(::Greeting)
}
