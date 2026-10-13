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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.QrPickupCanvas
import com.example.ui.theme.AmberSurprise
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun StudentOrdersScreen(
    orders: List<OrderEntity>,
    onRateOrder: (OrderEntity, Int, List<String>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOrderForQr by remember { mutableStateOf<OrderEntity?>(null) }
    var ratingOrderTarget by remember { mutableStateOf<OrderEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("student_orders_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "My Mystery Box Orders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Show digital QR code at vendor pickup counter",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (orders.isEmpty()) {
            item {
                EmptyOrdersView()
            }
        } else {
            // Active ready for pickup first
            val activePickups = orders.filter { it.status == OrderStatus.PAID.name }
            val completedOrders = orders.filter { it.status == OrderStatus.COMPLETED.name }
            val otherOrders = orders.filter { it.status != OrderStatus.PAID.name && it.status != OrderStatus.COMPLETED.name }

            if (activePickups.isNotEmpty()) {
                item {
                    Text(
                        text = "Ready For Pickup (${activePickups.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
                items(activePickups, key = { it.id }) { order ->
                    ActiveOrderPickupCard(
                        order = order,
                        onShowQr = { selectedOrderForQr = order }
                    )
                }
            }

            if (completedOrders.isNotEmpty()) {
                item {
                    Text(
                        text = "Completed & Picked Up (${completedOrders.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(completedOrders, key = { it.id }) { order ->
                    CompletedOrderCard(
                        order = order,
                        onRate = { ratingOrderTarget = order }
                    )
                }
            }

            if (otherOrders.isNotEmpty()) {
                item {
                    Text(
                        text = "Expired / Pending (${otherOrders.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(otherOrders, key = { it.id }) { order ->
                    OtherOrderCard(order = order)
                }
            }
        }
    }

    // QR Pickup Dialog
    if (selectedOrderForQr != null) {
        val targetOrder = selectedOrderForQr!!
        Dialog(onDismissRequest = { selectedOrderForQr = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pickup Verification Pass",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { selectedOrderForQr = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = targetOrder.boxTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Vendor: ${targetOrder.vendorName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Canvas
                    QrPickupCanvas(payload = targetOrder.qrSecretPayload)

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6-digit OTP Code
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OTP: ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = targetOrder.orderCode,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Present this QR code or 6-digit code to the vendor counter to collect your mystery food box.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { selectedOrderForQr = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }

    // Rate Order Dialog (Module E Accountability)
    if (ratingOrderTarget != null) {
        val targetOrder = ratingOrderTarget!!
        RatingDialog(
            order = targetOrder,
            onDismiss = { ratingOrderTarget = null },
            onSubmit = { stars, tags, comment ->
                onRateOrder(targetOrder, stars, tags, comment)
                ratingOrderTarget = null
            }
        )
    }
}

@Composable
private fun ActiveOrderPickupCard(
    order: OrderEntity,
    onShowQr: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderCode}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "READY FOR PICKUP",
                        color = EmeraldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Code: ${order.orderCode}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = order.boxTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Vendor: ${order.vendorName} • Paid ₹${order.paidAmount.toInt()}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onShowQr,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("show_qr_btn_${order.orderCode}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "QR",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Show Pickup QR Code", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CompletedOrderCard(
    order: OrderEntity,
    onRate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Handover Completed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SuccessGreen
                    )
                }
                Text(
                    text = "₹${order.paidAmount.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = order.boxTitle, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${order.vendorName} • Order ${order.orderCode}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (order.studentRating > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberSurprise.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rated: ${"★".repeat(order.studentRating)} (${order.studentRating} / 5)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberSurprise
                        )
                        if (order.ratingTags.isNotBlank()) {
                            Text(
                                text = " • ${order.ratingTags}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onRate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("rate_box_btn_${order.orderCode}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rate",
                        tint = AmberSurprise,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rate Mystery Box (1-5★)")
                }
            }
        }
    }
}

@Composable
private fun OtherOrderCard(order: OrderEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${order.status}: ${order.boxTitle}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Order ${order.orderCode} • ₹${order.paidAmount.toInt()}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RatingDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onSubmit: (Int, List<String>, String) -> Unit
) {
    var ratingStars by remember { mutableIntStateOf(5) }
    val selectedTags = remember { mutableStateListOf<String>() }
    var comments by remember { mutableStateOf("") }

    val feedbackTags = listOf(
        "Generous Portion",
        "Super Fresh",
        "Great Value",
        "Eco Packaging",
        "Warm & Crispy",
        "Small Portion",
        "Cold Food"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Rate Your Mystery Box",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${order.boxTitle} from ${order.vendorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive 5-Star Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        val isFilled = i <= ratingStars
                        IconButton(
                            onClick = { ratingStars = i },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "$i stars",
                                tint = if (isFilled) AmberSurprise else Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Text(
                    text = when (ratingStars) {
                        5 -> "Amazing! Highly Recommended (5/5)"
                        4 -> "Good value & taste (4/5)"
                        3 -> "Average experience (3/5)"
                        2 -> "Disappointing portion/taste (2/5)"
                        else -> "Poor / Cold surplus (1/5)"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (ratingStars < 3) DangerRed else AmberSurprise,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Accountability Notice (PRD Module E)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (ratingStars < 3) DangerRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = if (ratingStars < 3) {
                            "⚠️ Vendor Accountability: Ratings below 3.0 trigger automated account suspension."
                        } else {
                            "🛡️ Vendor Accountability: High ratings protect vendors from suspension (min 3.0★ required)."
                        },
                        fontSize = 10.sp,
                        color = if (ratingStars < 3) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Tag Chips
                Text(
                    text = "Select Feedback Tags:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(feedbackTags) { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                            },
                            label = { Text(tag, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    label = { Text("Optional Comments") },
                    placeholder = { Text("What surprise items did you receive?") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onSubmit(ratingStars, selectedTags.toList(), comments) },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_rating_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Submit Rating", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyOrdersView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎟️", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No Orders Yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Reserve a Mystery Box in the Student Explore tab to generate a pickup QR code.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
