package com.example.gamezone.repository

import com.example.gamezone.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun registerUser(user: User): Result<Unit>
    suspend fun authenticate(email: String, password: String): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun isEmailRegistered(email: String): Boolean
    suspend fun getUserByEmail(email: String): User?
    fun observeActiveUser(): Flow<User?>
    suspend fun setActiveUser(email: String)
    suspend fun clearActiveUser()
}
