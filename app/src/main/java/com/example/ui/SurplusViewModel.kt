package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.DietaryTag
import com.example.data.model.InventoryLockEntity
import com.example.data.model.MysteryBoxEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PushNotificationEntity
import com.example.data.model.VendorEntity
import com.example.data.repository.SurplusRepository
import com.example.domain.CampusLocation
import com.example.domain.DecayPricingEngine
import com.example.domain.GeofenceEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppRole {
    STUDENT,
    VENDOR
}

data class MysteryBoxWithVendor(
    val box: MysteryBoxEntity,
    val vendor: VendorEntity,
    val distanceKm: Double,
    val isWithinGeofence: Boolean,
    val currentDecayPrice: Double,
    val discountPercent: Int,
    val timeRemainingText: String,
    val decayProgress: Float
)

sealed interface UiMessage {
    data class Success(val message: String) : UiMessage
    data class Error(val message: String) : UiMessage
    data class GeofenceAlert(val title: String, val message: String, val boxId: Long?) : UiMessage
}

class SurplusViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = SurplusRepository(database.appDao())

    // Role state
    private val _currentRole = MutableStateFlow(AppRole.STUDENT)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Student location & geofence filter
    private val _studentLocation = MutableStateFlow(GeofenceEngine.CAMPUS_PRESETS[0])
    val studentLocation: StateFlow<CampusLocation> = _studentLocation.asStateFlow()

    private val _geofenceRadiusKm = MutableStateFlow(2.0) // 2km default per PRD
    val geofenceRadiusKm: StateFlow<Double> = _geofenceRadiusKm.asStateFlow()

    private val _dietaryFilter = MutableStateFlow<String?>(null) // null = all
    val dietaryFilter: StateFlow<String?> = _dietaryFilter.asStateFlow()

    // Selected vendor for vendor mode
    private val _activeVendorId = MutableStateFlow<Long>(1L)
    val activeVendorId: StateFlow<Long> = _activeVendorId.asStateFlow()

    // Real-time ticking timestamp (updated every 1 second)
    private val _currentTimeTicker = MutableStateFlow(System.currentTimeMillis())
    val currentTimeTicker: StateFlow<Long> = _currentTimeTicker.asStateFlow()

    // Active pending reservation (order awaiting payment with 5 min lock)
    private val _activePendingReservation = MutableStateFlow<Pair<OrderEntity, InventoryLockEntity>?>(null)
    val activePendingReservation: StateFlow<Pair<OrderEntity, InventoryLockEntity>?> = _activePendingReservation.asStateFlow()

    // In-app banner alert
    private val _bannerMessage = MutableStateFlow<UiMessage?>(null)
    val bannerMessage: StateFlow<UiMessage?> = _bannerMessage.asStateFlow()

    // Base flows from repository
    val allVendors: StateFlow<List<VendorEntity>> = repository.allVendors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<PushNotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class StudentFilterState(
        val location: CampusLocation,
        val radius: Double,
        val dietFilter: String?,
        val currentTime: Long
    )

    private val filterStateFlow = combine(
        _studentLocation,
        _geofenceRadiusKm,
        _dietaryFilter,
        _currentTimeTicker
    ) { location, radius, dietFilter, currentTime ->
        StudentFilterState(location, radius, dietFilter, currentTime)
    }

    // Enriched mystery boxes with vendor details, geofence, and dynamic decay price
    val enrichedBoxes: StateFlow<List<MysteryBoxWithVendor>> = combine(
        repository.allActiveBoxes,
        repository.allVendors,
        filterStateFlow
    ) { boxes, vendors, filter ->
        val vendorMap = vendors.associateBy { it.id }

        boxes.mapNotNull { box ->
            val vendor = vendorMap[box.vendorId] ?: return@mapNotNull null

            // Calculate spatial Haversine distance
            val distance = GeofenceEngine.calculateDistanceKm(
                filter.location.latitude,
                filter.location.longitude,
                vendor.latitude,
                vendor.longitude
            )
            val isWithin = distance <= filter.radius

            // Dynamic decay price calculation
            val decayPrice = DecayPricingEngine.calculateCurrentPrice(
                startPrice = box.startPrice,
                minPrice = box.minPrice,
                startTimeMillis = box.startTimeMillis,
                closingTimeMillis = box.closingTimeMillis,
                currentTimeMillis = filter.currentTime
            )

            val discount = DecayPricingEngine.calculateDiscountPercent(box.originalValue, decayPrice)
            val timeRemaining = DecayPricingEngine.formatRemainingTime(box.closingTimeMillis, filter.currentTime)
            val progress = DecayPricingEngine.calculateElapsedFraction(box.startTimeMillis, box.closingTimeMillis, filter.currentTime)

            MysteryBoxWithVendor(
                box = box,
                vendor = vendor,
                distanceKm = distance,
                isWithinGeofence = isWithin,
                currentDecayPrice = decayPrice,
                discountPercent = discount,
                timeRemainingText = timeRemaining,
                decayProgress = progress
            )
        }.filter { item ->
            // Apply dietary filter if selected
            filter.dietFilter == null || item.box.dietaryTag == filter.dietFilter
        }.sortedWith(
            compareBy<MysteryBoxWithVendor> { !it.isWithinGeofence }
                .thenBy { it.box.closingTimeMillis }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Real-time second-by-second ticker for dynamic pricing and Redis locks
        viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                _currentTimeTicker.value = now

                // Check active reservation 5-minute lock expiration
                val pending = _activePendingReservation.value
                if (pending != null) {
                    val lock = pending.second
                    if (now >= lock.expiresAtMillis) {
                        // Expired! Release back to inventory
                        repository.cancelReservationByOrder(pending.first)
                        _activePendingReservation.value = null
                        _bannerMessage.value = UiMessage.Error("5-Minute Lock Expired: Mystery box returned to inventory.")
                    }
                }

                delay(1000)
            }
        }
    }

    // Role switching
    fun switchRole(role: AppRole) {
        _currentRole.value = role
    }

    fun setStudentLocation(preset: CampusLocation) {
        _studentLocation.value = preset
        _bannerMessage.value = UiMessage.Success("Location set to: ${preset.name}")
    }

    fun setGeofenceRadius(radiusKm: Double) {
        _geofenceRadiusKm.value = radiusKm
    }

    fun setDietaryFilter(tag: String?) {
        _dietaryFilter.value = tag
    }

    fun setActiveVendorId(vendorId: Long) {
        _activeVendorId.value = vendorId
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    // Student Actions
    fun reserveBox(boxId: Long, onReserved: (OrderEntity) -> Unit) {
        viewModelScope.launch {
            val result = repository.reserveMysteryBox(boxId)
            result.onSuccess { (order, lock) ->
                _activePendingReservation.value = Pair(order, lock)
                _bannerMessage.value = UiMessage.Success("Unit locked for 5:00 minutes! Complete payment to secure.")
                onReserved(order)
            }.onFailure { error ->
                _bannerMessage.value = UiMessage.Error(error.message ?: "Failed to reserve box")
            }
        }
    }

    fun cancelActiveReservation() {
        viewModelScope.launch {
            val pending = _activePendingReservation.value
            if (pending != null) {
                repository.cancelReservationByOrder(pending.first)
                _activePendingReservation.value = null
                _bannerMessage.value = UiMessage.Success("Reservation released back to inventory.")
            }
        }
    }

    fun processRazorpayPayment(
        upiApp: String,
        onSuccess: (OrderEntity) -> Unit
    ) {
        viewModelScope.launch {
            val pending = _activePendingReservation.value
            if (pending == null) {
                _bannerMessage.value = UiMessage.Error("No active reservation to pay for")
                return@launch
            }

            val order = pending.first
            val result = repository.finalizeOrderPayment(order, upiApp)
            result.onSuccess { updatedOrder ->
                _activePendingReservation.value = null
                _bannerMessage.value = UiMessage.Success("Payment Confirmed! Pickup QR code generated.")
                onSuccess(updatedOrder)
            }.onFailure { err ->
                _bannerMessage.value = UiMessage.Error(err.message ?: "Payment processing failed")
            }
        }
    }

    // Rate Mystery Box
    fun rateMysteryBox(order: OrderEntity, stars: Int, tags: List<String>, comment: String) {
        viewModelScope.launch {
            val result = repository.submitOrderRating(order, stars, tags, comment)
            result.onSuccess { vendor ->
                if (vendor.isSuspended) {
                    _bannerMessage.value = UiMessage.Error("Account SUSPENDED: ${vendor.name} rating dropped to ${vendor.rating}★ (< 3.0 limit)!")
                } else {
                    _bannerMessage.value = UiMessage.Success("Review submitted! Thank you.")
                }
            }.onFailure { err ->
                _bannerMessage.value = UiMessage.Error(err.message ?: "Rating submission failed")
            }
        }
    }

    // Vendor Actions
    fun createVendorDeal(
        title: String,
        description: String,
        dietaryTag: String,
        originalValue: Double,
        startPrice: Double,
        minPrice: Double,
        quantity: Int,
        durationMinutes: Int,
        pickupWindow: String,
        itemsHint: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val vendorId = _activeVendorId.value
            val result = repository.createMysteryBox(
                vendorId = vendorId,
                title = title,
                description = description,
                dietaryTag = dietaryTag,
                originalValue = originalValue,
                startPrice = startPrice,
                minPrice = minPrice,
                quantity = quantity,
                durationMinutes = durationMinutes,
                pickupWindow = pickupWindow,
                itemsHint = itemsHint
            )

            result.onSuccess {
                _bannerMessage.value = UiMessage.Success("Mystery Box published! Push notification sent to students within 2km.")
                onSuccess()
            }.onFailure { err ->
                _bannerMessage.value = UiMessage.Error(err.message ?: "Failed to post deal")
            }
        }
    }

    fun verifyVendorPickup(codeOrPayload: String, onResult: (Result<OrderEntity>) -> Unit) {
        viewModelScope.launch {
            val vendorId = _activeVendorId.value
            val result = repository.verifyAndCompletePickup(codeOrPayload, vendorId)
            result.onSuccess { completedOrder ->
                _bannerMessage.value = UiMessage.Success("Pickup verified! Handover completed for ${completedOrder.orderCode}.")
            }.onFailure { err ->
                _bannerMessage.value = UiMessage.Error(err.message ?: "Pickup verification failed")
            }
            onResult(result)
        }
    }

    fun reinstateVendorTesting(vendorId: Long) {
        viewModelScope.launch {
            repository.reinstateVendorForTesting(vendorId)
            _bannerMessage.value = UiMessage.Success("Vendor reinstated for testing.")
        }
    }
}
