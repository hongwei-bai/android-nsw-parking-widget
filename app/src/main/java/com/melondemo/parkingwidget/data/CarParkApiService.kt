package com.melondemo.parkingwidget.data

import retrofit2.Response
import retrofit2.http.GET

interface CarParkApiService {
    @GET("v1/carpark/occupancy")
    suspend fun getCarParkOccupancy(): Response<CarParkResponse>
}