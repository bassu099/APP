package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InventoryLockEntity
import com.example.data.model.MysteryBoxEntity
import com.example.data.model.OrderEntity
import com.example.data.model.PushNotificationEntity
import com.example.data.model.VendorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Vendors ---
    @Query("SELECT * FROM vendors ORDER BY rating DESC")
    fun getAllVendors(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE id = :id LIMIT 1")
    fun getVendorById(id: Long): Flow<VendorEntity?>

    @Query("SELECT * FROM vendors WHERE id = :id LIMIT 1")
    suspend fun getVendorByIdSync(id: Long): VendorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: VendorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendors(vendors: List<VendorEntity>)

    @Update
    suspend fun updateVendor(vendor: VendorEntity)

    @Query("UPDATE vendors SET rating = :newRating, totalRatings = :newTotal, isSuspended = :isSuspended, suspensionReason = :suspensionReason WHERE id = :vendorId")
    suspend fun updateVendorRatingAndStatus(
        vendorId: Long,
        newRating: Double,
        newTotal: Int,
        isSuspended: Boolean,
        suspensionReason: String?
    )

    // --- Mystery Boxes ---
    @Query("SELECT * FROM mystery_boxes WHERE isActive = 1 ORDER BY closingTimeMillis ASC")
    fun getAllActiveBoxes(): Flow<List<MysteryBoxEntity>>

    @Query("SELECT * FROM mystery_boxes WHERE vendorId = :vendorId ORDER BY closingTimeMillis ASC")
    fun getBoxesForVendor(vendorId: Long): Flow<List<MysteryBoxEntity>>

    @Query("SELECT * FROM mystery_boxes WHERE id = :id LIMIT 1")
    fun getBoxById(id: Long): Flow<MysteryBoxEntity?>

    @Query("SELECT * FROM mystery_boxes WHERE id = :id LIMIT 1")
    suspend fun getBoxByIdSync(id: Long): MysteryBoxEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBox(box: MysteryBoxEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoxes(boxes: List<MysteryBoxEntity>)

    @Update
    suspend fun updateBox(box: MysteryBoxEntity)

    // --- Inventory Locks (Redis TTL simulation) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLock(lock: InventoryLockEntity): Long

    @Query("SELECT * FROM inventory_locks WHERE isReleased = 0 ORDER BY expiresAtMillis ASC")
    fun getAllActiveLocks(): Flow<List<InventoryLockEntity>>

    @Query("SELECT * FROM inventory_locks WHERE boxId = :boxId AND isReleased = 0 ORDER BY expiresAtMillis DESC LIMIT 1")
    fun getActiveLockForBox(boxId: Long): Flow<InventoryLockEntity?>

    @Query("UPDATE inventory_locks SET isReleased = 1 WHERE id = :lockId")
    suspend fun releaseLockById(lockId: Long)

    @Query("UPDATE inventory_locks SET isReleased = 1 WHERE boxId = :boxId")
    suspend fun releaseAllLocksForBox(boxId: Long)

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY reservedAtMillis DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    fun getOrderById(id: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE orderCode = :code LIMIT 1")
    fun getOrderByCode(code: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE orderCode = :code LIMIT 1")
    suspend fun getOrderByCodeSync(code: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE vendorId = :vendorId ORDER BY reservedAtMillis DESC")
    fun getOrdersForVendor(vendorId: Long): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestampMillis DESC")
    fun getAllNotifications(): Flow<List<PushNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: PushNotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()
}
