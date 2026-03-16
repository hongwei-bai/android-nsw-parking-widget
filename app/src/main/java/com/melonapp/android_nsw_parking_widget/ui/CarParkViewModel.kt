package com.melonapp.android_nsw_parking_widget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melonapp.android_nsw_parking_widget.data.model.CarParkResponse
import com.melonapp.android_nsw_parking_widget.data.repository.CarParkRepository
import com.melonapp.android_nsw_parking_widget.util.CarParkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SelectedCarPark(
    val id: String,
    val name: String,
    val abbr: String,
    val availableSpots: Int = 0
)

data class CarParkUiState(
    val isLoading: Boolean = false,
    val facilities: Map<String, String> = emptyMap(),
    val selectedFacilityDetails: CarParkResponse? = null,
    val selectedCarParks: List<SelectedCarPark> = emptyList(),
    val errorMessage: String? = null,
    val hasOverlayPermission: Boolean = false
)

class CarParkViewModel(private val repository: CarParkRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CarParkUiState())
    val uiState: StateFlow<CarParkUiState> = _uiState.asStateFlow()

    // In a real app, this should be stored securely
    private var apiKey: String = "YOUR_API_KEY_HERE" // Default or provided by user

    fun setApiKey(key: String) {
        apiKey = key
    }

    fun setOverlayPermission(hasPermission: Boolean) {
        _uiState.update { it.copy(hasOverlayPermission = hasPermission) }
    }

    fun fetchFacilities() {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
            _uiState.update { it.copy(errorMessage = "API Key is missing") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val facilities = repository.getAllFacilities(apiKey)
                if (facilities.isNotEmpty()) {
                    _uiState.update { it.copy(facilities = facilities, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to fetch facilities") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun toggleCarParkSelection(id: String, name: String) {
        _uiState.update { state ->
            val currentSelected = state.selectedCarParks.toMutableList()
            val existing = currentSelected.find { it.id == id }
            
            if (existing != null) {
                currentSelected.remove(existing)
            } else {
                if (currentSelected.size < 3) {
                    currentSelected.add(
                        SelectedCarPark(
                            id = id,
                            name = name,
                            abbr = CarParkUtils.getAbbreviation(name)
                        )
                    )
                }
            }
            state.copy(selectedCarParks = currentSelected)
        }
        
        // Refresh details for selected car parks
        refreshSelectedCarParks()
    }

    private fun refreshSelectedCarParks() {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") return

        viewModelScope.launch {
            val currentSelected = _uiState.value.selectedCarParks
            val updatedList = currentSelected.map { selected ->
                val details = repository.getCarParkDetails(apiKey, selected.id)
                selected.copy(availableSpots = details?.availableSpots ?: 0)
            }
            _uiState.update { it.copy(selectedCarParks = updatedList) }
        }
    }
}
