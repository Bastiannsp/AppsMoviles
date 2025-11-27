package com.example.gamezone.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamezone.model.User
import com.example.gamezone.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.observeActiveUser().collectLatest { user ->
                _uiState.update { it.copy(activeUser = user, isLoading = false) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.clearActiveUser()
        }
    }
}

data class SessionUiState(
    val activeUser: User? = null,
    val isLoading: Boolean = true
)
