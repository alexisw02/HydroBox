package com.hydrobox.app.auth.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth_prefs")

class AuthStore(private val context: Context) {
    private val KEY_LOGGED = booleanPreferencesKey("logged_in")
    private val KEY_USER_ID = longPreferencesKey("user_id")

    val state: Flow<AuthState> = context.dataStore.data.map { p ->
        AuthState(p[KEY_LOGGED] ?: false, p[KEY_USER_ID])
    }

    suspend fun setLoggedIn(userId: Long) {
        context.dataStore.edit {
            it[KEY_LOGGED] = true
            it[KEY_USER_ID] = userId
        }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it[KEY_LOGGED] = false
            it.remove(KEY_USER_ID)
        }
    }
}
