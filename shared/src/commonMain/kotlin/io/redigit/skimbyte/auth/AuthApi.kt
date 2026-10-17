package io.redigit.skimbyte.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.redigit.skimbyte.auth.dto.LoginRequestDto
import io.redigit.skimbyte.auth.dto.LoginResponseDto
import io.redigit.skimbyte.auth.dto.RefreshRequestDto
import io.redigit.skimbyte.auth.dto.RegisterRequestDto
import io.redigit.skimbyte.network.ApiConfig

// Talks to the live auth server (auth-server, the auth server) at ApiConfig.AUTH_BASE_URL — routes and
// request/response shapes confirmed against its source (routes/AuthRoutes.kt, routes/Dtos.kt).
interface AuthApi {
    suspend fun login(email: String, password: String): LoginResponseDto
    suspend fun register(email: String, password: String): LoginResponseDto
    suspend fun refresh(refreshToken: String): LoginResponseDto
}

class KtorAuthApi(private val client: HttpClient) : AuthApi {

    override suspend fun login(email: String, password: String): LoginResponseDto =
        client.post("${ApiConfig.AUTH_BASE_URL}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email = email, password = password))
        }.body()

    override suspend fun register(email: String, password: String): LoginResponseDto =
        client.post("${ApiConfig.AUTH_BASE_URL}/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(email = email, password = password))
        }.body()

    override suspend fun refresh(refreshToken: String): LoginResponseDto =
        client.post("${ApiConfig.AUTH_BASE_URL}/auth/token/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequestDto(refreshToken = refreshToken))
        }.body()
}
