package com.j4x.iris_ids.domain.usecase

import com.j4x.iris_ids.domain.repository.AuthRepository
import javax.inject.Inject

class LoginAdminUseCase @Inject constructor(
    private val repo: AuthRepository,
) {
    suspend operator fun invoke(username: String, password: String): Result<String> =
        repo.loginAdmin(username, password)
}
