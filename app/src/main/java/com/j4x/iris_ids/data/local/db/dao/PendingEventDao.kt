package com.j4x.iris_ids.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.j4x.iris_ids.data.local.db.entity.PendingEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingEventDao {
    @Insert
    suspend fun insert(event: PendingEventEntity): Long

    @Query("SELECT * FROM pending_events ORDER BY createdAt ASC")
    fun getAll(): Flow<List<PendingEventEntity>>

    @Query("SELECT * FROM pending_events WHERE inspectorId = :inspectorId ORDER BY createdAt ASC")
    fun getByInspector(inspectorId: String): Flow<List<PendingEventEntity>>

    @Query("SELECT * FROM pending_events ORDER BY createdAt ASC")
    suspend fun getAllOnce(): List<PendingEventEntity>

    @Query("SELECT * FROM pending_events WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsynced(): List<PendingEventEntity>

    @Query("UPDATE pending_events SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM pending_events WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
