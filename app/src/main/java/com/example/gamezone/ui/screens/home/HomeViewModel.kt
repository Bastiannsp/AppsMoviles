package com.example.gamezone.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamezone.data.location.DeviceLocation
import com.example.gamezone.data.location.LocationTracker
import com.example.gamezone.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun setUser(user: User?) {
        _uiState.update { it.copy(activeUser = user) }
    }

    fun requestLocation() {
        if (_uiState.value.isFetchingLocation) return
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingLocation = true, locationError = null) }
            val result = locationTracker.getCurrentLocation()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isFetchingLocation = false,
                        deviceLocation = result.getOrNull(),
                        locationError = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isFetchingLocation = false,
                        locationError = result.exceptionOrNull()?.message ?: "No se pudo obtener la ubicación"
                    )
                }
            }
        }
    }

    fun clearLocationMessage() {
        _uiState.update { it.copy(locationError = null) }
    }
}

data class HomeUiState(
    val activeUser: User? = null,
    val deviceLocation: DeviceLocation? = null,
    val isFetchingLocation: Boolean = false,
    val locationError: String? = null
)
