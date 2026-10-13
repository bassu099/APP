package com.example.ui.screens.vendor

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DietaryTag
import com.example.data.model.MysteryBoxEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.VendorEntity
import com.example.domain.DecayPricingEngine
import com.example.ui.components.DecayPriceDisplay
import com.example.ui.components.DietaryTagBadge
import com.example.ui.theme.AmberSurprise
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun VendorDashboardScreen(
    vendors: List<VendorEntity>,
    activeVendorId: Long,
    onSelectVendor: (Long) -> Unit,
    boxes: List<MysteryBoxEntity>,
    orders: List<OrderEntity>,
    currentTimeMillis: Long,
    onCreateDeal: (String, String, String, Double, Double, Double, Int, Int, String, String) -> Unit,
    onVerifyPickup: (String, (Result<OrderEntity>) -> Unit) -> Unit,
    onReinstateVendor: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentVendor = vendors.find { it.id == activeVendorId } ?: vendors.firstOrNull()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var vendorMenuExpanded by remember { mutableStateOf(false) }

    if (currentVendor == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading Vendor Profile...")
        }
        return
    }

    val vendorBoxes = boxes.filter { it.vendorId == currentVendor.id }
    val vendorOrders = orders.filter { it.vendorId == currentVendor.id }
    val readyPickups = vendorOrders.filter { it.status == OrderStatus.PAID.name }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("vendor_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Vendor Header Selector
        item {
            VendorProfileHeader(
                vendor = currentVendor,
                vendors = vendors,
                menuExpanded = vendorMenuExpanded,
                onMenuExpand = { vendorMenuExpanded = it },
                onSelectVendor = {
                    onSelectVendor(it)
                    vendorMenuExpanded = false
                }
            )
        }

        // Automated Suspension Alert per PRD Module E
        if (currentVendor.isSuspended || currentVendor.rating < 3.0) {
            item {
                VendorSuspendedCard(
                    vendor = currentVendor,
                    onReinstate = { onReinstateVendor(currentVendor.id) }
                )
            }
        }

        // Actions: Create Box & Scan QR
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showCreateDialog = true },
                    enabled = !currentVendor.isSuspended,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("create_box_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Post Mystery Box", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showScannerDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_scanner_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberSurprise)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan / Redeem QR", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        // Active Orders Awaiting Handover
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Awaiting Pickup Handover (${readyPickups.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (readyPickups.isNotEmpty()) {
                    Text(
                        text = "Scan QR to complete",
                        fontSize = 11.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (readyPickups.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No pending student pickups right now. New paid reservations appear here immediately.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
            items(readyPickups, key = { it.id }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "OTP: ${order.orderCode}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = EmeraldPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• Paid ₹${order.paidAmount.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = order.boxTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                onVerifyPickup(order.orderCode) { /* handled */ }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Handover", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Active Mystery Boxes Posted by Vendor
        item {
            Text(
                text = "Active Mystery Boxes (${vendorBoxes.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (vendorBoxes.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No active surplus boxes created. Tap 'Post Mystery Box' above to launch a decay deal!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
            items(vendorBoxes, key = { it.id }) { box ->
                val currentDecayPrice = DecayPricingEngine.calculateCurrentPrice(
                    startPrice = box.startPrice,
                    minPrice = box.minPrice,
                    startTimeMillis = box.startTimeMillis,
                    closingTimeMillis = box.closingTimeMillis,
                    currentTimeMillis = currentTimeMillis
                )
                val discount = DecayPricingEngine.calculateDiscountPercent(box.originalValue, currentDecayPrice)
                val remaining = DecayPricingEngine.formatRemainingTime(box.closingTimeMillis, currentTimeMillis)
                val progress = DecayPricingEngine.calculateElapsedFraction(box.startTimeMillis, box.closingTimeMillis, currentTimeMillis)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = box.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            DietaryTagBadge(tag = box.dietaryTag)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        DecayPriceDisplay(
                            originalPrice = box.originalValue,
                            currentPrice = currentDecayPrice,
                            minPrice = box.minPrice,
                            discountPercent = discount,
                            decayProgress = progress
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TTL: $remaining • ${box.availableQuantity} available (${box.lockedQuantity} locked)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Pickup: ${box.pickupWindow}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Deal Dialog with Dynamic Decay Curve Preview
    if (showCreateDialog) {
        CreateDealDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { title, desc, tag, orig, start, min, qty, dur, pickup, hint ->
                onCreateDeal(title, desc, tag, orig, start, min, qty, dur, pickup, hint)
                showCreateDialog = false
            }
        )
    }

    // QR & Code Pickup Scanner Dialog
    if (showScannerDialog) {
        VendorScannerDialog(
            vendorName = currentVendor.name,
            onDismiss = { showScannerDialog = false },
            onVerifyCode = { code ->
                onVerifyPickup(code) { result ->
                    if (result.isSuccess) {
                        showScannerDialog = false
                    }
                }
            }
        )
    }
}

@Composable
private fun VendorProfileHeader(
    vendor: VendorEntity,
    vendors: List<VendorEntity>,
    menuExpanded: Boolean,
    onMenuExpand: (Boolean) -> Unit,
    onSelectVendor: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMenuExpand(true) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = vendor.avatarEmoji, fontSize = 28.sp, modifier = Modifier.padding(end = 10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vendor.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Switch Vendor",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "${vendor.category} • Tap to switch test vendor",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { onMenuExpand(false) }
            ) {
                vendors.forEach { v ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(v.avatarEmoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(v.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${v.category} • ★${v.rating} ${if (v.isSuspended) "(SUSPENDED)" else ""}",
                                        fontSize = 11.sp,
                                        color = if (v.isSuspended) DangerRed else Color.Gray
                                    )
                                }
                            }
                        },
                        onClick = { onSelectVendor(v.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // FSSAI & Accountability Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = "FSSAI", tint = EmeraldLight, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FSSAI: ${vendor.fssaiLicense}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (vendor.rating >= 3.0) AmberSurprise.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = if (vendor.rating >= 3.0) AmberSurprise else DangerRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${vendor.rating} ★ (${vendor.totalRatings} Reviews)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (vendor.rating >= 3.0) AmberSurprise else DangerRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorSuspendedCard(
    vendor: VendorEntity,
    onReinstate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DangerRed)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Suspended",
                    tint = DangerRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ACCOUNT SUSPENDED",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = DangerRed
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Automated Accountability Action (PRD Module E): Vendor rating dropped to ${vendor.rating}★, which is below the mandatory 3.0★ minimum threshold. Creating new Mystery Boxes is disabled until FSSAI compliance review.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onReinstate,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                modifier = Modifier.testTag("reinstate_vendor_btn")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reinstate", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reinstate Vendor (Test Sandbox Demo)", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CreateDealDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Double, Double, Double, Int, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf("Evening Pastry Surprise Box") }
    var description by remember { mutableStateOf("Fresh assortment of daily baked specialties.") }
    var selectedDietTag by remember { mutableStateOf("VEG") }
    var originalValueText by remember { mutableStateOf("400") }
    var startPriceText by remember { mutableStateOf("200") }
    var minPriceText by remember { mutableStateOf("80") }
    var quantityText by remember { mutableStateOf("5") }
    var durationMinutes by remember { mutableIntStateOf(90) }
    var pickupWindow by remember { mutableStateOf("9:30 PM - 10:45 PM") }
    var itemsHint by remember { mutableStateOf("Croissants, muffins, and savory puffs") }

    val origVal = originalValueText.toDoubleOrNull() ?: 400.0
    val startP = startPriceText.toDoubleOrNull() ?: 200.0
    val minP = minPriceText.toDoubleOrNull() ?: 80.0
    val qty = quantityText.toIntOrNull() ?: 5

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("create_deal_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Create Mystery Box Deal",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Module A: Dynamic decay pricing & 2km geofence alert",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Box Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Dietary Classification", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DietaryTag.entries.forEach { tag ->
                            val isSelected = selectedDietTag == tag.name
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedDietTag = tag.name }
                            ) {
                                Text(
                                    text = "${tag.icon} ${tag.label}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Pricing Row: Original, Start, Min Floor
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = originalValueText,
                            onValueChange = { originalValueText = it },
                            label = { Text("Original (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = startPriceText,
                            onValueChange = { startPriceText = it },
                            label = { Text("Start (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minPriceText,
                            onValueChange = { minPriceText = it },
                            label = { Text("Floor (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Dynamic Decay Curve Visual Preview
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Dynamic Decay Curve Preview",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Starts at ₹${startP.toInt()} -> Decays linearly over $durationMinutes mins -> Reaches floor ₹${minP.toInt()}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Simple Canvas decay curve
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                val w = size.width
                                val h = size.height

                                // Draw diagonal decaying line
                                val path = Path().apply {
                                    moveTo(0f, 10f)
                                    lineTo(w, h - 10f)
                                }
                                drawPath(
                                    path = path,
                                    color = EmeraldLight,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                drawCircle(color = AmberSurprise, radius = 5.dp.toPx(), center = Offset(0f, 10f))
                                drawCircle(color = EmeraldLight, radius = 5.dp.toPx(), center = Offset(w, h - 10f))
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Total Units") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pickupWindow,
                            onValueChange = { pickupWindow = it },
                            label = { Text("Pickup Window") },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = itemsHint,
                        onValueChange = { itemsHint = it },
                        label = { Text("Surprise Items Hint (for students)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onSubmit(
                                title,
                                description,
                                selectedDietTag,
                                origVal,
                                startP,
                                minP,
                                qty,
                                durationMinutes,
                                pickupWindow,
                                itemsHint
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_create_deal_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Publish Mystery Box & Trigger Geofence", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorScannerDialog(
    vendorName: String,
    onDismiss: () -> Unit,
    onVerifyCode: (String) -> Unit
) {
    var manualCodeInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(22.dp))
                .testTag("vendor_scanner_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pickup Handover Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = vendorName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated Camera Viewfinder with animated laser line
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF07110C))
                        .border(2.dp, EmeraldLight, RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val currentY = size.height * laserY
                        drawLine(
                            color = DangerRed,
                            start = Offset(0f, currentY),
                            end = Offset(size.width, currentY),
                            strokeWidth = 3.dp.toPx()
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Align Student QR Inside",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "OR Enter Student 6-Digit Order OTP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = manualCodeInput,
                    onValueChange = { manualCodeInput = it.uppercase() },
                    placeholder = { Text("e.g. SBX-8492") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scanner_code_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (manualCodeInput.isNotBlank()) {
                            onVerifyCode(manualCodeInput)
                        }
                    },
                    enabled = manualCodeInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_code_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Verify & Confirm Handover", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
