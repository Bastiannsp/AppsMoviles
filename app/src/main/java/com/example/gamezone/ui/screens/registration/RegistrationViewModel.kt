package com.example.gamezone.ui.screens.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamezone.model.User
import com.example.gamezone.repository.UserRepository
import com.example.gamezone.util.validateConfirmPassword
import com.example.gamezone.util.validateFullName
import com.example.gamezone.util.validateGenres
import com.example.gamezone.util.validatePassword
import com.example.gamezone.util.validatePhone
import com.example.gamezone.util.validateRegistrationEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistrationViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        val sanitized = value.take(100)
        _uiState.update { current ->
            current.copy(
                fullName = sanitized,
                errors = current.errors.copy(fullName = validateFullName(sanitized))
            )
        }
    }

    fun onEmailChange(value: String) {
        val sanitized = value.take(60)
        _uiState.update { current ->
            current.copy(
                email = sanitized,
                errors = current.errors.copy(email = validateRegistrationEmail(sanitized)),
                emailAvailabilityMessage = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { current ->
            val passwordError = validatePassword(value)
            val confirmError = validateConfirmPassword(value, current.confirmPassword)
            current.copy(
                password = value,
                errors = current.errors.copy(
                    password = passwordError,
                    confirmPassword = confirmError
                )
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { current ->
            current.copy(
                confirmPassword = value,
                errors = current.errors.copy(
                    confirmPassword = validateConfirmPassword(current.password, value)
                )
            )
        }
    }

    fun onPhoneChange(value: String) {
        val sanitized = value.take(20)
        _uiState.update { current ->
            current.copy(
                phone = sanitized,
                errors = current.errors.copy(phone = validatePhone(sanitized))
            )
        }
    }

    fun onGenreToggle(genre: String, isSelected: Boolean) {
        _uiState.update { current ->
            val updated = if (isSelected) current.selectedGenres + genre else current.selectedGenres - genre
            current.copy(
                selectedGenres = updated,
                errors = current.errors.copy(genres = validateGenres(updated))
            )
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun clearLastRegisteredEmail() {
        _uiState.update { it.copy(lastRegisteredEmail = null) }
    }

    fun resetMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun submitRegistration() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, message = null) }

            val current = _uiState.value
            val validationErrors = validateCurrentInput(current)

            if (validationErrors.hasAny()) {
                _uiState.update { state ->
                    state.copy(isSubmitting = false, errors = validationErrors)
                }
                return@launch
            }

            val isEmailTaken = runCatching { userRepository.isEmailRegistered(current.email) }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            message = error.message ?: "No se pudo verificar el correo"
                        )
                    }
                }
                .getOrElse { return@launch }
            if (isEmailTaken) {
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        errors = state.errors.copy(email = "El correo ya está registrado"),
                        emailAvailabilityMessage = "El correo ya está registrado"
                    )
                }
                return@launch
            }

            val user = User(
                fullName = current.fullName.trim(),
                email = current.email.trim(),
                password = current.password,
                phone = current.phone.trim().ifBlank { null },
                favoriteGenres = current.selectedGenres.toList()
            )

            val result = userRepository.registerUser(user)
            if (result.isSuccess) {
                _uiState.update {
                    RegistrationUiState(
                        message = "Registro exitoso",
                        isSuccess = true,
                        lastRegisteredEmail = user.email.trim()
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        message = result.exceptionOrNull()?.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    private fun validateCurrentInput(state: RegistrationUiState): RegistrationErrors {
        val fullNameError = validateFullName(state.fullName)
        val emailError = validateRegistrationEmail(state.email)
        val passwordError = validatePassword(state.password)
        val confirmPasswordError = validateConfirmPassword(state.password, state.confirmPassword)
        val phoneError = validatePhone(state.phone)
        val genresError = validateGenres(state.selectedGenres)

        return RegistrationErrors(
            fullName = fullNameError,
            email = emailError,
            password = passwordError,
            confirmPassword = confirmPasswordError,
            phone = phoneError,
            genres = genresError
        )
    }
}

data class RegistrationUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val selectedGenres: Set<String> = emptySet(),
    val errors: RegistrationErrors = RegistrationErrors(),
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String? = null,
    val emailAvailabilityMessage: String? = null,
    val lastRegisteredEmail: String? = null
)

data class RegistrationErrors(
    val fullName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
    val phone: String? = null,
    val genres: String? = null
) {
    fun hasAny(): Boolean =
        listOf(fullName, email, password, confirmPassword, phone, genres).any { it != null }
}
