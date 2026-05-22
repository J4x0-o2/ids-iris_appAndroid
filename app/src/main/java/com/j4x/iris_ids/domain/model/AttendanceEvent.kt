package com.j4x.iris_ids.domain.model

data class AttendanceEvent(
    val id: Long = 0,
    val inspectorId: String,
    val eventType: EventType,
    val deviceId: String,
    val timestamp: String,
)
