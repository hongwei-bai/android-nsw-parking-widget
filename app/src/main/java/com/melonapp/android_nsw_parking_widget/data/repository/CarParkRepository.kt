package com.melonapp.android_nsw_parking_widget.data.repository

import com.melonapp.android_nsw_parking_widget.data.api.TfNswApiService
import com.melonapp.android_nsw_parking_widget.data.model.CarParkResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class CarParkRepository(private val apiService: TfNswApiService) {

    /**
     * Fetches all available car park facilities.
     * @param apiKey The API key for TfNSW.
     * @return A map of facility ID to facility name.
     */
    suspend fun getAllFacilities(apiKey: String): Map<String, String> {
        val auth = "apikey $apiKey"
        val response = apiService.getAllCarParks(auth)
        return if (response.isSuccessful) {
            response.body() ?: emptyMap()
        } else {
            emptyMap()
        }
    }

    /**
     * Fetches detailed information for a specific car park facility.
     * @param apiKey The API key for TfNSW.
     * @param facilityId The ID of the facility.
     */
    suspend fun getCarParkDetails(apiKey: String, facilityId: String): CarParkResponse? {
        val auth = "apikey $apiKey"
        val response = apiService.getCarParkById(auth, facilityId)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}
