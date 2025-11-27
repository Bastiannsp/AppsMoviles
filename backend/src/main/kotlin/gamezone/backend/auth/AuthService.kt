package gamezone.backend.auth

import gamezone.backend.dto.AuthResponse
import gamezone.backend.dto.LoginRequest
import gamezone.backend.user.UserMapper
import gamezone.backend.user.UserRepository
import java.util.Locale
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val normalizedEmail = request.email.trim().lowercase(Locale.ROOT)
        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        val userResponse = UserMapper.toUserResponse(user)
        return AuthResponse(user = userResponse, token = "dummy-token")
    }
}
