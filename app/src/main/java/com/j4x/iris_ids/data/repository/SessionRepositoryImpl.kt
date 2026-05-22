package com.j4x.iris_ids.data.repository

import com.j4x.iris_ids.data.local.prefs.SessionDataStore
import com.j4x.iris_ids.domain.model.Session
import com.j4x.iris_ids.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val dataStore: SessionDataStore,
) : SessionRepository {
    override fun getActiveSession(): Flow<Session?> = dataStore.session
    override suspend fun saveSession(session: Session) = dataStore.save(session)
    override suspend fun clearSession() = dataStore.clear()
}
