package io.redigit.skimbyte.di.modules

import io.redigit.skimbyte.ui.screens.feed.FeedViewModel
import io.redigit.skimbyte.ui.screens.feed.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val feedModule = module {
    viewModel { FeedViewModel(get(), get(), get(), get()) }
    viewModel { SearchViewModel(get()) }
}
