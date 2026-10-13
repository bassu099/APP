package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderStatus
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.components.RoleSwitcherHeader
import com.example.ui.components.SpatialRadarView
import com.example.ui.screens.student.CheckoutDialog
import com.example.ui.screens.student.StudentHomeScreen
import com.example.ui.screens.student.StudentOrdersScreen
import com.example.ui.screens.vendor.VendorDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.SurplusViewModel
import com.example.ui.AppRole

class MainActivity : ComponentActivity() {

    private val viewModel: SurplusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SurplusApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SurplusApp(viewModel: SurplusViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val studentLocation by viewModel.studentLocation.collectAsStateWithLifecycle()
    val geofenceRadius by viewModel.geofenceRadiusKm.collectAsStateWithLifecycle()
    val dietaryFilter by viewModel.dietaryFilter.collectAsStateWithLifecycle()
    val activeVendorId by viewModel.activeVendorId.collectAsStateWithLifecycle()
    val currentTimeTicker by viewModel.currentTimeTicker.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()
    val activeReservation by viewModel.activePendingReservation.collectAsStateWithLifecycle()

    val vendors by viewModel.allVendors.collectAsStateWithLifecycle()
    val enrichedBoxes by viewModel.enrichedBoxes.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()

    var studentNavTab by remember { mutableIntStateOf(0) } // 0: Explore, 1: Orders, 2: Campus Map

    val activePickupCount = allOrders.count { it.status == OrderStatus.PAID.name }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            Column {
                RoleSwitcherHeader(
                    currentRole = currentRole,
                    onRoleSelected = { viewModel.switchRole(it) }
                )
                InAppNotificationBanner(
                    message = bannerMessage,
                    onDismiss = { viewModel.clearBanner() }
                )
            }
        },
        bottomBar = {
            if (currentRole == AppRole.STUDENT) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = studentNavTab == 0,
                        onClick = { studentNavTab = 0 },
                        icon = { Icon(Icons.Default.Explore, contentDescription = "Explore") },
                        label = { Text("Explore Deals") },
                        modifier = Modifier.testTag("nav_explore_tab")
                    )

                    NavigationBarItem(
                        selected = studentNavTab == 1,
                        onClick = { studentNavTab = 1 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (activePickupCount > 0) {
                                        Badge { Text(activePickupCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ConfirmationNumber, contentDescription = "Orders")
                            }
                        },
                        label = { Text("Pickup & Orders") },
                        modifier = Modifier.testTag("nav_orders_tab")
                    )

                    NavigationBarItem(
                        selected = studentNavTab == 2,
                        onClick = { studentNavTab = 2 },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                        label = { Text("Campus Radar") },
                        modifier = Modifier.testTag("nav_radar_tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                AppRole.STUDENT -> {
                    when (studentNavTab) {
                        0 -> {
                            StudentHomeScreen(
                                studentLocation = studentLocation,
                                onLocationChange = { viewModel.setStudentLocation(it) },
                                geofenceRadiusKm = geofenceRadius,
                                onRadiusChange = { viewModel.setGeofenceRadius(it) },
                                dietaryFilter = dietaryFilter,
                                onDietaryFilterChange = { viewModel.setDietaryFilter(it) },
                                boxesWithVendors = enrichedBoxes,
                                onReserveBox = { boxId ->
                                    viewModel.reserveBox(boxId) { /* reservation opened */ }
                                }
                            )
                        }
                        1 -> {
                            StudentOrdersScreen(
                                orders = allOrders,
                                onRateOrder = { order, stars, tags, comment ->
                                    viewModel.rateMysteryBox(order, stars, tags, comment)
                                }
                            )
                        }
                        2 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Campus Spatial Geofence Map",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "PostGIS ST_DWithin 2km radius simulation. Tap any vendor blip to inspect distance.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                SpatialRadarView(
                                    studentLocation = studentLocation,
                                    geofenceRadiusKm = geofenceRadius,
                                    boxesWithVendors = enrichedBoxes,
                                    onSelectBox = { studentNavTab = 0 },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                AppRole.VENDOR -> {
                    VendorDashboardScreen(
                        vendors = vendors,
                        activeVendorId = activeVendorId,
                        onSelectVendor = { viewModel.setActiveVendorId(it) },
                        boxes = enrichedBoxes.map { it.box },
                        orders = allOrders,
                        currentTimeMillis = currentTimeTicker,
                        onCreateDeal = { title, desc, tag, orig, start, min, qty, dur, pickup, hint ->
                            viewModel.createVendorDeal(title, desc, tag, orig, start, min, qty, dur, pickup, hint) {}
                        },
                        onVerifyPickup = { code, onResult ->
                            viewModel.verifyVendorPickup(code, onResult)
                        },
                        onReinstateVendor = { vendorId ->
                            viewModel.reinstateVendorTesting(vendorId)
                        }
                    )
                }
            }

            // Checkout Dialog with 5-Minute Redis-style Ephemeral Lock
            activeReservation?.let { reservation ->
                CheckoutDialog(
                    reservation = reservation,
                    currentTimeMillis = currentTimeTicker,
                    onConfirmPayment = { upiApp ->
                        viewModel.processRazorpayPayment(upiApp) {
                            studentNavTab = 1 // Switch to orders tab to see pickup QR
                        }
                    },
                    onCancelReservation = {
                        viewModel.cancelActiveReservation()
                    }
                )
            }
        }
    }
}
