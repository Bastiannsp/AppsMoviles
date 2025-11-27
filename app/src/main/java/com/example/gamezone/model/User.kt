package com.example.gamezone.model

data class User(
    val id: Long = 0,
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String?,
    val favoriteGenres: List<String>,
    val avatarPath: String? = null
)
