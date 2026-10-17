package io.redigit.skimbyte.posts

import org.koin.dsl.module

val postsModule = module {
    single<PostsApi> { KtorPostsApi(get()) }
    single { BookmarkIdsStore(get()) }
    single<PostsRepository> { PostsRepositoryImpl(get(), get()) }
}
