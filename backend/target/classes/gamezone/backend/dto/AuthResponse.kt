package gamezone.backend.dto

data class AuthResponse(
    val user: UserResponse,
    val token: String
)
