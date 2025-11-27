package com.example.gamezone.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamezone.repository.UserRepository
import com.example.gamezone.util.EMAIL_DUOC_REGEX
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        val sanitized = value.take(60)
        _uiState.update { current ->
            current.copy(
                email = sanitized,
                emailError = validateLoginEmail(sanitized),
                credentialsError = null,
                message = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { current ->
            current.copy(
                password = value,
                passwordError = validateLoginPassword(value),
                credentialsError = null,
                message = null
            )
        }
    }

    fun setRegisteredEmail(email: String) {
        val sanitized = email.take(60)
        _uiState.update { current ->
            current.copy(
                email = sanitized,
                emailError = null
            )
        }
    }

    fun showRegistrationSuccessMessage() {
        _uiState.update { it.copy(message = "Registro exitoso. Inicia sesión.") }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun submitLogin() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    credentialsError = null,
                    message = null
                )
            }

            val current = _uiState.value
            val emailError = validateLoginEmail(current.email)
            val passwordError = validateLoginPassword(current.password)

            if (emailError != null || passwordError != null) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        emailError = emailError,
                        passwordError = passwordError
                    )
                }
                return@launch
            }

            val authResult = userRepository.authenticate(current.email.trim(), current.password)
            if (authResult.isSuccess) {
                val authenticated = authResult.getOrThrow()
                userRepository.setActiveUser(authenticated.email)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isSuccess = true,
                        password = "",
                        credentialsError = null
                    )
                }
            } else {
                val errorMessage = authResult.exceptionOrNull()?.message ?: "Error desconocido"
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        credentialsError = errorMessage
                    )
                }
            }
        }
    }

    private fun validateLoginEmail(email: String): String? {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return "El correo es obligatorio"
        if (!trimmed.matches(EMAIL_DUOC_REGEX)) return "Debe ser un correo @duoc.cl válido"
        return null
    }

    private fun validateLoginPassword(password: String): String? {
        if (password.isBlank()) return "La contraseña es obligatoria"
        return null
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val credentialsError: String? = null,
    val message: String? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)
