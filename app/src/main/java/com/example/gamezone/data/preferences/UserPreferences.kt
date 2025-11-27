package com.example.gamezone.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val ACTIVE_USER_EMAIL = stringPreferencesKey("active_user_email")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    val activeUserEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ACTIVE_USER_EMAIL]
    }

    val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    suspend fun setSession(email: String, token: String) {
        dataStore.edit { preferences ->
            preferences[ACTIVE_USER_EMAIL] = email
            preferences[AUTH_TOKEN] = token
        }
    }

    suspend fun setActiveUser(email: String) {
        dataStore.edit { preferences ->
            preferences[ACTIVE_USER_EMAIL] = email
        }
    }

    suspend fun clearActiveUser() {
        dataStore.edit { preferences ->
            preferences.remove(ACTIVE_USER_EMAIL)
            preferences.remove(AUTH_TOKEN)
        }
    }
}
