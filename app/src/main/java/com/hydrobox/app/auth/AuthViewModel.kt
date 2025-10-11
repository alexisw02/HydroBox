package com.hydrobox.app.auth

import android.app.*
import androidx.lifecycle.*
import com.hydrobox.app.auth.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app, viewModelScope)
    private val repo = AuthRepository(db.authDao(), AuthStore(app))

    val authState = repo.authState
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = AuthState())
    val currentUser = repo.currentUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    fun login(email: String, pass: String, onResult: (Boolean)->Unit) {
        viewModelScope.launch { onResult(repo.login(email, pass)) }
    }
    fun logout() { viewModelScope.launch { repo.logout() } }
}