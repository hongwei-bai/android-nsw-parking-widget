package com.melondemo.parkingwidget.data

data class CarParkResponse(
    val carparks: List<CarPark>
)

data class CarPark(
    val id: String,
    val name: String,
    val location: String,
    val occupancy: Occupancy
)

data class Occupancy(
    val total: Int,
    val available: Int
)
