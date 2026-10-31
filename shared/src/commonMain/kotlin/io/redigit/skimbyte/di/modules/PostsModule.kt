package io.redigit.skimbyte.di.modules

import io.redigit.skimbyte.data.remote.PostsApi
import io.redigit.skimbyte.data.remote.KtorPostsApi
import io.redigit.skimbyte.data.remote.KtorMeApi
import io.redigit.skimbyte.data.remote.MeApi
import io.redigit.skimbyte.data.repositories.BookmarkIdsStore
import io.redigit.skimbyte.data.repositories.PostsRepository
import io.redigit.skimbyte.data.repositories.PostsRepositoryImpl
import io.redigit.skimbyte.data.repositories.UserDataRepository
import io.redigit.skimbyte.data.repositories.UserDataRepositoryImpl

import org.koin.dsl.module

val postsModule = module {
    single<PostsApi> { KtorPostsApi(get()) }
    // Shares the main (demo-aware) HttpClient with PostsApi — both call skimbyte-server.
    single<MeApi> { KtorMeApi(get()) }
    single<UserDataRepository> { UserDataRepositoryImpl(get()) }
    single { BookmarkIdsStore(get()) }
    single<PostsRepository> { PostsRepositoryImpl(get(), get()) }
}
