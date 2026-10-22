package io.redigit.skimbyte.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.redigit.skimbyte.data.remote.RefreshCoordinator
import io.redigit.skimbyte.data.local.SessionStorage
import io.redigit.skimbyte.data.models.dto.LoginResponseDto
import io.redigit.skimbyte.data.models.dto.RefreshRequestDto
import io.redigit.skimbyte.data.remote.ApiConfig
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> { createHttpClient(get(), get()) }
}

fun createHttpClient(sessionStorage: SessionStorage, refreshCoordinator: RefreshCoordinator): HttpClient =
    HttpClient { configureSkimbyteClient(sessionStorage, refreshCoordinator) }

/** Test-only entry point so TokenRefreshTest can substitute a MockEngine. */
internal fun createHttpClient(sessionStorage: SessionStorage, refreshCoordinator: RefreshCoordinator, engine: HttpClientEngine): HttpClient =
    HttpClient(engine) { configureSkimbyteClient(sessionStorage, refreshCoordinator) }

/**
 * The refresh call is made with the HttpClient the Auth plugin itself hands to the
 * refreshTokens callback, which is not subject to Auth interception — calling through the
 * regular injected client here would recurse back into this same callback on a 401.
 */
private fun HttpClientConfig<*>.configureSkimbyteClient(sessionStorage: SessionStorage, refreshCoordinator: RefreshCoordinator) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) {
        // Not Logger.DEFAULT: that routes through SLF4J, which is a silent no-op without a
        // binding on the classpath (Android/iOS/JS have none) — this prints directly instead.
        logger = object : Logger {
            override fun log(message: String) = println("HttpClient: $message")
        }
        level = LogLevel.ALL
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }
    install(Auth) {
        bearer {
            loadTokens {
                val accessToken = sessionStorage.accessToken() ?: return@loadTokens null
                BearerTokens(accessToken = accessToken, refreshToken = sessionStorage.refreshToken().orEmpty())
            }
            refreshTokens {
                val refreshToken = sessionStorage.refreshToken() ?: return@refreshTokens null
                val (accessToken, newRefreshToken) = refreshCoordinator.refresh(refreshToken) { presented ->
                    val response = client.post("${ApiConfig.AUTH_BASE_URL}/auth/token/refresh") {
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequestDto(refreshToken = presented))
                    }.body<LoginResponseDto>()
                    response.accessToken to response.refreshToken
                } ?: return@refreshTokens null
                BearerTokens(accessToken = accessToken, refreshToken = newRefreshToken)
            }
        }
    }
}
