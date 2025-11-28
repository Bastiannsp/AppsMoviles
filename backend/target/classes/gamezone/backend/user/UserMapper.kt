package gamezone.backend.user

import gamezone.backend.dto.RegisterRequest
import gamezone.backend.dto.UpdateUserRequest
import gamezone.backend.dto.UserResponse
import java.util.Locale

object UserMapper {
    fun fromRegisterRequest(request: RegisterRequest, passwordHash: String): UserEntity {
        val sanitizedGenres = request.favoriteGenres
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableSet()

        return UserEntity(
            fullName = request.fullName.trim(),
            email = request.email.trim().lowercase(Locale.ROOT),
            passwordHash = passwordHash,
            phone = request.phone?.trim()?.ifEmpty { null },
            favoriteGenres = sanitizedGenres,
            avatarUrl = null
        )
    }

    fun toUserResponse(entity: UserEntity): UserResponse {
        val genres = entity.favoriteGenres
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .sorted()

        return UserResponse(
            id = entity.id,
            fullName = entity.fullName,
            email = entity.email,
            phone = entity.phone,
            favoriteGenres = genres,
            avatarUrl = entity.avatarUrl
        )
    }

    fun applyUpdate(entity: UserEntity, request: UpdateUserRequest, newPasswordHash: String? = null): UserEntity {
        entity.fullName = request.fullName.trim()
        entity.phone = request.phone?.trim()?.ifEmpty { null }
        entity.favoriteGenres = request.favoriteGenres
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableSet()
        entity.avatarUrl = request.avatarUrl?.trim()?.ifEmpty { null }
        newPasswordHash?.let { entity.passwordHash = it }
        return entity
    }
}
