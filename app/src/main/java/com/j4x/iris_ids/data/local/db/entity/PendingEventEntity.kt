package com.j4x.iris_ids.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_events")
data class PendingEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inspectorId: String,
    val eventType: String,
    val deviceId: String,
    val timestamp: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
)
