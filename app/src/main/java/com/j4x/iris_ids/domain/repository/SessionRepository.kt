package com.j4x.iris_ids.domain.repository

import com.j4x.iris_ids.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getActiveSession(): Flow<Session?>
    suspend fun saveSession(session: Session)
    suspend fun clearSession()
}
