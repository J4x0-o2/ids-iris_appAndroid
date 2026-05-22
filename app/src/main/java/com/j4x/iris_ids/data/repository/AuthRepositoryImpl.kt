package com.j4x.iris_ids.data.repository

import com.j4x.iris_ids.data.local.prefs.TokenStore
import com.j4x.iris_ids.data.remote.api.IrisApi
import com.j4x.iris_ids.data.remote.dto.LoginRequest
import com.j4x.iris_ids.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: IrisApi,
    private val tokenStore: TokenStore,
) : AuthRepository {

    override suspend fun loginAdmin(username: String, password: String): Result<String> =
        runCatching {
            val token = api.loginAdmin(LoginRequest(username, password))
                .body()?.token ?: error("No token in response")
            tokenStore.save(token)
            token
        }

    override fun getAdminToken(): String? = tokenStore.get()
    override fun clearAdminToken() = tokenStore.clear()
}
