package com.j4x.iris_ids.data.remote.dto

// ── Verify ────────────────────────────────────────────────────────────────────

data class VerifyRequest(
    val imageBase64: String,
    val deviceId: String,
)

data class VerifyInspector(
    val id: String,
    val name: String,
    val documentId: String,
    val role: String,
)

data class VerifyResponse(
    val matched: Boolean,
    val inspector: VerifyInspector? = null,
    val similarity: Float = 0f,
)

// ── Event ─────────────────────────────────────────────────────────────────────

data class EventRequest(
    val inspectorId: String,
    val eventType: String,
    val deviceId: String,
    val timestamp: String,
)

data class EventResponse(
    val eventId: String,
    val recorded: Boolean,
)

// ── Sync ──────────────────────────────────────────────────────────────────────

data class SyncEventDto(
    val localId: Long,
    val inspectorId: String,
    val eventType: String,
    val deviceId: String,
    val timestamp: String,
)

data class SyncRequest(
    val events: List<SyncEventDto>,
)

data class SyncResponse(
    val synced: List<Long> = emptyList(),
    val failed: List<Long> = emptyList(),
)

// ── History ───────────────────────────────────────────────────────────────────

data class HistoryEventDto(
    val id: Long,
    val inspectorId: String,
    val eventType: String,
    val deviceId: String,
    val clientTimestamp: String,
    val serverTimestamp: String,
)
