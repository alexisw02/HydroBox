package com.hydrobox.app.auth

import com.hydrobox.app.api.ApiUser
import com.hydrobox.app.api.HydroApi
import com.hydrobox.app.auth.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

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
        if (e.isBlank() || p.isBlank()) return false

        // 1) Login contra la API de Laravel (Hostinger)
        val remote: ApiUser? = try {
            HydroApi.login(e, p)
        } catch (_: Exception) {
            null
        }

        if (remote == null) {
            store.clear()
            return false
        }

        // 2) Sincronizar usuario remoto en Room
        val existing = dao.findByEmail(remote.email)

        val entity = if (existing != null) {
            existing.copy(
                name          = remote.name,
                lastName      = remote.lastName ?: existing.lastName,
                email         = remote.email,
                passwordPlain = p,   // solo local
                avatarUri     = remote.avatarUrl ?: existing.avatarUri,
                phonePrefix   = remote.phonePrefix ?: existing.phonePrefix,
                phone         = remote.phone ?: existing.phone
            )
        } else {
            UserEntity(
                id            = 0,
                name          = remote.name,
                lastName      = remote.lastName ?: "",
                email         = remote.email,
                passwordPlain = p,
                avatarUri     = remote.avatarUrl,
                phonePrefix   = remote.phonePrefix,
                phone         = remote.phone
            )
        }

        // 3) Guardar/actualizar en Room y marcar sesión iniciada
        val localId = dao.upsert(entity)
        store.setLoggedIn(localId, remote.email, remember)
        return true
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
