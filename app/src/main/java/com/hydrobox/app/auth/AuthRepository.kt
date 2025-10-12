package com.hydrobox.app.auth

import com.hydrobox.app.auth.data.*
import kotlinx.coroutines.flow.*

class AuthRepository(
    private val dao: AuthDao,
    private val store: AuthStore
) {
    val authState: Flow<AuthState> = store.state.distinctUntilChanged()
    val currentUser: Flow<UserEntity?> =
        authState.flatMapLatest { st -> st.userId?.let { dao.observeById(it) } ?: flowOf(null) }
            .distinctUntilChanged()

    val lastEmail: Flow<String?> = store.lastEmail
    val rememberPref: Flow<Boolean> = store.rememberPref

    suspend fun login(email: String, pass: String, remember: Boolean): Boolean {
        val user = dao.findByEmail(email) ?: return false
        val ok = user.passwordPlain == pass
        if (ok) store.setLoggedIn(user.id, email, remember) else store.clear()
        return ok
    }

    suspend fun logout() { store.clear() }
    suspend fun onAppLaunch() { store.onAppLaunch() }
}
