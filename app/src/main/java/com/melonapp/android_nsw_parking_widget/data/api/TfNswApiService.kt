package com.melonapp.android_nsw_parking_widget.data.api

import com.melonapp.android_nsw_parking_widget.data.model.CarParkResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TfNswApiService {
    @GET("v1/carpark")
    suspend fun getAllCarParks(
        @Header("Authorization") auth: String
    ): Response<Map<String, String>>

    @GET("v1/carpark")
    suspend fun getCarParkById(
        @Header("Authorization") auth: String,
        @Query("facility") facilityId: String
    ): Response<CarParkResponse>
}
