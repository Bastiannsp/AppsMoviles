package gamezone.backend.dto

data class UserResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String?,
    val favoriteGenres: List<String>,
    val avatarUrl: String?
)
