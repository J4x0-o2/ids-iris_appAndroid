package com.j4x.iris_ids.data.repository

import com.j4x.iris_ids.data.local.db.dao.PendingEventDao
import com.j4x.iris_ids.data.local.db.entity.PendingEventEntity
import com.j4x.iris_ids.data.remote.api.IrisApi
import com.j4x.iris_ids.data.remote.dto.EventRequest
import com.j4x.iris_ids.data.remote.dto.SyncEventDto
import com.j4x.iris_ids.data.remote.dto.SyncRequest
import com.j4x.iris_ids.data.remote.dto.VerifyRequest
import com.j4x.iris_ids.domain.model.AttendanceEvent
import com.j4x.iris_ids.domain.model.EventType
import com.j4x.iris_ids.domain.model.Inspector
import com.j4x.iris_ids.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val api: IrisApi,
    private val dao: PendingEventDao,
) : AttendanceRepository {

    override suspend fun verifyFace(imageBase64: String, deviceId: String): Result<Inspector> =
        runCatching {
            val body = api.verifyFace(VerifyRequest(imageBase64 = imageBase64, deviceId = deviceId))
                .body() ?: error("Empty response")
            if (!body.matched || body.inspector == null) error("Face not recognized")
            val i = body.inspector
            Inspector(id = i.id, name = i.name, documentId = i.documentId, role = i.role)
        }

    override suspend fun registerEvent(
        inspectorId: String,
        eventType: EventType,
        deviceId: String,
        timestamp: String,
    ): Result<Unit> = runCatching {
        // Save to Room first — HomeViewModel and ChooseViewModel read from here
        val localId = dao.insert(
            PendingEventEntity(
                inspectorId = inspectorId,
                eventType   = eventType.id,
                deviceId    = deviceId,
                timestamp   = timestamp,
            )
        )
        // Try to send to API immediately; mark as synced if it succeeds
        runCatching {
            val response = api.registerEvent(
                EventRequest(
                    inspectorId = inspectorId,
                    eventType   = eventType.id,
                    deviceId    = deviceId,
                    timestamp   = timestamp,
                )
            )
            if (response.isSuccessful) dao.markSynced(listOf(localId))
        }
        // Always succeed — the event is saved locally even if the network call fails
    }

    override suspend fun savePendingEvent(event: AttendanceEvent) {
        dao.insert(
            PendingEventEntity(
                inspectorId = event.inspectorId,
                eventType   = event.eventType.id,
                deviceId    = event.deviceId,
                timestamp   = event.timestamp,
            )
        )
    }

    override fun getPendingEvents(): Flow<List<AttendanceEvent>> =
        dao.getAll().map { list ->
            list.map { e ->
                AttendanceEvent(
                    id          = e.id,
                    inspectorId = e.inspectorId,
                    eventType   = EventType.from(e.eventType),
                    deviceId    = e.deviceId,
                    timestamp   = e.timestamp,
                )
            }
        }

    override suspend fun syncPendingEvents() {
        // Only send events that haven't been synced yet
        val entities = dao.getUnsynced()
        if (entities.isEmpty()) return
        val response = api.syncEvents(
            SyncRequest(
                events = entities.map { e ->
                    SyncEventDto(
                        localId     = e.id,
                        inspectorId = e.inspectorId,
                        eventType   = e.eventType,
                        deviceId    = e.deviceId,
                        timestamp   = e.timestamp,
                    )
                }
            )
        )
        if (response.isSuccessful) {
            val syncedIds = response.body()?.synced ?: emptyList()
            if (syncedIds.isNotEmpty()) dao.markSynced(syncedIds)
        }
    }

    override suspend fun getHistory(inspectorId: String, limit: Int): Result<List<AttendanceEvent>> =
        runCatching {
            val dtos = api.getHistory(inspectorId, limit).body() ?: error("Empty response")
            dtos.map { dto ->
                AttendanceEvent(
                    id          = dto.id,
                    inspectorId = dto.inspectorId,
                    eventType   = EventType.from(dto.eventType),
                    deviceId    = dto.deviceId,
                    timestamp   = dto.clientTimestamp,
                )
            }
        }
}
