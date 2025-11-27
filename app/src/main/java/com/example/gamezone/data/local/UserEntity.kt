package com.example.gamezone.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val remoteId: Long,
    val fullName: String,
    val password: String,
    val phone: String?,
    val favoriteGenres: List<String>,
    val avatarPath: String? = null
)
