package com.j4x.iris_ids.domain.usecase

import com.j4x.iris_ids.domain.model.Session
import com.j4x.iris_ids.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveSessionUseCase @Inject constructor(
    private val repo: SessionRepository,
) {
    operator fun invoke(): Flow<Session?> = repo.getActiveSession()
}
