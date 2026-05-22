package com.j4x.iris_ids.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.j4x.iris_ids.data.local.db.dao.PendingEventDao
import com.j4x.iris_ids.data.local.db.entity.PendingEventEntity

@Database(
    entities = [PendingEventEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class IrisDatabase : RoomDatabase() {
    abstract fun pendingEventDao(): PendingEventDao
}
