package cl.duoc.appmovil2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.appmovil2.data.session.SessionPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberSession: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false
)

sealed class LoginEvent {
    data class NavigateToHome(val rememberSession: Boolean) : LoginEvent()
    data object NavigateToRegister : LoginEvent()
}

class LoginViewModel(
    private val sessionPreferencesRepository: SessionPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            sessionPreferencesRepository.rememberSessionFlow.collect { remember ->
                _uiState.update { current ->
                    current.copy(rememberSession = remember)
                }
            }
        }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { current ->
            current.copy(email = newEmail, emailError = null)
        }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { current ->
            current.copy(password = newPassword, passwordError = null)
        }
    }

    fun onRememberSessionChange(remember: Boolean) {
        _uiState.update { current ->
            current.copy(rememberSession = remember)
        }
    }

    fun onLoginClick() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)

        if (emailError != null || passwordError != null) {
            _uiState.update { current ->
                current.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, emailError = null, passwordError = null) }

        viewModelScope.launch {
            sessionPreferencesRepository.setRememberSession(_uiState.value.rememberSession)
            _events.emit(LoginEvent.NavigateToHome(_uiState.value.rememberSession))
            _uiState.update { current ->
                current.copy(isLoading = false)
            }
        }
    }

    fun onRegisterClick() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToRegister)
        }
    }

    private fun validateEmail(email: String): String? {
        if (email.isEmpty()) {
            return "El correo es obligatorio"
        }
        if (!email.lowercase().endsWith("@duoc.cl")) {
            return "Debes usar un correo @duoc.cl"
        }
        return null
    }

    private fun validatePassword(password: String): String? {
        if (password.isBlank()) {
            return "La contraseña es obligatoria"
        }
        return null
    }
}

class LoginViewModelFactory(
    private val sessionPreferencesRepository: SessionPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(sessionPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
