package com.hydrobox.app.auth.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*

private val Context.dataStore by preferencesDataStore("auth_prefs")

class AuthStore(private val context: Context) {
    private val KEY_LOGGED    = booleanPreferencesKey("logged_in")
    private val KEY_USER_ID   = longPreferencesKey("user_id")
    private val KEY_REMEMBER  = booleanPreferencesKey("remember_me")
    private val KEY_LAST_EMAIL= stringPreferencesKey("last_email")

    val state: Flow<AuthState> = context.dataStore.data.map { p ->
        AuthState(
            isLoggedIn = p[KEY_LOGGED] ?: false,
            userId     = p[KEY_USER_ID]
        )
    }
    val lastEmail: Flow<String?> = context.dataStore.data.map { it[KEY_LAST_EMAIL] }
    val rememberPref: Flow<Boolean> = context.dataStore.data.map { it[KEY_REMEMBER] ?: false }

    suspend fun setLoggedIn(userId: Long, email: String, remember: Boolean) {
        context.dataStore.edit {
            it[KEY_LOGGED]   = true
            it[KEY_USER_ID]  = userId
            it[KEY_REMEMBER] = remember
            if (remember) it[KEY_LAST_EMAIL] = email else it.remove(KEY_LAST_EMAIL)
        }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it[KEY_LOGGED] = false
            it.remove(KEY_USER_ID)
        }
    }

    suspend fun onAppLaunch() {
        context.dataStore.edit {
            it[KEY_LOGGED] = false
            it.remove(KEY_USER_ID)
        }
    }
}
