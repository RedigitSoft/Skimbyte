package io.redigit.skimbyte.bookmarks

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val bookmarksModule = module {
    viewModel { BookmarksViewModel(get(), get()) }
}
