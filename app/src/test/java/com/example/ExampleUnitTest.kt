package com.example

import com.example.domain.DecayPricingEngine
import com.example.domain.GeofenceEngine
import com.example.domain.QrGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDynamicDecayPricing_initialTime_returnsStartPrice() {
        val startPrice = 200.0
        val minPrice = 80.0
        val startTime = 1000000L
        val closingTime = 1000000L + (100 * 60 * 1000L) // 100 minutes

        val price = DecayPricingEngine.calculateCurrentPrice(
            startPrice = startPrice,
            minPrice = minPrice,
            startTimeMillis = startTime,
            closingTimeMillis = closingTime,
            currentTimeMillis = startTime
        )
        assertEquals(200.0, price, 0.01)
    }

    @Test
    fun testDynamicDecayPricing_halfway_returnsMidPrice() {
        val startPrice = 200.0
        val minPrice = 80.0
        val startTime = 1000000L
        val totalDuration = 100 * 60 * 1000L
        val closingTime = startTime + totalDuration
        val halfTime = startTime + (totalDuration / 2) // 50% elapsed -> 200 - 0.5*(120) = 140

        val price = DecayPricingEngine.calculateCurrentPrice(
            startPrice = startPrice,
            minPrice = minPrice,
            startTimeMillis = startTime,
            closingTimeMillis = closingTime,
            currentTimeMillis = halfTime
        )
        assertEquals(140.0, price, 0.01)
    }

    @Test
    fun testDynamicDecayPricing_afterClosing_returnsMinFloorPrice() {
        val startPrice = 200.0
        val minPrice = 80.0
        val startTime = 1000000L
        val closingTime = startTime + (60 * 60 * 1000L)
        val afterClose = closingTime + 5000L

        val price = DecayPricingEngine.calculateCurrentPrice(
            startPrice = startPrice,
            minPrice = minPrice,
            startTimeMillis = startTime,
            closingTimeMillis = closingTime,
            currentTimeMillis = afterClose
        )
        assertEquals(80.0, price, 0.01)
    }

    @Test
    fun testSpatialGeofence_withinAndOutside2km() {
        // Campus Quad
        val studentLat = 12.9716
        val studentLon = 77.5946

        // Nearby Bakery (~200 meters)
        val bakeryLat = 12.9725
        val bakeryLon = 77.5935
        val distNearby = GeofenceEngine.calculateDistanceKm(studentLat, studentLon, bakeryLat, bakeryLon)
        assertTrue(distNearby < 1.0)
        assertTrue(GeofenceEngine.isWithinRadius(studentLat, studentLon, bakeryLat, bakeryLon, 2.0))

        // Highway Diner (~4.8 km)
        val dinerLat = 12.9980
        val dinerLon = 77.6300
        val distFar = GeofenceEngine.calculateDistanceKm(studentLat, studentLon, dinerLat, dinerLon)
        assertTrue(distFar > 3.0)
        assertFalse(GeofenceEngine.isWithinRadius(studentLat, studentLon, dinerLat, dinerLon, 2.0))
    }

    @Test
    fun testQrMatrixAndPayloadGeneration() {
        val payload = QrGenerator.generateOrderPickupPayload("SBX-8492", 1L, 1L)
        assertTrue(payload.contains("SBX-8492"))

        val matrix = QrGenerator.generateQrMatrix(payload)
        assertEquals(21, matrix.size)
        assertEquals(21, matrix[0].size)

        // Parse order code
        val parsedCode = QrGenerator.parseOrderCodeFromPayload(payload)
        assertEquals("SBX-8492", parsedCode)
    }
}
