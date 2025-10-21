package cl.duoc.appmovil2.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_preferences")

class SessionPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val REMEMBER_SESSION = booleanPreferencesKey("remember_session")
    }

    val rememberSessionFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.REMEMBER_SESSION] ?: false
    }

    suspend fun setRememberSession(rememberSession: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.REMEMBER_SESSION] = rememberSession
        }
    }
}
