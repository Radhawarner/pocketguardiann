package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GuardianSessionEntity
import com.example.data.model.SafeZoneEntity
import com.example.service.GuardianUiState
import com.example.ui.theme.EmergencyContainer
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedLight
import com.example.ui.theme.OnEmergencyContainer
import com.example.ui.theme.OnSafeContainer
import com.example.ui.theme.OnWarningContainer
import com.example.ui.theme.SafeContainer
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuardianScreen(
    guardianState: GuardianUiState,
    allSessions: List<GuardianSessionEntity>,
    safeZones: List<SafeZoneEntity>,
    onStartJourney: (destination: String, arrivalMinutes: Int, checkInIntervalMinutes: Int) -> Unit,
    onConfirmSafe: () -> Unit,
    onRequestHelp: () -> Unit,
    onCompleteJourney: () -> Unit,
    onSimulateCheckInPrompt: () -> Unit,
    onAddSafeZone: (name: String, cat: String, addr: String, lat: Double, lng: Double, radius: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var destinationInput by remember { mutableStateOf("KSRCT College Gate") }
    var selectedArrivalMinutes by remember { mutableIntStateOf(30) }
    var selectedIntervalMinutes by remember { mutableIntStateOf(15) }
    var isAddSafeZoneOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Active Journey or Start Form
        if (guardianState.hasActiveJourney) {
            ActiveJourneyCard(
                guardianState = guardianState,
                onConfirmSafe = onConfirmSafe,
                onRequestHelp = onRequestHelp,
                onCompleteJourney = onCompleteJourney,
                onSimulateCheckInPrompt = onSimulateCheckInPrompt
            )
        } else {
            StartJourneyCard(
                destinationInput = destinationInput,
                onDestinationChange = { destinationInput = it },
                selectedArrivalMinutes = selectedArrivalMinutes,
                onArrivalMinutesChange = { selectedArrivalMinutes = it },
                selectedIntervalMinutes = selectedIntervalMinutes,
                onIntervalMinutesChange = { selectedIntervalMinutes = it },
                onStartClick = {
                    if (destinationInput.isNotBlank()) {
                        onStartJourney(destinationInput, selectedArrivalMinutes, selectedIntervalMinutes)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Safe Zones & Designated Havens Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SAFE HAVENS & GEO-ZONES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Designated shelters with 1-tap navigation",
                    fontSize = 12.sp,
                    color = Slate500
                )
            }

            OutlinedButton(
                onClick = { isAddSafeZoneOpen = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp).testTag("add_safe_zone_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Zone", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Safe Zones list
        safeZones.forEach { zone ->
            SafeZoneItemCard(
                zone = zone,
                onNavigate = {
                    val uri = Uri.parse("geo:${zone.latitude},${zone.longitude}?q=${Uri.encode(zone.name)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        // fallback web maps
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${zone.latitude},${zone.longitude}"))
                        context.startActivity(webIntent)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Guardian Mode Policy Explainer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Safety Policy",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HOW GUARDIAN MODE PROTECTS YOU",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Set destination and check-in intervals (e.g. 15 or 20 minutes).\n" +
                            "2. At each interval, tap 'I'm Safe' within the 60-second grace window.\n" +
                            "3. If a check-in is missed, contacts receive an escalation alert with your last verified GPS coordinates.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (isAddSafeZoneOpen) {
        AddSafeZoneDialog(
            onDismiss = { isAddSafeZoneOpen = false },
            onSave = { name, cat, addr, lat, lng, radius ->
                onAddSafeZone(name, cat, addr, lat, lng, radius)
                isAddSafeZoneOpen = false
            }
        )
    }
}

@Composable
fun SafeZoneItemCard(
    zone: SafeZoneEntity,
    onNavigate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("safe_zone_${zone.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = SafeContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (zone.category) {
                                "Police Station" -> Icons.Default.LocalPolice
                                "Hospital" -> Icons.Default.LocalHospital
                                "Campus" -> Icons.Default.School
                                else -> Icons.Default.Home
                            },
                            contentDescription = null,
                            tint = SafeGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = zone.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "${zone.category} • ${zone.address}",
                        fontSize = 11.sp,
                        color = Slate500,
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = onNavigate,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Route", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddSafeZoneDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, cat: String, addr: String, lat: Double, lng: Double, radius: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Campus") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Safe Zone") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Zone Name *") },
                    placeholder = { Text("e.g. Student Center, Main Dorm") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("safe_zone_name_input")
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Location Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Campus", "Police", "Home", "Hospital").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        // Use default campus coords for demo zone
                        onSave(name, category, address, 37.7749, -122.4194, 200)
                    }
                },
                modifier = Modifier.testTag("save_safe_zone_btn")
            ) {
                Text("Save Safe Zone")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StartJourneyCard(
    destinationInput: String,
    onDestinationChange: (String) -> Unit,
    selectedArrivalMinutes: Int,
    onArrivalMinutesChange: (Int) -> Unit,
    selectedIntervalMinutes: Int,
    onIntervalMinutesChange: (Int) -> Unit,
    onStartClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("start_journey_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = "Start Journey",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Start Safety Journey",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Automated check-ins along your route",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Destination Field
            OutlinedTextField(
                value = destinationInput,
                onValueChange = onDestinationChange,
                label = { Text("Destination or Route") },
                placeholder = { Text("e.g. KSRCT College, Downtown Hub") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Destination")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journey_destination_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Expected Duration Chips
            Text(
                text = "Estimated Travel Time",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 30, 45, 60).forEach { mins ->
                    FilterChip(
                        selected = selectedArrivalMinutes == mins,
                        onClick = { onArrivalMinutesChange(mins) },
                        label = { Text("$mins mins") },
                        leadingIcon = if (selectedArrivalMinutes == mins) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Check-in interval chips
            Text(
                text = "Check-in Frequency",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10, 15, 20, 30).forEach { interval ->
                    FilterChip(
                        selected = selectedIntervalMinutes == interval,
                        onClick = { onIntervalMinutesChange(interval) },
                        label = { Text("Every $interval min") },
                        leadingIcon = if (selectedIntervalMinutes == interval) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_guardian_journey_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START GUARDIAN MODE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActiveJourneyCard(
    guardianState: GuardianUiState,
    onConfirmSafe: () -> Unit,
    onRequestHelp: () -> Unit,
    onCompleteJourney: () -> Unit,
    onSimulateCheckInPrompt: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_journey_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (guardianState.isCheckInPromptVisible) WarningContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (guardianState.isCheckInPromptVisible) WarningAmber else MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (guardianState.isCheckInPromptVisible) WarningAmber else SafeGreen,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (guardianState.isCheckInPromptVisible) Icons.Default.Warning else Icons.AutoMirrored.Filled.DirectionsWalk,
                                contentDescription = "Active Status",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "JOURNEY ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (guardianState.isCheckInPromptVisible) OnWarningContainer else SafeGreen,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = guardianState.destination,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SafeContainer
                ) {
                    Text(
                        text = "LIVE",
                        color = OnSafeContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Check-in Countdown Box
            val mins = guardianState.secondsUntilNextCheckIn / 60
            val secs = guardianState.secondsUntilNextCheckIn % 60
            val formattedTime = String.format(Locale.US, "%02d:%02d", mins, secs)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Next Check-in in",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                        Text(
                            text = formattedTime,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Check-in Prompt Dialog / Banner
            AnimatedVisibility(visible = guardianState.isCheckInPromptVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ARE YOU SAFE?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = OnWarningContainer
                    )
                    Text(
                        text = "Auto-escalation in ${guardianState.checkInGraceRemainingSeconds}s",
                        fontSize = 12.sp,
                        color = OnWarningContainer,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onConfirmSafe,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("confirm_safe_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("I'M SAFE", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onRequestHelp,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("need_help_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Emergency, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("NEED HELP", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSimulateCheckInPrompt,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("test_checkin_prompt_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Test Check-in", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onCompleteJourney,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("complete_journey_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Arrived Safe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
