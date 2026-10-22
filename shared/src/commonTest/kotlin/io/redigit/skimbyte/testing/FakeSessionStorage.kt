package io.redigit.skimbyte.testing

import io.redigit.skimbyte.data.local.SessionStorage

class FakeSessionStorage(accessToken: String? = null, refreshToken: String? = null) : SessionStorage {
    private var access = accessToken
    private var refresh = refreshToken

    override fun accessToken(): String? = access
    override fun refreshToken(): String? = refresh
    override fun save(accessToken: String, refreshToken: String) {
        access = accessToken
        refresh = refreshToken
    }
    override fun clear() {
        access = null
        refresh = null
    }
}
