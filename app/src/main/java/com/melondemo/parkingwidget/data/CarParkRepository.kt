package com.melondemo.parkingwidget.data

import android.util.Log
import com.melondemo.parkingwidget.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CarParkRepository {
    private val _state = MutableStateFlow("")
    val state: StateFlow<String> = _state.asStateFlow()

    suspend fun fetchCarParkData() {
        val response = carParkApiService.getCarParkOccupancy()
        if (response.isSuccessful) {
            val data = response.body()
            _state.value = data.toString()
            Log.d("bbbb", data.toString())
        } else {
            Log.e("bbbb", response.toString())
            _state.value = "Error: $response"
            Log.w("bbbb", "apiKey: ${BuildConfig.PARKING_API_KEY}")
        }
    }
}
