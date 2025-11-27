package com.example.gamezone.network.dto

import com.example.gamezone.model.User

/**
 * Data Transfer Objects (DTO) que reflejan la forma en que el backend expone los datos.
 * Mantener estos modelos separados de las entidades de dominio permite desacoplar
 * la capa de red y facilita la evolución de la API sin impactar directamente la UI.
 */
data class RegisterRequestDto(
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String?,
    val favoriteGenres: List<String>,
    val avatarUrl: String? = null
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class UpdateUserRequestDto(
    val fullName: String,
    val phone: String?,
    val favoriteGenres: List<String>,
    val password: String?,
    val avatarUrl: String?
)

data class UserResponseDto(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String?,
    val favoriteGenres: List<String>,
    val avatarUrl: String?
)

data class AuthResponseDto(
    val user: UserResponseDto,
    val token: String
)

/**
 * Conversores simples entre las capas de red y dominio. Usa el password conocido
 * (si existe) porque el backend nunca debe devolverlo en la respuesta.
 */
fun UserResponseDto.toDomain(existingPassword: String = ""): User =
    User(
        id = id,
        fullName = fullName,
        email = email,
        password = existingPassword,
        phone = phone,
        favoriteGenres = favoriteGenres,
        avatarPath = avatarUrl
    )

fun User.toRegisterRequestDto(): RegisterRequestDto =
    RegisterRequestDto(
        fullName = fullName.trim(),
        email = email.trim(),
        password = password,
        phone = phone?.ifBlank { null },
        favoriteGenres = favoriteGenres,
        avatarUrl = avatarPath
    )

fun User.toUpdateUserRequestDto(newPassword: String? = null): UpdateUserRequestDto =
    UpdateUserRequestDto(
        fullName = fullName.trim(),
        phone = phone?.ifBlank { null },
        favoriteGenres = favoriteGenres,
        password = newPassword?.ifBlank { null },
        avatarUrl = avatarPath
    )
