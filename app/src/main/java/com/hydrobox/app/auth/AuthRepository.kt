package com.hydrobox.app.auth

import com.hydrobox.app.auth.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi


class AuthRepository(
    private val dao: AuthDao,
    private val store: AuthStore
) {
    val authState: Flow<AuthState> = store.state.distinctUntilChanged()
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<UserEntity?> =
        authState.flatMapLatest { st -> st.userId?.let { dao.observeById(it) } ?: flowOf(null) }
            .distinctUntilChanged()

    val lastEmail: Flow<String?> = store.lastEmail
    val rememberPref: Flow<Boolean> = store.rememberPref

    suspend fun login(email: String, pass: String, remember: Boolean): Boolean {
        val e = email.trim()
        val p = pass.trim()
        val user = dao.findByEmail(e) ?: return false
        val ok = user.passwordPlain == p
        if (ok) store.setLoggedIn(user.id, e, remember) else store.clear()
        return ok
    }

    suspend fun logout() { store.clear() }
    suspend fun onAppLaunch() { store.onAppLaunch() }

    suspend fun updateProfile(
        userId: Long,
        name: String,
        lastName: String,
        email: String,
        newPasswordPlain: String?,
        avatarUri: String?,
        phonePrefix: String?,
        phone: String?
    ) {
        val current = dao.findById(userId) ?: return
        val updated = current.copy(
            name = name,
            lastName = lastName,
            email = email,
            passwordPlain = newPasswordPlain?.takeIf { it.isNotEmpty() } ?: current.passwordPlain,
            avatarUri = avatarUri,
            phonePrefix = phonePrefix,
            phone = phone
        )
        dao.update(updated)
    }
}
