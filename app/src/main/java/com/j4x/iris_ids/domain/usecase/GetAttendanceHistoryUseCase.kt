package com.j4x.iris_ids.domain.usecase

import com.j4x.iris_ids.domain.model.AttendanceEvent
import com.j4x.iris_ids.domain.repository.AttendanceRepository
import javax.inject.Inject

class GetAttendanceHistoryUseCase @Inject constructor(
    private val repo: AttendanceRepository,
) {
    suspend operator fun invoke(inspectorId: String, limit: Int = 100): Result<List<AttendanceEvent>> =
        repo.getHistory(inspectorId, limit)
}
