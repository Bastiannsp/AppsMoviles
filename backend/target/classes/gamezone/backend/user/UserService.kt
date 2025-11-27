package gamezone.backend.user

import gamezone.backend.dto.RegisterRequest
import gamezone.backend.dto.UpdateUserRequest
import gamezone.backend.dto.UserResponse
import java.util.Locale
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun register(request: RegisterRequest): UserResponse {
        val normalizedEmail = request.email.trim().lowercase(Locale.ROOT)
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw UserAlreadyExistsException(normalizedEmail)
        }

        val passwordHash = passwordEncoder.encode(request.password)
        val entity = UserMapper.fromRegisterRequest(request, passwordHash)
        val stored = userRepository.save(entity)
        return UserMapper.toUserResponse(stored)
    }

    @Transactional(readOnly = true)
    fun getByEmail(email: String): UserResponse {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw UserNotFoundException(normalizedEmail)
        return UserMapper.toUserResponse(user)
    }

    @Transactional
    fun update(email: String, request: UpdateUserRequest): UserResponse {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw UserNotFoundException(normalizedEmail)

        val newPasswordHash = request.password
            ?.takeIf { it.isNotBlank() }
            ?.let { passwordEncoder.encode(it) }

        val updated = UserMapper.applyUpdate(user, request, newPasswordHash)
        val stored = userRepository.save(updated)
        return UserMapper.toUserResponse(stored)
    }
}
