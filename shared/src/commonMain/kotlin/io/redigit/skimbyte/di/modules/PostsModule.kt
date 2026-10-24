package io.redigit.skimbyte.di.modules

import io.redigit.skimbyte.data.remote.PostsApi
import io.redigit.skimbyte.data.remote.KtorPostsApi
import io.redigit.skimbyte.data.repositories.BookmarkIdsStore
import io.redigit.skimbyte.data.repositories.PostsRepository
import io.redigit.skimbyte.data.repositories.PostsRepositoryImpl

import org.koin.dsl.module

val postsModule = module {
    single<PostsApi> { KtorPostsApi(get()) }
    single { BookmarkIdsStore(get()) }
    single<PostsRepository> { PostsRepositoryImpl(get(), get()) }
}
