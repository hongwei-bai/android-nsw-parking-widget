package com.melondemo.parkingwidget.data

import android.util.Log
import com.melondemo.parkingwidget.BuildConfig

object CarParkRepository {
    suspend fun fetchCarParkData() {
        val response = carParkApiService.getCarParkOccupancy()
        if (response.isSuccessful) {
            val data = response.body()

            Log.d("bbbb", data.toString())
        } else {
            Log.e("bbbb", response.toString())
            Log.w("bbbb", "apiKey: ${BuildConfig.PARKING_API_KEY}")
        }
    }
}
