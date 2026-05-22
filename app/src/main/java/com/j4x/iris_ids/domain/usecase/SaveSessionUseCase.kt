package com.j4x.iris_ids.domain.usecase

import com.j4x.iris_ids.domain.model.Session
import com.j4x.iris_ids.domain.repository.SessionRepository
import javax.inject.Inject

class SaveSessionUseCase @Inject constructor(
    private val repo: SessionRepository,
) {
    suspend operator fun invoke(session: Session) = repo.saveSession(session)
}
