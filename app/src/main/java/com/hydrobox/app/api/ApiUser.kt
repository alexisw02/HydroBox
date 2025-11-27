package com.hydrobox.app.api

data class ApiUser(
    val id: Long,
    val name: String,
    val lastName: String?,
    val email: String,
    val phonePrefix: String?,
    val phone: String?,
    val avatarUrl: String?
)
