package com.melonapp.android_nsw_parking_widget.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the detailed response for a speci66fic car park facility.
 */
data class CarParkResponse(
    @SerializedName("facility_id") val facilityId: String,
    @SerializedName("facility_name") val facilityName: String?,
    @SerializedName("spots") val totalSpots: String?,
    @SerializedName("occupancy") val occupancy: Occupancy?,
    @SerializedName("tsn") val tsn: String?,
    @SerializedName("time") val time: String?,
    @SerializedName("MessageDate") val messageDate: String?,
    @SerializedName("location") val location: Location?,
    @SerializedName("zones") val zones: List<Zone>?
) {
    /**
     * Calculates the number of available spots.
     * Returns 0 if data is missing or invalid.
     */
    val availableSpots: Int
        get() {
            val total = totalSpots?.toIntOrNull() ?: 0
            val occupied = occupancy?.total?.toIntOrNull() ?: 0
            return (total - occupied).coerceAtLeast(0)
        }
}

data class Occupancy(
    @SerializedName("loop") val loop: String?,
    @SerializedName("total") val total: String?,
    @SerializedName("monthlies") val monthlies: String?,
    @SerializedName("open_gate") val open_gate: String?,
    @SerializedName("transients") val transients: String?
)

data class Location(
    @SerializedName("suburb") val suburb: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("latitude") val latitude: String?,
    @SerializedName("longitude") val longitude: String?
)

data class Zone(
    @SerializedName("zone_id") val zoneId: String?,
    @SerializedName("zone_name") val zoneName: String?,
    @SerializedName("spots") val spots: String?,
    @SerializedName("occupancy") val occupancy: Occupancy?,
    @SerializedName("parent_zone_id") val parentZoneId: String?
)
