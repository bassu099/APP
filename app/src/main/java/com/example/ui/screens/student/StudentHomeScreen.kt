package com.example.ui.screens.student

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DietaryTag
import com.example.domain.CampusLocation
import com.example.domain.GeofenceEngine
import com.example.ui.MysteryBoxWithVendor
import com.example.ui.components.DecayPriceDisplay
import com.example.ui.components.DietaryTagBadge
import com.example.ui.components.SpatialRadarView
import com.example.ui.theme.AmberSurprise
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    studentLocation: CampusLocation,
    onLocationChange: (CampusLocation) -> Unit,
    geofenceRadiusKm: Double,
    onRadiusChange: (Double) -> Unit,
    dietaryFilter: String?,
    onDietaryFilterChange: (String?) -> Unit,
    boxesWithVendors: List<MysteryBoxWithVendor>,
    onReserveBox: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRadar by remember { mutableStateOf(true) }
    var locationMenuExpanded by remember { mutableStateOf(false) }
    var selectedDetailBox by remember { mutableStateOf<MysteryBoxWithVendor?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("student_home_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Campus Location Bar
        item {
            CampusLocationBar(
                currentLocation = studentLocation,
                menuExpanded = locationMenuExpanded,
                onMenuExpand = { locationMenuExpanded = it },
                onSelectLocation = {
                    onLocationChange(it)
                    locationMenuExpanded = false
                }
            )
        }

        // Radar Visualizer & Toggle
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spatial Geofence (2km Perimeter)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (showRadar) "Hide Radar" else "Show Radar",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { showRadar = !showRadar }
                            .padding(4.dp)
                    )
                }

                if (showRadar) {
                    Spacer(modifier = Modifier.height(6.dp))
                    SpatialRadarView(
                        studentLocation = studentLocation,
                        geofenceRadiusKm = geofenceRadiusKm,
                        boxesWithVendors = boxesWithVendors,
                        onSelectBox = { selectedDetailBox = it }
                    )
                }
            }
        }

        // Geofence Radius Selector & Dietary Chips
        item {
            FilterChipsSection(
                currentRadius = geofenceRadiusKm,
                onRadiusChange = onRadiusChange,
                selectedDiet = dietaryFilter,
                onDietChange = onDietaryFilterChange
            )
        }

        // Deals Header with count
        item {
            val inZoneCount = boxesWithVendors.count { it.isWithinGeofence }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Mystery Boxes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "$inZoneCount within 2km geofence • Prices decay in real-time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Active Mystery Boxes List
        if (boxesWithVendors.isEmpty()) {
            item {
                EmptyDealsPlaceholder()
            }
        } else {
            items(boxesWithVendors, key = { it.box.id }) { item ->
                MysteryBoxCard(
                    item = item,
                    onCardClick = { selectedDetailBox = item },
                    onReserveClick = { onReserveBox(item.box.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }

    // Detail Bottom Sheet
    if (selectedDetailBox != null) {
        val detail = selectedDetailBox!!
        ModalBottomSheet(
            onDismissRequest = { selectedDetailBox = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            MysteryBoxDetailSheet(
                item = detail,
                onReserve = {
                    onReserveBox(detail.box.id)
                    selectedDetailBox = null
                },
                onDismiss = { selectedDetailBox = null }
            )
        }
    }
}

@Composable
private fun CampusLocationBar(
    currentLocation: CampusLocation,
    menuExpanded: Boolean,
    onMenuExpand: (Boolean) -> Unit,
    onSelectLocation: (CampusLocation) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .clickable { onMenuExpand(true) }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "YOUR CAMPUS LOCATION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = currentLocation.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Change Location",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { onMenuExpand(false) }
            ) {
                GeofenceEngine.CAMPUS_PRESETS.forEach { loc ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(loc.name, fontWeight = FontWeight.Bold)
                                Text(loc.description, fontSize = 11.sp, color = Color.Gray)
                            }
                        },
                        onClick = { onSelectLocation(loc) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipsSection(
    currentRadius: Double,
    onRadiusChange: (Double) -> Unit,
    selectedDiet: String?,
    onDietChange: (String?) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        // Radius filter row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Radius:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val radii = listOf(1.0 to "1 km", 2.0 to "2 km (Default)", 5.0 to "5 km", 20.0 to "All")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(radii) { (rad, label) ->
                    val isSelected = currentRadius == rad
                    FilterChip(
                        selected = isSelected,
                        onClick = { onRadiusChange(rad) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Dietary tags row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = selectedDiet == null,
                    onClick = { onDietChange(null) },
                    label = { Text("All Food", fontSize = 11.sp) }
                )
            }
            items(DietaryTag.entries) { tag ->
                val isSelected = selectedDiet == tag.name
                FilterChip(
                    selected = isSelected,
                    onClick = { onDietChange(if (isSelected) null else tag.name) },
                    label = { Text("${tag.icon} ${tag.label}", fontSize = 11.sp) }
                )
            }
        }
    }
}

@Composable
private fun MysteryBoxCard(
    item: MysteryBoxWithVendor,
    onCardClick: () -> Unit,
    onReserveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onCardClick() }
            .testTag("mystery_box_card_${item.box.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isWithinGeofence) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Vendor header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.vendor.avatarEmoji,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.vendor.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "FSSAI Verified",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = AmberSurprise,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${item.vendor.rating} (${item.vendor.totalRatings})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = " • ${item.vendor.category}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Geofence status pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.isWithinGeofence) EmeraldPrimary.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Walk",
                            tint = if (item.isWithinGeofence) EmeraldLight else DangerRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${GeofenceEngine.formatDistance(item.distanceKm)} (${GeofenceEngine.estimateWalkingTimeMinutes(item.distanceKm)}m)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isWithinGeofence) EmeraldLight else DangerRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Box Title & Dietary Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.box.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                DietaryTagBadge(tag = item.box.dietaryTag)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Items Surprise Hint
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🎁 Hint: ${item.box.surpriseItemsHint}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Dynamic Decay Price Display
            DecayPriceDisplay(
                originalPrice = item.box.originalValue,
                currentPrice = item.currentDecayPrice,
                minPrice = item.box.minPrice,
                discountPercent = item.discountPercent,
                decayProgress = item.decayProgress
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions & Expiry
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = AmberSurprise,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Closes: ${item.timeRemainingText}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberSurprise
                        )
                    }
                    Text(
                        text = "${item.box.availableQuantity} boxes left (${item.box.lockedQuantity} locked)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onReserveClick,
                    enabled = item.box.availableQuantity > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("reserve_box_btn_${item.box.id}")
                ) {
                    Text(
                        text = if (item.box.availableQuantity > 0) "Reserve Unit" else "Sold Out",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MysteryBoxDetailSheet(
    item: MysteryBoxWithVendor,
    onReserve: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = item.box.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.vendor.name} • ${item.vendor.category}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DietaryTagBadge(tag = item.box.dietaryTag)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Vendor Trust & FSSAI Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "FSSAI",
                        tint = EmeraldLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FSSAI License: ${item.vendor.fssaiLicense}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pickup Location: ${item.vendor.address}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Pickup Window: ${item.box.pickupWindow}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Decay Pricing Breakdown Explanation per PRD Module C
        Text(
            text = "Dynamic Decay Pricing Breakdown",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Price smoothly drops from starting ₹${item.box.startPrice.toInt()} to minimum floor ₹${item.box.minPrice.toInt()} as closing time approaches. Formula: max(min_price, start_price - (elapsed/total) * (start_price - min_price)).",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        DecayPriceDisplay(
            originalPrice = item.box.originalValue,
            currentPrice = item.currentDecayPrice,
            minPrice = item.box.minPrice,
            discountPercent = item.discountPercent,
            decayProgress = item.decayProgress
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Mystery Box Contents",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = item.box.description,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = AmberSurprise.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "✨ Included Items: ${item.box.surpriseItemsHint}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AmberSurprise,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close")
            }

            Button(
                onClick = onReserve,
                enabled = item.box.availableQuantity > 0,
                modifier = Modifier.weight(1.5f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Reserve for 5 Mins", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EmptyDealsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "📦", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No Mystery Boxes in this Range",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Try adjusting your geofence radius to 5km or changing your dietary filter.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
