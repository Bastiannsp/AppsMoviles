package com.example.gamezone.repository

import com.example.gamezone.data.local.UserDao
import com.example.gamezone.data.local.UserEntity
import com.example.gamezone.data.preferences.UserPreferences
import com.example.gamezone.model.User
import com.example.gamezone.network.ApiService
import com.example.gamezone.network.dto.LoginRequestDto
import com.example.gamezone.network.dto.toDomain
import com.example.gamezone.network.dto.toRegisterRequestDto
import com.example.gamezone.network.dto.toUpdateUserRequestDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences,
    private val apiService: ApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {

    override suspend fun registerUser(user: User): Result<Unit> = withContext(ioDispatcher) {
        val sanitizedUser = user.sanitized()

        return@withContext try {
            val response = apiService.register(sanitizedUser.toRegisterRequestDto())
            if (!response.isSuccessful) {
                val message = when (response.code()) {
                    400 -> "Los datos enviados no son válidos"
                    409 -> "El correo ya está registrado"
                    else -> "No se pudo completar el registro"
                }
                return@withContext Result.failure(IllegalStateException(message))
            }

            val body = response.body()
                ?: return@withContext Result.failure(IllegalStateException("Respuesta de registro vacía"))

            val registeredUser = body.toDomain(existingPassword = sanitizedUser.password)
            cacheUser(registeredUser)
            Result.success(Unit)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            Result.failure(exception.toDomainError("No se pudo contactar al servidor"))
        }
    }

    override suspend fun authenticate(email: String, password: String): Result<User> = withContext(ioDispatcher) {
        val sanitizedEmail = email.trim()

        return@withContext try {
            val response = apiService.login(LoginRequestDto(sanitizedEmail, password))
            if (!response.isSuccessful) {
                val message = when (response.code()) {
                    400 -> "Los datos enviados no son válidos"
                    401 -> "Correo o contraseña incorrectos"
                    404 -> "El usuario no existe"
                    else -> "No se pudo iniciar sesión"
                }
                return@withContext Result.failure(IllegalStateException(message))
            }

            val body = response.body()
                ?: return@withContext Result.failure(IllegalStateException("Respuesta de autenticación vacía"))

            val authenticatedUser = body.user.toDomain(existingPassword = password)
            cacheUser(authenticatedUser)
            userPreferences.setSession(authenticatedUser.email, body.token)
            Result.success(authenticatedUser)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            Result.failure(exception.toDomainError("No se pudo contactar al servidor"))
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> = withContext(ioDispatcher) {
        val sanitizedUser = user.sanitized()
        val existingEntity = userDao.getByEmail(sanitizedUser.email)
            ?: return@withContext Result.failure(IllegalArgumentException("El usuario no existe"))

        val newPassword = if (existingEntity.password != sanitizedUser.password) sanitizedUser.password else null

        return@withContext try {
            val response = apiService.updateUser(
                sanitizedUser.email,
                sanitizedUser.toUpdateUserRequestDto(newPassword)
            )

            if (!response.isSuccessful) {
                val message = when (response.code()) {
                    400 -> "Los datos enviados no son válidos"
                    404 -> "El usuario no existe"
                    else -> "No se pudo actualizar la información"
                }
                return@withContext Result.failure(IllegalStateException(message))
            }

            val body = response.body()
                ?: return@withContext Result.failure(IllegalStateException("Respuesta de actualización vacía"))

            val storedPassword = newPassword ?: existingEntity.password
            val updatedUser = body.toDomain(existingPassword = storedPassword)
            cacheUser(updatedUser.copy(password = storedPassword))
            Result.success(Unit)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            Result.failure(exception.toDomainError("No se pudo contactar al servidor"))
        }
    }

    override suspend fun isEmailRegistered(email: String): Boolean = withContext(ioDispatcher) {
        val sanitizedEmail = email.trim()

        return@withContext try {
            val response = apiService.getUser(sanitizedEmail)
            when {
                response.isSuccessful -> true
                response.code() == 404 -> false
                else -> throw IllegalStateException(mapGenericHttpError(response.code()))
            }
        } catch (exception: HttpException) {
            if (exception.code() == 404) {
                false
            } else {
                throw IllegalStateException(mapGenericHttpError(exception.code()))
            }
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            throw exception.toDomainError("No se pudo verificar el correo")
        }
    }

    override suspend fun getUserByEmail(email: String): User? = withContext(ioDispatcher) {
        val sanitizedEmail = email.trim()
        val localEntity = userDao.getByEmail(sanitizedEmail)

        return@withContext try {
            val response = apiService.getUser(sanitizedEmail)
            when {
                response.isSuccessful -> {
                    val body = response.body() ?: return@withContext localEntity?.toDomain()
                    val password = localEntity?.password ?: ""
                    val remoteUser = body.toDomain(existingPassword = password)
                    cacheUser(remoteUser.copy(password = if (password.isBlank()) remoteUser.password else password))
                    remoteUser
                }

                response.code() == 404 -> localEntity?.toDomain()
                else -> throw IllegalStateException(mapGenericHttpError(response.code()))
            }
        } catch (exception: HttpException) {
            if (exception.code() == 404) {
                localEntity?.toDomain()
            } else {
                throw IllegalStateException(mapGenericHttpError(exception.code()))
            }
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            throw exception.toDomainError("No se pudo obtener los datos del usuario")
        }
    }

    override fun observeActiveUser(): Flow<User?> =
        userPreferences.activeUserEmail.flatMapLatest { email ->
            if (email.isNullOrBlank()) {
                flowOf(null)
            } else {
                userDao.observeByEmail(email).map { it?.toDomain() }
            }
        }

    override suspend fun setActiveUser(email: String) {
        userPreferences.setActiveUser(email.trim())
    }

    override suspend fun clearActiveUser() {
        userPreferences.clearActiveUser()
    }

    private fun User.toEntity(): UserEntity =
        UserEntity(
            email = email.trim(),
            remoteId = id,
            fullName = fullName.trim(),
            password = password,
            phone = phone?.trim()?.ifBlank { null },
            favoriteGenres = favoriteGenres,
            avatarPath = avatarPath?.trim()?.ifBlank { null }
        )

    private fun UserEntity.toDomain(): User =
        User(
            id = remoteId,
            fullName = fullName,
            email = email,
            password = password,
            phone = phone,
            favoriteGenres = favoriteGenres,
            avatarPath = avatarPath
        )

    private suspend fun cacheUser(user: User) {
        userDao.upsert(user.sanitized().toEntity())
    }

    private fun User.sanitized(): User =
        copy(
            fullName = fullName.trim(),
            email = email.trim(),
            phone = phone?.trim()?.ifBlank { null },
            favoriteGenres = favoriteGenres.map { it.trim() }.filter { it.isNotBlank() },
            avatarPath = avatarPath?.trim()?.ifBlank { null }
        )

    private fun Throwable.toDomainError(fallback: String): Throwable = when (this) {
        is CancellationException -> this
        is HttpException -> IllegalStateException(mapGenericHttpError(code()))
        is IOException -> IllegalStateException(fallback)
        is IllegalStateException -> this
        else -> IllegalStateException(message ?: fallback)
    }

    private fun mapGenericHttpError(code: Int): String = when (code) {
        400 -> "Los datos enviados no son válidos"
        401 -> "No autorizado para realizar la acción"
        403 -> "Acceso denegado"
        404 -> "Recurso no encontrado"
        409 -> "Ya existe un registro con esos datos"
        500 -> "Error interno del servidor"
        else -> "Error inesperado ($code)"
    }
}
