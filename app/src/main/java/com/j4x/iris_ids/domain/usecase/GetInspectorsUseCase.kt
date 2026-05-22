package com.j4x.iris_ids.domain.usecase

import com.j4x.iris_ids.domain.model.Inspector
import com.j4x.iris_ids.domain.repository.InspectorRepository
import javax.inject.Inject

class GetInspectorsUseCase @Inject constructor(
    private val repo: InspectorRepository,
) {
    suspend operator fun invoke(): Result<List<Inspector>> = repo.getInspectors()
}
