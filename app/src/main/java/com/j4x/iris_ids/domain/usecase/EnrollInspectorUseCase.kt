package com.j4x.iris_ids.domain.usecase

import com.j4x.iris_ids.domain.model.Inspector
import com.j4x.iris_ids.domain.repository.InspectorRepository
import javax.inject.Inject

class EnrollInspectorUseCase @Inject constructor(
    private val repo: InspectorRepository,
) {
    suspend operator fun invoke(
        name: String,
        documentId: String,
        role: String,
        imagesBase64: List<String>,
    ): Result<Inspector> = repo.enrollInspector(name, documentId, role, imagesBase64)
}
