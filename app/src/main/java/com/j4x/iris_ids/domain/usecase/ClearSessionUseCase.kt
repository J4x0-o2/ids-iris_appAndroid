package com.j4x.iris_ids.domain.usecase

import com.j4x.iris_ids.domain.repository.SessionRepository
import javax.inject.Inject

class ClearSessionUseCase @Inject constructor(
    private val repo: SessionRepository,
) {
    suspend operator fun invoke() = repo.clearSession()
}
