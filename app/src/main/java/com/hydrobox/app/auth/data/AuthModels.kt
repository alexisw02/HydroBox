package com.hydrobox.app.auth.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users_local")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lastName: String,
    val email: String,
    val passwordPlain: String
)

data class AuthState(
    val isLoggedIn: Boolean = false,
    val userId: Long? = null
)
