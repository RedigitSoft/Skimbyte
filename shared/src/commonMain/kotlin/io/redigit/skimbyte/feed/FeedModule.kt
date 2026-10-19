package io.redigit.skimbyte.feed

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val feedModule = module {
    viewModel { FeedViewModel(get(), get(), get(), get()) }
    viewModel { SearchViewModel(get()) }
}
