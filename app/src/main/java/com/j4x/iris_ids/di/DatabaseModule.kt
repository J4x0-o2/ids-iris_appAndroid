package com.j4x.iris_ids.di

import android.content.Context
import androidx.room.Room
import com.j4x.iris_ids.data.local.db.IrisDatabase
import com.j4x.iris_ids.data.local.db.dao.PendingEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): IrisDatabase =
        Room.databaseBuilder(context, IrisDatabase::class.java, "iris.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePendingEventDao(db: IrisDatabase): PendingEventDao = db.pendingEventDao()
}
