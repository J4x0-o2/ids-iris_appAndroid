package com.j4x.iris_ids.data.repository

import com.j4x.iris_ids.data.remote.api.IrisApi
import com.j4x.iris_ids.data.remote.dto.EnrollRequest
import com.j4x.iris_ids.domain.model.Inspector
import com.j4x.iris_ids.domain.repository.InspectorRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InspectorRepositoryImpl @Inject constructor(
    private val api: IrisApi,
) : InspectorRepository {

    override suspend fun getInspectors(): Result<List<Inspector>> = runCatching {
        api.getWorkers().body()?.map { dto ->
            Inspector(id = dto.id, name = dto.name, documentId = dto.documentId, role = dto.role)
        } ?: emptyList()
    }

    override suspend fun enrollInspector(
        name: String,
        documentId: String,
        role: String,
        imagesBase64: List<String>,
    ): Result<Inspector> = runCatching {
        val body = api.enrollWorker(
            EnrollRequest(
                name         = name,
                documentId   = documentId,
                role         = role,
                imagesBase64 = imagesBase64,
            )
        ).body() ?: error("Empty response")
        Inspector(id = body.inspectorId, name = name, documentId = documentId, role = role)
    }
}
