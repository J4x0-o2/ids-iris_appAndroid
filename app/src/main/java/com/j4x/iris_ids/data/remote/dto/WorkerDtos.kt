package com.j4x.iris_ids.data.remote.dto

data class EnrollRequest(
    val name: String,
    val documentId: String,
    val role: String,
    val imageBase64: String,
)

data class EnrollResponse(
    val inspectorId: String,
    val enrolled: Boolean,
)

data class WorkerResponse(
    val id: String,
    val name: String,
    val documentId: String,
    val role: String,
)
