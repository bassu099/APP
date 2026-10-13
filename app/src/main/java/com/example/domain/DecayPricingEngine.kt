package com.example.domain

import kotlin.math.max
import kotlin.math.roundToInt

object DecayPricingEngine {

    /**
     * Module C: Dynamic Decay Pricing Algorithm
     * Formula: current_price = max(min_price, start_price - (elapsed/total) * (start_price - min_price))
     */
    fun calculateCurrentPrice(
        startPrice: Double,
        minPrice: Double,
        startTimeMillis: Long,
        closingTimeMillis: Long,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Double {
        val totalDuration = (closingTimeMillis - startTimeMillis).coerceAtLeast(1L)
        val elapsed = (currentTimeMillis - startTimeMillis).coerceAtLeast(0L)

        if (elapsed >= totalDuration) {
            return minPrice
        }

        val elapsedFraction = (elapsed.toDouble() / totalDuration.toDouble()).coerceIn(0.0, 1.0)
        val decayedPrice = startPrice - (elapsedFraction * (startPrice - minPrice))
        val currentPrice = max(minPrice, decayedPrice)
        return (currentPrice * 100.0).roundToInt() / 100.0 // 2 decimal precision
    }

    fun calculateElapsedFraction(
        startTimeMillis: Long,
        closingTimeMillis: Long,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Float {
        val total = (closingTimeMillis - startTimeMillis).coerceAtLeast(1L).toFloat()
        val elapsed = (currentTimeMillis - startTimeMillis).coerceAtLeast(0L).toFloat()
        return (elapsed / total).coerceIn(0f, 1f)
    }

    fun calculateDiscountPercent(originalValue: Double, currentPrice: Double): Int {
        if (originalValue <= 0.0) return 0
        val discount = ((originalValue - currentPrice) / originalValue) * 100.0
        return discount.roundToInt().coerceIn(0, 99)
    }

    fun formatRemainingTime(closingTimeMillis: Long, currentTimeMillis: Long = System.currentTimeMillis()): String {
        val remainingMillis = closingTimeMillis - currentTimeMillis
        if (remainingMillis <= 0) return "Closed"
        val totalSeconds = remainingMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%dh %02dm", hours, minutes)
        } else {
            String.format("%02dm %02ds", minutes, seconds)
        }
    }
}
