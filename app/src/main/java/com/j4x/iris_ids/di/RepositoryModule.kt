package com.j4x.iris_ids.di

import com.j4x.iris_ids.data.repository.AttendanceRepositoryImpl
import com.j4x.iris_ids.data.repository.AuthRepositoryImpl
import com.j4x.iris_ids.data.repository.InspectorRepositoryImpl
import com.j4x.iris_ids.data.repository.SessionRepositoryImpl
import com.j4x.iris_ids.domain.repository.AttendanceRepository
import com.j4x.iris_ids.domain.repository.AuthRepository
import com.j4x.iris_ids.domain.repository.InspectorRepository
import com.j4x.iris_ids.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository

    @Binds @Singleton
    abstract fun bindInspectorRepository(impl: InspectorRepositoryImpl): InspectorRepository

    @Binds @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
