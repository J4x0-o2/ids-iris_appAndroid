package com.j4x.iris_ids.domain.usecase

import com.j4x.iris_ids.domain.model.EventType
import com.j4x.iris_ids.domain.repository.AttendanceRepository
import java.time.Instant
import javax.inject.Inject

class RegisterEventUseCase @Inject constructor(
    private val repo: AttendanceRepository,
) {
    suspend operator fun invoke(inspectorId: String, eventType: EventType, deviceId: String): Result<Unit> =
        repo.registerEvent(inspectorId, eventType, deviceId, Instant.now().toString())
}
