package com.j4x.iris_ids.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.j4x.iris_ids.domain.model.Session
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "iris_session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_ID   = stringPreferencesKey("inspector_id")
        private val KEY_NAME = stringPreferencesKey("inspector_name")
        private val KEY_DOC  = stringPreferencesKey("document_id")
        private val KEY_ROLE = stringPreferencesKey("role")
    }

    val session: Flow<Session?> = context.sessionDataStore.data.map { prefs ->
        val id = prefs[KEY_ID] ?: return@map null
        Session(
            inspectorId   = id,
            inspectorName = prefs[KEY_NAME] ?: "",
            documentId    = prefs[KEY_DOC]  ?: "",
            role          = prefs[KEY_ROLE] ?: "",
        )
    }

    suspend fun save(session: Session) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_ID]   = session.inspectorId
            prefs[KEY_NAME] = session.inspectorName
            prefs[KEY_DOC]  = session.documentId
            prefs[KEY_ROLE] = session.role
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }
}
