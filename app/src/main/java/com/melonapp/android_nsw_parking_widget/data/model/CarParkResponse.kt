package com.melonapp.android_nsw_parking_widget.data.model

import com.google.gson.annotations.SerializedName

data class CarParkResponse(
    @SerializedName("facility_id") val facilityId: String,
    @SerializedName("facility_name") val facilityName: String?,
    @SerializedName("spots") val totalSpots: String?,
    @SerializedName("occupancy") val occupancy: Occupancy?,
    @SerializedName("ts") val timestamp: String?,
    @SerializedName("zones") val zones: List<Zone>?
)

data class Occupancy(
    @SerializedName("loop") val loop: String?,
    @SerializedName("total") val total: String?
)

data class Zone(
    @SerializedName("zone_id") val zoneId: String?,
    @SerializedName("zone_name") val zoneName: String?,
    @SerializedName("spots") val spots: String?,
    @SerializedName("occupancy") val occupancy: Occupancy?
)
