package com.example.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class CampusLocation(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

object GeofenceEngine {

    val CAMPUS_PRESETS = listOf(
        CampusLocation(
            id = "quad",
            name = "Campus Central Quad",
            description = "Main Academic Lawn & Student Union",
            latitude = 12.9716,
            longitude = 77.5946
        ),
        CampusLocation(
            id = "north_gate",
            name = "North Gate Promenade",
            description = "Adjacent to bakeries & shopping street",
            latitude = 12.9750,
            longitude = 77.5930
        ),
        CampusLocation(
            id = "eng_block",
            name = "Engineering Block B",
            description = "Labs & Tech Auditorium",
            latitude = 12.9740,
            longitude = 77.5915
        ),
        CampusLocation(
            id = "hostels",
            name = "South Hostels & Dorms",
            description = "Residential hostel complex",
            latitude = 12.9650,
            longitude = 77.5965
        ),
        CampusLocation(
            id = "sports",
            name = "University Stadium",
            description = "West boundary athletic tracks",
            latitude = 12.9680,
            longitude = 77.5890
        )
    )

    /**
     * Haversine formula for calculating spherical distance between two GPS coordinates in kilometers.
     * Corresponds to PostGIS ST_DWithin computation in PRD Module B.
     */
    fun calculateDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadiusKm * c
        return (distance * 100.0).toInt() / 100.0 // 2 decimal precision
    }

    fun isWithinRadius(
        studentLat: Double,
        studentLon: Double,
        vendorLat: Double,
        vendorLon: Double,
        radiusKm: Double = 2.0
    ): Boolean {
        return calculateDistanceKm(studentLat, studentLon, vendorLat, vendorLon) <= radiusKm
    }

    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            "${(distanceKm * 1000).toInt()}m away"
        } else {
            String.format("%.1f km away", distanceKm)
        }
    }

    fun estimateWalkingTimeMinutes(distanceKm: Double): Int {
        // Average walking speed ~ 4.8 km/h -> 1 km ≈ 12.5 minutes
        val minutes = (distanceKm * 12.5).toInt()
        return minutes.coerceAtLeast(1)
    }
}
