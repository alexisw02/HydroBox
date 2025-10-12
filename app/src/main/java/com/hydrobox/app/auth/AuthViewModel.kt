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

    // Para el LoginScreen:
    val lastEmail = repo.lastEmail
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
    val rememberPref = repo.rememberPref
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = false)

    private val _booting = MutableStateFlow(true)
    val booting: StateFlow<Boolean> = _booting

    init {
        viewModelScope.launch {
            repo.onAppLaunch()
            _booting.value = false
        }
    }

    fun login(email: String, pass: String, remember: Boolean, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { repo.login(email, pass, remember) }
            onResult(ok)
        }
    }

    fun logout() { viewModelScope.launch { repo.logout() } }
}