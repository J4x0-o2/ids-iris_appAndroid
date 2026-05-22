package com.j4x.iris_ids.domain.repository

import com.j4x.iris_ids.domain.model.AttendanceEvent
import com.j4x.iris_ids.domain.model.EventType
import com.j4x.iris_ids.domain.model.Inspector
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    suspend fun verifyFace(imageBase64: String, deviceId: String): Result<Inspector>
    suspend fun registerEvent(inspectorId: String, eventType: EventType, deviceId: String, timestamp: String): Result<Unit>
    suspend fun savePendingEvent(event: AttendanceEvent)
    fun getPendingEvents(): Flow<List<AttendanceEvent>>
    suspend fun syncPendingEvents()
    suspend fun getHistory(inspectorId: String, limit: Int = 100): Result<List<AttendanceEvent>>
}
