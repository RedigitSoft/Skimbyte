package io.redigit.skimbyte.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.redigit.skimbyte.data.models.dto.LoginRequestDto
import io.redigit.skimbyte.data.models.dto.LoginResponseDto
import io.redigit.skimbyte.data.models.dto.MeResponseDto
import io.redigit.skimbyte.data.models.dto.RefreshRequestDto
import io.redigit.skimbyte.data.models.dto.RegisterRequestDto
import io.redigit.skimbyte.util.PickedImage

// Talks to the live auth server at ApiConfig.AUTH_BASE_URL — routes and
// request/response shapes confirmed against its source (routes/AuthRoutes.kt, routes/Dtos.kt).
interface AuthApi {
    suspend fun login(email: String, password: String): LoginResponseDto
    suspend fun register(email: String, password: String): LoginResponseDto
    suspend fun refresh(refreshToken: String): LoginResponseDto
    suspend fun me(): MeResponseDto
    suspend fun uploadImage(type: String, image: PickedImage)
}

class KtorAuthApi(private val client: HttpClient) : AuthApi {

    override suspend fun login(email: String, password: String): LoginResponseDto =
        client.post("${ApiConfig.AUTH_BASE_URL}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email = email, password = password, productId = ApiConfig.PRODUCT_ID))
        }.body()

    override suspend fun register(email: String, password: String): LoginResponseDto =
        client.post("${ApiConfig.AUTH_BASE_URL}/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(email = email, password = password, productId = ApiConfig.PRODUCT_ID))
        }.body()

    override suspend fun refresh(refreshToken: String): LoginResponseDto =
        client.post("${ApiConfig.AUTH_BASE_URL}/auth/token/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequestDto(refreshToken = refreshToken))
        }.body()

    override suspend fun me(): MeResponseDto =
        client.get("${ApiConfig.AUTH_BASE_URL}/me").body()

    override suspend fun uploadImage(type: String, image: PickedImage) {
        client.post("${ApiConfig.AUTH_BASE_URL}/me/image") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("type", type)
                        append(
                            "file",
                            image.bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, image.mimeType)
                                append(HttpHeaders.ContentDisposition, "filename=\"upload\"")
                            },
                        )
                    },
                ),
            )
        }
    }
}
