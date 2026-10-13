package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppDao
import com.example.data.model.InventoryLockEntity
import com.example.data.model.MysteryBoxEntity
import com.example.data.model.OrderEntity
import com.example.data.model.PushNotificationEntity
import com.example.data.model.VendorEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        VendorEntity::class,
        MysteryBoxEntity::class,
        InventoryLockEntity::class,
        OrderEntity::class,
        PushNotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "surplus_box_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database.appDao())
                }
            }
        }
    }
}

suspend fun populateInitialData(dao: AppDao) {
    val now = System.currentTimeMillis()

    val vendors = listOf(
        VendorEntity(
            id = 1,
            name = "The Crust & Crumb Patisserie",
            category = "Bakery & Patisserie",
            fssaiLicense = "11223344556677",
            address = "Shop 4, Campus Promenade, North Gate",
            latitude = 12.9725,
            longitude = 77.5935,
            rating = 4.8,
            totalRatings = 210,
            isSuspended = false,
            avatarEmoji = "🥐",
            openingHours = "8:00 AM - 11:00 PM",
            bannerGradientIndex = 0
        ),
        VendorEntity(
            id = 2,
            name = "Spice Route Dum Biryani",
            category = "Biryani & Bowls",
            fssaiLicense = "11559988443322",
            address = "Plot 12, University Food Court Lane",
            latitude = 12.9760,
            longitude = 77.5970,
            rating = 4.6,
            totalRatings = 345,
            isSuspended = false,
            avatarEmoji = "🥘",
            openingHours = "11:00 AM - 11:45 PM",
            bannerGradientIndex = 1
        ),
        VendorEntity(
            id = 3,
            name = "Campus Chai & Samosa Hub",
            category = "Cafe & Street Bites",
            fssaiLicense = "11883377221199",
            address = "Near Central Library Steps",
            latitude = 12.9695,
            longitude = 77.5910,
            rating = 4.4,
            totalRatings = 180,
            isSuspended = false,
            avatarEmoji = "☕",
            openingHours = "7:30 AM - 10:30 PM",
            bannerGradientIndex = 2
        ),
        VendorEntity(
            id = 4,
            name = "Woodfired Crust Pizzeria",
            category = "Artisan Pizzeria",
            fssaiLicense = "11994466228833",
            address = "Sector 3, Student Plaza",
            latitude = 12.9780,
            longitude = 77.5925,
            rating = 4.7,
            totalRatings = 156,
            isSuspended = false,
            avatarEmoji = "🍕",
            openingHours = "12:00 PM - 11:30 PM",
            bannerGradientIndex = 3
        ),
        VendorEntity(
            id = 5,
            name = "Green Grove Bowls & Deli",
            category = "Healthy Deli & Salads",
            fssaiLicense = "11332211445566",
            address = "Hostel Block A Commercial complex",
            latitude = 12.9640,
            longitude = 77.5960,
            rating = 3.1, // Near 3.0 threshold for testing auto-suspension!
            totalRatings = 48,
            isSuspended = false,
            avatarEmoji = "🥗",
            openingHours = "9:00 AM - 10:15 PM",
            bannerGradientIndex = 4
        ),
        VendorEntity(
            id = 6,
            name = "Highway 24 Diner (Out-of-Radius)",
            category = "All Night Diner",
            fssaiLicense = "11776655443322",
            address = "Expressway Junction, 4.8km away",
            latitude = 12.9980,
            longitude = 77.6300, // ~4.8 km away
            rating = 4.2,
            totalRatings = 88,
            isSuspended = false,
            avatarEmoji = "🍔",
            openingHours = "Open 24 Hours",
            bannerGradientIndex = 0
        )
    )
    dao.insertVendors(vendors)

    val boxes = listOf(
        MysteryBoxEntity(
            id = 1,
            vendorId = 1,
            title = "Artisan French Pastry Mystery Box",
            description = "Surplus batch of morning and evening baked flaky pastries. Packed in eco-friendly kraft box.",
            dietaryTag = "EGG",
            originalValue = 450.0,
            startPrice = 220.0,
            minPrice = 99.0,
            totalQuantity = 5,
            availableQuantity = 4,
            lockedQuantity = 0,
            startTimeMillis = now - 35 * 60 * 1000L, // started 35 mins ago
            closingTimeMillis = now + 85 * 60 * 1000L, // closes in 85 mins
            pickupWindow = "9:30 PM - 10:45 PM",
            surpriseItemsHint = "Contains 3-4 items: e.g. Almond Croissant, Pain au Chocolat, Danish Custard Whirl, or Cinnamon Bun",
            isActive = true
        ),
        MysteryBoxEntity(
            id = 2,
            vendorId = 2,
            title = "Royal Awadhi Dum Biryani Feast Box",
            description = "Slow-cooked saffron dum biryani sealed with dough. Freshly made surplus handi portions.",
            dietaryTag = "NON_VEG",
            originalValue = 380.0,
            startPrice = 190.0,
            minPrice = 85.0,
            totalQuantity = 6,
            availableQuantity = 5,
            lockedQuantity = 0,
            startTimeMillis = now - 20 * 60 * 1000L,
            closingTimeMillis = now + 100 * 60 * 1000L,
            pickupWindow = "10:00 PM - 11:30 PM",
            surpriseItemsHint = "Full rice & tender chicken portion, Mirchi ka Salan, Burani Raita, and 1 Warm Gulab Jamun",
            isActive = true
        ),
        MysteryBoxEntity(
            id = 3,
            vendorId = 3,
            title = "Evening Campus Snack Combo Box",
            description = "End of evening golden fried crispy savory treats paired with specialty chutneys.",
            dietaryTag = "VEG",
            originalValue = 250.0,
            startPrice = 120.0,
            minPrice = 50.0,
            totalQuantity = 8,
            availableQuantity = 7,
            lockedQuantity = 0,
            startTimeMillis = now - 50 * 60 * 1000L,
            closingTimeMillis = now + 40 * 60 * 1000L,
            pickupWindow = "9:00 PM - 10:15 PM",
            surpriseItemsHint = "3x Paneer Samosas, 2x Khasta Kachoris, Masala Chai Bun, and signature spicy mint dip",
            isActive = true
        ),
        MysteryBoxEntity(
            id = 4,
            vendorId = 4,
            title = "Woodfired Slices & Cheesy Bread Box",
            description = "Gourmet sourdough slices from our late oven batch. Heated and packed fresh for pickup.",
            dietaryTag = "VEG",
            originalValue = 520.0,
            startPrice = 260.0,
            minPrice = 110.0,
            totalQuantity = 4,
            availableQuantity = 3,
            lockedQuantity = 0,
            startTimeMillis = now - 15 * 60 * 1000L,
            closingTimeMillis = now + 115 * 60 * 1000L,
            pickupWindow = "10:15 PM - 11:30 PM",
            surpriseItemsHint = "2 Jumbo slices (Margherita / Farmhouse Truffle) + 4 Cheesy Garlic Pull-Apart sticks",
            isActive = true
        ),
        MysteryBoxEntity(
            id = 5,
            vendorId = 5,
            title = "Harvest Macro Bowl & Fresh Juice Box",
            description = "Freshly prepared wholesome salads and cold-pressed botanical refreshments.",
            dietaryTag = "VEGAN",
            originalValue = 360.0,
            startPrice = 180.0,
            minPrice = 75.0,
            totalQuantity = 4,
            availableQuantity = 4,
            lockedQuantity = 0,
            startTimeMillis = now - 45 * 60 * 1000L,
            closingTimeMillis = now + 55 * 60 * 1000L,
            pickupWindow = "9:15 PM - 10:30 PM",
            surpriseItemsHint = "Rainbow Quinoa & Roasted Veggie Bowl + 250ml Cold Pressed Orange Ginger Elixir",
            isActive = true
        ),
        MysteryBoxEntity(
            id = 6,
            vendorId = 6,
            title = "Diner Burger & Crispy Fries Box (Far)",
            description = "Late night smash burger surplus. Beyond campus 2km perimeter.",
            dietaryTag = "NON_VEG",
            originalValue = 420.0,
            startPrice = 210.0,
            minPrice = 90.0,
            totalQuantity = 3,
            availableQuantity = 3,
            lockedQuantity = 0,
            startTimeMillis = now - 10 * 60 * 1000L,
            closingTimeMillis = now + 120 * 60 * 1000L,
            pickupWindow = "10:30 PM - 12:00 AM",
            surpriseItemsHint = "Double smashed patty cheeseburger with seasoned curly fries",
            isActive = true
        )
    )
    dao.insertBoxes(boxes)

    val notifications = listOf(
        PushNotificationEntity(
            title = "⚡ Geofence Alert: 2km Radius",
            message = "The Crust & Crumb just posted a French Pastry Mystery Box! Price is decaying towards ₹99.",
            vendorId = 1,
            boxId = 1,
            timestampMillis = now - 10 * 60 * 1000L
        ),
        PushNotificationEntity(
            title = "🔥 Price Drop Alert!",
            message = "Campus Chai & Samosa box price dropped to ₹78! Closes in 40 mins.",
            vendorId = 3,
            boxId = 3,
            timestampMillis = now - 5 * 60 * 1000L
        )
    )
    for (notif in notifications) {
        dao.insertNotification(notif)
    }
}
