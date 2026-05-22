package com.j4x.iris_ids.domain.repository

import com.j4x.iris_ids.domain.model.Inspector

interface InspectorRepository {
    suspend fun getInspectors(): Result<List<Inspector>>
    suspend fun enrollInspector(name: String, documentId: String, role: String, imagesBase64: List<String>): Result<Inspector>
}
