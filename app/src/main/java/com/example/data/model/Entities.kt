package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DietaryTag(val label: String, val icon: String) {
    VEG("Pure Veg", "🌱"),
    NON_VEG("Non-Veg", "🍗"),
    EGG("Contains Egg", "🥚"),
    VEGAN("Vegan", "🌿")
}

enum class OrderStatus {
    RESERVED,
    PAID,
    COMPLETED,
    EXPIRED
}

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val fssaiLicense: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double,
    val totalRatings: Int,
    val isSuspended: Boolean = false,
    val suspensionReason: String? = null,
    val avatarEmoji: String = "🏪",
    val openingHours: String = "10:00 AM - 11:30 PM",
    val bannerGradientIndex: Int = 0
)

@Entity(tableName = "mystery_boxes")
data class MysteryBoxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorId: Long,
    val title: String,
    val description: String,
    val dietaryTag: String, // VEG, NON_VEG, EGG, VEGAN
    val originalValue: Double,
    val startPrice: Double,
    val minPrice: Double,
    val totalQuantity: Int,
    val availableQuantity: Int,
    val lockedQuantity: Int = 0,
    val startTimeMillis: Long,
    val closingTimeMillis: Long,
    val pickupWindow: String,
    val surpriseItemsHint: String,
    val isActive: Boolean = true
)

@Entity(tableName = "inventory_locks")
data class InventoryLockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boxId: Long,
    val studentId: String = "STUDENT_DEMO",
    val lockedAtMillis: Long,
    val expiresAtMillis: Long, // 5 minutes TTL
    val isReleased: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderCode: String, // e.g. "SBX-8492"
    val boxId: Long,
    val vendorId: Long,
    val vendorName: String,
    val boxTitle: String,
    val paidAmount: Double,
    val originalValue: Double,
    val status: String, // OrderStatus name
    val reservedAtMillis: Long,
    val paidAtMillis: Long = 0L,
    val completedAtMillis: Long = 0L,
    val qrSecretPayload: String,
    val studentRating: Int = 0, // 0 = unrated, 1..5
    val ratingTags: String = "",
    val ratingComment: String = ""
)

@Entity(tableName = "notifications")
data class PushNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val vendorId: Long? = null,
    val boxId: Long? = null,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
