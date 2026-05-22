package com.j4x.iris_ids.domain.usecase

import com.j4x.iris_ids.domain.model.Inspector
import com.j4x.iris_ids.domain.repository.AttendanceRepository
import javax.inject.Inject

class VerifyFaceUseCase @Inject constructor(
    private val repo: AttendanceRepository,
) {
    suspend operator fun invoke(imageBase64: String, deviceId: String): Result<Inspector> =
        repo.verifyFace(imageBase64, deviceId)
}
