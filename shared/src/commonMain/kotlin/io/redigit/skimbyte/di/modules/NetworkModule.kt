package io.redigit.skimbyte.di.modules

import io.ktor.client.HttpClient
import io.redigit.skimbyte.data.remote.createHttpClient
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> { createHttpClient(get(), get()) }
}
