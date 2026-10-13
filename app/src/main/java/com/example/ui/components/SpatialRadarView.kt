package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CampusLocation
import com.example.domain.GeofenceEngine
import com.example.ui.MysteryBoxWithVendor
import com.example.ui.theme.AmberSurprise
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpatialRadarView(
    studentLocation: CampusLocation,
    geofenceRadiusKm: Double,
    boxesWithVendors: List<MysteryBoxWithVendor>,
    onSelectBox: (MysteryBoxWithVendor) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBlip by remember { mutableStateOf<MysteryBoxWithVendor?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_wave"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Radar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hyperlocal Spatial Radar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${geofenceRadiusKm.toInt()}km Geofence Active",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Radar Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF07110C))
                .testTag("spatial_radar_canvas")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = (size.minDimension / 2f) - 20f

                // Distance concentric range rings
                val rings = listOf(0.33f, 0.66f, 1.0f)
                rings.forEachIndexed { idx, frac ->
                    val r = maxRadius * frac
                    val isGeofenceBoundary = idx == 2
                    drawCircle(
                        color = if (isGeofenceBoundary) EmeraldLight.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f),
                        radius = r,
                        center = center,
                        style = Stroke(
                            width = if (isGeofenceBoundary) 2.5f else 1f,
                            cap = StrokeCap.Round
                        )
                    )
                }

                // Crosshairs
                drawLine(
                    color = Color.White.copy(alpha = 0.1f),
                    start = Offset(center.x, center.y - maxRadius),
                    end = Offset(center.x, center.y + maxRadius),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.1f),
                    start = Offset(center.x - maxRadius, center.y),
                    end = Offset(center.x + maxRadius, center.y),
                    strokeWidth = 1f
                )

                // Pulsing wave
                val pulseRadius = maxRadius * pulseProgress
                drawCircle(
                    color = EmeraldLight.copy(alpha = (1f - pulseProgress) * 0.35f),
                    radius = pulseRadius,
                    center = center,
                    style = Stroke(width = 3f)
                )

                // Student center blip
                drawCircle(
                    color = EmeraldLight,
                    radius = 8f,
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = center
                )
            }

            // Student position label
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 28.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Text(
                        text = "You: ${studentLocation.name.take(18)}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Vendor blips plotted around the canvas
            BoxWithVendorBlips(
                studentLocation = studentLocation,
                geofenceRadiusKm = geofenceRadiusKm,
                boxes = boxesWithVendors,
                onBlipClick = {
                    selectedBlip = it
                    onSelectBox(it)
                }
            )
        }

        // Selected vendor card snippet
        val activeBlip = selectedBlip ?: boxesWithVendors.firstOrNull { it.isWithinGeofence }
        if (activeBlip != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectBox(activeBlip) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeBlip.vendor.avatarEmoji,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeBlip.vendor.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "★ ${activeBlip.vendor.rating}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberSurprise
                            )
                        }
                        Text(
                            text = "${activeBlip.box.title} • ₹${activeBlip.currentDecayPrice.toInt()} (was ₹${activeBlip.box.originalValue.toInt()})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (activeBlip.isWithinGeofence) EmeraldPrimary.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (activeBlip.isWithinGeofence) "IN 2KM ZONE" else "OUT OF ZONE",
                                color = if (activeBlip.isWithinGeofence) EmeraldLight else DangerRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${GeofenceEngine.formatDistance(activeBlip.distanceKm)} (${GeofenceEngine.estimateWalkingTimeMinutes(activeBlip.distanceKm)}m walk)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxWithVendorBlips(
    studentLocation: CampusLocation,
    geofenceRadiusKm: Double,
    boxes: List<MysteryBoxWithVendor>,
    onBlipClick: (MysteryBoxWithVendor) -> Unit
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val centerX = maxWidth / 2f
        val centerY = maxHeight / 2f
        val maxPx = (if (maxWidth < maxHeight) maxWidth else maxHeight) / 2f - 26.dp

        boxes.forEach { item ->
            val vLat = item.vendor.latitude
            val vLon = item.vendor.longitude
            val sLat = studentLocation.latitude
            val sLon = studentLocation.longitude

            val dLat = vLat - sLat
            val dLon = vLon - sLon

            // Angle and radial distance
            val angle = atan2(dLon, dLat) // radians
            val distFrac = (item.distanceKm / geofenceRadiusKm).coerceIn(0.1, 1.25)
            val r = maxPx * distFrac.toFloat()

            val offsetX = centerX + (r * sin(angle).toFloat()) - 16.dp
            val offsetY = centerY - (r * cos(angle).toFloat()) - 16.dp

            val blipColor = if (item.isWithinGeofence) EmeraldLight else Color(0xFF64748B)

            Box(
                modifier = Modifier
                    .padding(start = offsetX.coerceAtLeast(4.dp), top = offsetY.coerceAtLeast(4.dp))
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F241A).copy(alpha = 0.85f))
                    .border(1.5.dp, blipColor, CircleShape)
                    .clickable { onBlipClick(item) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.vendor.avatarEmoji,
                    fontSize = 14.sp
                )
            }
        }
    }
}
