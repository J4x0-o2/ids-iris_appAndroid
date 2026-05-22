package com.j4x.iris_ids.domain.repository

interface AuthRepository {
    suspend fun loginAdmin(username: String, password: String): Result<String>
    fun getAdminToken(): String?
    fun clearAdminToken()
}
