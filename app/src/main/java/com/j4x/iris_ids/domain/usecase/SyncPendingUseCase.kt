package com.j4x.iris_ids.domain.usecase

import com.j4x.iris_ids.domain.repository.AttendanceRepository
import javax.inject.Inject

class SyncPendingUseCase @Inject constructor(
    private val repo: AttendanceRepository,
) {
    suspend operator fun invoke() = repo.syncPendingEvents()
}
