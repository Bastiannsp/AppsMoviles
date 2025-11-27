package com.example.gamezone.ui.screens.profile

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamezone.data.storage.ProfilePhotoStorage
import com.example.gamezone.model.User
import com.example.gamezone.repository.UserRepository
import com.example.gamezone.util.validateConfirmPassword
import com.example.gamezone.util.validateFullName
import com.example.gamezone.util.validateGenres
import com.example.gamezone.util.validatePassword
import com.example.gamezone.util.validatePhone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val photoStorage: ProfilePhotoStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private var baseUser: User? = null

    fun setUser(user: User?) {
        if (user == null) return
        if (baseUser?.email == user.email && _uiState.value.isLoaded) return
        baseUser = user
        _uiState.value = ProfileUiState(
            email = user.email,
            fullName = user.fullName,
            phone = user.phone.orEmpty(),
            selectedGenres = user.favoriteGenres.toSet(),
            avatarPath = user.avatarPath,
            isLoaded = true
        )
    }

    fun onFullNameChange(value: String) {
        val sanitized = value.take(100)
        _uiState.update { current ->
            current.copy(
                fullName = sanitized,
                errors = current.errors.copy(fullName = validateFullName(sanitized))
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

    fun onNewPasswordChange(value: String) {
        _uiState.update { current ->
            val passwordError = if (value.isBlank()) null else validatePassword(value)
            val confirmError = if (current.confirmPassword.isBlank()) null
            else validateConfirmPassword(value, current.confirmPassword)
            current.copy(
                newPassword = value,
                errors = current.errors.copy(
                    newPassword = passwordError,
                    confirmPassword = confirmError
                )
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { current ->
            val confirmError = if (current.newPassword.isBlank() && value.isBlank()) null
            else validateConfirmPassword(current.newPassword, value)
            current.copy(
                confirmPassword = value,
                errors = current.errors.copy(confirmPassword = confirmError)
            )
        }
    }

    fun resetMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun saveProfilePhoto(bitmap: Bitmap) {
        val user = baseUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPhoto = true, message = null) }
            val storageResult = runCatching { photoStorage.saveProfilePhoto(bitmap, user.email) }
            val path = storageResult.getOrNull()
            if (path == null) {
                _uiState.update {
                    it.copy(
                        isSavingPhoto = false,
                        message = storageResult.exceptionOrNull()?.message ?: "No se pudo guardar la foto"
                    )
                }
                return@launch
            }

            val updatedUser = user.copy(avatarPath = path)
            val result = userRepository.updateUser(updatedUser)
            if (result.isSuccess) {
                baseUser = updatedUser
                _uiState.update {
                    it.copy(
                        avatarPath = path,
                        isSavingPhoto = false,
                        message = "Foto de perfil actualizada"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSavingPhoto = false,
                        message = result.exceptionOrNull()?.message ?: "No se pudo actualizar la foto"
                    )
                }
            }
        }
    }

    fun submitUpdates() {
        val user = baseUser ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }

            val current = _uiState.value

            val fullNameError = validateFullName(current.fullName)
            val phoneError = validatePhone(current.phone)
            val genresError = validateGenres(current.selectedGenres)
            val newPasswordError = if (current.newPassword.isBlank()) null else validatePassword(current.newPassword)
            val confirmPasswordError = if (current.newPassword.isBlank() && current.confirmPassword.isBlank()) {
                null
            } else {
                validateConfirmPassword(current.newPassword, current.confirmPassword)
            }

            val errors = ProfileErrors(
                fullName = fullNameError,
                phone = phoneError,
                genres = genresError,
                newPassword = newPasswordError,
                confirmPassword = confirmPasswordError
            )

            if (errors.hasAny()) {
                _uiState.update {
                    it.copy(isSaving = false, errors = errors)
                }
                return@launch
            }

            val updatedUser = user.copy(
                fullName = current.fullName.trim(),
                phone = current.phone.trim().ifBlank { null },
                password = if (current.newPassword.isBlank()) user.password else current.newPassword,
                favoriteGenres = current.selectedGenres.toList(),
                avatarPath = current.avatarPath
            )

            val result = userRepository.updateUser(updatedUser)
            if (result.isSuccess) {
                baseUser = updatedUser
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = "Datos actualizados correctamente",
                        newPassword = "",
                        confirmPassword = "",
                        errors = it.errors.copy(newPassword = null, confirmPassword = null),
                        isSuccess = true,
                        avatarPath = updatedUser.avatarPath
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = result.exceptionOrNull()?.message ?: "No se pudo actualizar la información"
                    )
                }
            }
        }
    }
}

data class ProfileUiState(
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val selectedGenres: Set<String> = emptySet(),
    val newPassword: String = "",
    val confirmPassword: String = "",
    val errors: ProfileErrors = ProfileErrors(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val isSuccess: Boolean = false,
    val isLoaded: Boolean = false,
    val avatarPath: String? = null,
    val isSavingPhoto: Boolean = false
)

data class ProfileErrors(
    val fullName: String? = null,
    val phone: String? = null,
    val genres: String? = null,
    val newPassword: String? = null,
    val confirmPassword: String? = null
) {
    fun hasAny(): Boolean =
        listOf(fullName, phone, genres, newPassword, confirmPassword).any { it != null }
}
