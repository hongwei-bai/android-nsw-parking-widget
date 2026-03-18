package com.melonapp.android_nsw_parking_overlay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melonapp.android_nsw_parking_overlay.data.DataStoreManager
import com.melonapp.android_nsw_parking_overlay.data.model.CarParkResponse
import com.melonapp.android_nsw_parking_overlay.data.repository.CarParkRepository
import com.melonapp.android_nsw_parking_overlay.util.CarParkUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    val hasOverlayPermission: Boolean = false,
    val apiKey: String = ""
)

class CarParkViewModel(
    private val repository: CarParkRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarParkUiState())
    val uiState: StateFlow<CarParkUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    init {
        observeDataStore()
    }

    private fun observeDataStore() {
        viewModelScope.launch {
            dataStoreManager.apiKey.collectLatest { key ->
                _uiState.update { it.copy(apiKey = key ?: "") }
            }
        }
        viewModelScope.launch {
            dataStoreManager.selectedCarParks.collectLatest { json ->
                if (!json.isNullOrBlank()) {
                    val type = object : TypeToken<List<SelectedCarPark>>() {}.type
                    val list: List<SelectedCarPark> = gson.fromJson(json, type)
                    _uiState.update { it.copy(selectedCarParks = list) }
                }
            }
        }
    }

    fun setApiKey(key: String) {
        viewModelScope.launch {
            dataStoreManager.saveApiKey(key)
        }
    }

    fun setOverlayPermission(hasPermission: Boolean) {
        _uiState.update { it.copy(hasOverlayPermission = hasPermission) }
    }

    fun fetchFacilities() {
        val apiKey = _uiState.value.apiKey
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
        val currentSelected = _uiState.value.selectedCarParks.toMutableList()
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
        
        viewModelScope.launch {
            dataStoreManager.saveSelectedCarParks(gson.toJson(currentSelected))
        }
    }

    fun refreshSelectedCarParks() {
        val apiKey = _uiState.value.apiKey
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") return

        viewModelScope.launch {
            val currentSelected = _uiState.value.selectedCarParks
            val updatedList = currentSelected.map { selected ->
                val details = repository.getCarParkDetails(apiKey, selected.id)
                selected.copy(availableSpots = details?.availableSpots ?: 0)
            }
            _uiState.update { it.copy(selectedCarParks = updatedList) }
            // Note: We don't necessarily need to save back to DataStore here 
            // because spots change frequently, but we might want to if we want the widget 
            // to show the last fetched value. However, the widget fetches its own data.
        }
    }
}
