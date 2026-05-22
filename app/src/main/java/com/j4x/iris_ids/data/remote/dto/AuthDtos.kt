package com.j4x.iris_ids.data.remote.dto

data class LoginRequest(
    val username: String,
    val password: String,
)

data class LoginResponse(
    val token: String,
)
