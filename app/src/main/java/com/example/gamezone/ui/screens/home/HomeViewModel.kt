package com.example.gamezone.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamezone.data.location.DeviceLocation
import com.example.gamezone.data.location.LocationTracker
import com.example.gamezone.model.User
import com.example.gamezone.model.GameDeal
import com.example.gamezone.repository.GameDealsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val locationTracker: LocationTracker,
    private val gameDealsRepository: GameDealsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshDeals()
    }

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

    fun refreshDeals() {
        if (_uiState.value.isFetchingDeals) return
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingDeals = true, dealsError = null) }
            val result = gameDealsRepository.fetchTopDeals()
            _uiState.update { current ->
                result.fold(
                    onSuccess = { deals ->
                        current.copy(
                            isFetchingDeals = false,
                            gameDeals = deals,
                            dealsError = null
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isFetchingDeals = false,
                            dealsError = error.message ?: "No se pudieron cargar las ofertas"
                        )
                    }
                )
            }
        }
    }
}

data class HomeUiState(
    val activeUser: User? = null,
    val deviceLocation: DeviceLocation? = null,
    val isFetchingLocation: Boolean = false,
    val locationError: String? = null,
    val gameDeals: List<GameDeal> = emptyList(),
    val isFetchingDeals: Boolean = false,
    val dealsError: String? = null
)
