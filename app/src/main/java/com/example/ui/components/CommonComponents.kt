package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DietaryTag
import com.example.domain.QrGenerator
import com.example.ui.AppRole
import com.example.ui.UiMessage
import com.example.ui.theme.AmberSurprise
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun RoleSwitcherHeader(
    currentRole: AppRole,
    onRoleSelected: (AppRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RoleOptionPill(
                title = "Student Mode",
                subtitle = "Explore & Reserve",
                icon = Icons.Default.School,
                isSelected = currentRole == AppRole.STUDENT,
                onClick = { onRoleSelected(AppRole.STUDENT) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("role_student_btn")
            )
            Spacer(modifier = Modifier.width(6.dp))
            RoleOptionPill(
                title = "Vendor Portal",
                subtitle = "Manage & Handover",
                icon = Icons.Default.Storefront,
                isSelected = currentRole == AppRole.VENDOR,
                onClick = { onRoleSelected(AppRole.VENDOR) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("role_vendor_btn")
            )
        }
    }
}

@Composable
private fun RoleOptionPill(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textCol = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textCol,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = textCol.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun InAppNotificationBanner(
    message: UiMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        if (message == null) return@AnimatedVisibility

        val (bgCol, iconCol, titleText, bodyText) = when (message) {
            is UiMessage.Success -> {
                Quad(SuccessGreen, Color.White, "Success", message.message)
            }
            is UiMessage.Error -> {
                Quad(DangerRed, Color.White, "Notice", message.message)
            }
            is UiMessage.GeofenceAlert -> {
                Quad(AmberSurprise, Color.White, message.title, message.message)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = bgCol),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Alert",
                    tint = iconCol,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = bodyText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@Composable
fun DietaryTagBadge(tag: String, modifier: Modifier = Modifier) {
    val (label, icon, color) = when (tag.uppercase()) {
        "VEG" -> Triple("Pure Veg", "🌱", EmeraldLight)
        "NON_VEG" -> Triple("Non-Veg", "🍗", DangerRed)
        "EGG" -> Triple("Egg", "🥚", AmberSurprise)
        "VEGAN" -> Triple("Vegan", "🌿", EmeraldPrimary)
        else -> Triple(tag, "🍽️", MaterialTheme.colorScheme.primary)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DecayPriceDisplay(
    originalPrice: Double,
    currentPrice: Double,
    minPrice: Double,
    discountPercent: Int,
    decayProgress: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "₹${currentPrice.toInt()}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "₹${originalPrice.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = DangerRed.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "$discountPercent% OFF",
                    color = DangerRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Decay progression line
        Column {
            LinearProgressIndicator(
                progress = { decayProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AmberSurprise,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Decaying from start",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = "Floor: ₹${minPrice.toInt()}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberSurprise
                )
            }
        }
    }
}

@Composable
fun QrPickupCanvas(
    payload: String,
    modifier: Modifier = Modifier
) {
    val matrix = QrGenerator.generateQrMatrix(payload)
    val matrixSize = matrix.size

    Surface(
        modifier = modifier
            .size(210.dp)
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(190.dp)) {
            val moduleWidth = size.width / matrixSize
            val moduleHeight = size.height / matrixSize

            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (matrix[r][c]) {
                        drawRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(c * moduleWidth, r * moduleHeight),
                            size = Size(moduleWidth + 0.5f, moduleHeight + 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockCountdownBadge(
    expiresAtMillis: Long,
    currentTimeMillis: Long,
    modifier: Modifier = Modifier
) {
    val remainingMillis = (expiresAtMillis - currentTimeMillis).coerceAtLeast(0L)
    val remainingSec = remainingMillis / 1000
    val minutes = remainingSec / 60
    val seconds = remainingSec % 60
    val isCritical = remainingSec < 60

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isCritical) DangerRed.copy(alpha = 0.15f) else AmberSurprise.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCritical) DangerRed.copy(alpha = 0.6f) else AmberSurprise.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = if (isCritical) DangerRed else AmberSurprise,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("Locked: %02d:%02d", minutes, seconds),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCritical) DangerRed else AmberSurprise
            )
        }
    }
}
