package io.redigit.skimbyte.di.modules

import io.redigit.skimbyte.ui.screens.bookmarks.BookmarksViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val bookmarksModule = module {
    viewModel { BookmarksViewModel(get(), get()) }
}
