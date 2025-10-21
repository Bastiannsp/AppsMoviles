package cl.duoc.appmovil2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.appmovil2.data.local.entity.User
import cl.duoc.appmovil2.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_NAME_LENGTH = 100
private const val MAX_EMAIL_LENGTH = 60
private val NAME_REGEX = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]+$")
private val PASSWORD_REGEX = Regex(
    pattern = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\\$%^&*()_+\\-=\\[\\]{};':"\\\\|,.<>/?]).{8,}$""")

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val selectedGenders: Set<String> = emptySet(),
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val genderError: String? = null,
    val generalError: String? = null,
    val isEmailChecking: Boolean = false,
    val isSubmitting: Boolean = false,
    val showSuccess: Boolean = false
) {
    val availableGenders: List<String> = listOf(
        "Femenino",
        "Masculino",
        "No binario",
        "Prefiero no decirlo",
        "Otro"
    )

    val isNameValid: Boolean get() = nameError == null && fullName.isNotBlank()
    val isEmailValid: Boolean get() = emailError == null && email.isNotBlank()
    val isPasswordValid: Boolean get() = passwordError == null && password.isNotBlank()
    val isConfirmPasswordValid: Boolean get() = confirmPasswordError == null && confirmPassword.isNotBlank()
    val isGenderValid: Boolean get() = genderError == null && selectedGenders.isNotEmpty()
}

class RegisterViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private var emailValidationJob: Job? = null

    fun onFullNameChange(newValue: String) {
        _uiState.update { current ->
            current.copy(
                fullName = newValue,
                nameError = validateName(newValue),
                generalError = null
            )
        }
    }

    fun onEmailChange(newValue: String) {
        _uiState.update { current ->
            current.copy(
                email = newValue,
                generalError = null
            )
        }

        emailValidationJob?.cancel()
        val immediateError = validateEmail(newValue)
        val trimmed = newValue.trim()
        _uiState.update { current ->
            current.copy(
                emailError = immediateError,
                isEmailChecking = immediateError == null && trimmed.isNotEmpty()
            )
        }

        if (immediateError == null && trimmed.isNotEmpty()) {
            emailValidationJob = viewModelScope.launch {
                delay(350)
                val exists = userRepository.isEmailRegistered(trimmed)
                _uiState.update { current ->
                    current.copy(
                        emailError = if (exists) "El correo ya está registrado" else null,
                        isEmailChecking = false
                    )
                }
            }
        } else {
            _uiState.update { it.copy(isEmailChecking = false) }
        }
    }

    fun onPasswordChange(newValue: String) {
        _uiState.update { current ->
            val passwordError = validatePassword(newValue)
            val confirmError = validateConfirmPassword(newValue, current.confirmPassword)
            current.copy(
                password = newValue,
                passwordError = passwordError,
                confirmPasswordError = confirmError,
                generalError = null
            )
        }
    }

    fun onConfirmPasswordChange(newValue: String) {
        _uiState.update { current ->
            current.copy(
                confirmPassword = newValue,
                confirmPasswordError = validateConfirmPassword(current.password, newValue),
                generalError = null
            )
        }
    }

    fun onPhoneChange(newValue: String) {
        if (newValue.length > 20) return
        val filtered = newValue.filter { it.isDigit() || it == '+' || it == ' ' }
        _uiState.update { current ->
            current.copy(
                phone = filtered,
                generalError = null
            )
        }
    }

    fun onGenderToggled(gender: String, isSelected: Boolean) {
        _uiState.update { current ->
            val updated = current.selectedGenders.toMutableSet()
            if (isSelected) {
                updated.add(gender)
            } else {
                updated.remove(gender)
            }
            current.copy(
                selectedGenders = updated,
                genderError = if (updated.isEmpty()) "Selecciona al menos un género" else null,
                generalError = null
            )
        }
    }

    fun onSubmit() {
        val current = _uiState.value
        val nameError = validateName(current.fullName)
        val emailError = validateEmail(current.email)
        val passwordError = validatePassword(current.password)
        val confirmPasswordError = validateConfirmPassword(current.password, current.confirmPassword)
        val genderError = if (current.selectedGenders.isEmpty()) "Selecciona al menos un género" else null

        if (nameError != null || emailError != null || passwordError != null ||
            confirmPasswordError != null || genderError != null
        ) {
            _uiState.update { state ->
                state.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    genderError = genderError,
                    generalError = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, generalError = null) }

            val sanitizedEmail = current.email.trim().lowercase()
            val emailExists = userRepository.isEmailRegistered(sanitizedEmail)
            if (emailExists) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        emailError = "El correo ya está registrado"
                    )
                }
                return@launch
            }

            try {
                val user = User(
                    fullName = current.fullName.trim(),
                    email = sanitizedEmail,
                    password = current.password,
                    phone = current.phone.ifBlank { null },
                    genders = current.selectedGenders.toList()
                )
                userRepository.registerUser(user)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        showSuccess = true,
                        generalError = null
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        generalError = "No se pudo completar el registro. Intenta nuevamente."
                    )
                }
            }
        }
    }

    fun onSuccessConsumed() {
        _uiState.update {
            it.copy(showSuccess = false)
        }
    }

    private fun validateName(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) {
            return "El nombre es obligatorio"
        }
        if (trimmed.length > MAX_NAME_LENGTH) {
            return "Máximo $MAX_NAME_LENGTH caracteres"
        }
        if (!NAME_REGEX.matches(trimmed)) {
            return "Solo se permiten letras y espacios"
        }
        return null
    }

    private fun validateEmail(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) {
            return "El correo es obligatorio"
        }
        if (trimmed.length > MAX_EMAIL_LENGTH) {
            return "Máximo $MAX_EMAIL_LENGTH caracteres"
        }
        if (!trimmed.lowercase().endsWith("@duoc.cl")) {
            return "Debe ser un correo @duoc.cl"
        }
        return null
    }

    private fun validatePassword(value: String): String? {
        if (value.isBlank()) {
            return "La contraseña es obligatoria"
        }
        if (!PASSWORD_REGEX.containsMatchIn(value)) {
            return "Debe tener 8 caracteres, mayúsculas, minúsculas, número y símbolo"
        }
        return null
    }

    private fun validateConfirmPassword(password: String, confirmation: String): String? {
        if (confirmation.isBlank()) {
            return "Confirma tu contraseña"
        }
        if (password != confirmation) {
            return "Las contraseñas no coinciden"
        }
        return null
    }
}

class RegisterViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
