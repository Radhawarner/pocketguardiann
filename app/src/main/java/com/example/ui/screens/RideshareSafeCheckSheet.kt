package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RideshareSafeCheck
import com.example.service.LocationState
import com.example.ui.theme.SafeContainer
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer

@Composable
fun RideshareSafeCheckScreen(
    rideshareState: RideshareSafeCheck,
    locationState: LocationState,
    onUpdateDetails: (plate: String, driver: String, vehicle: String, service: String, destination: String) -> Unit,
    onStartSafetyTimer: (minutes: Int, activity: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var service by remember(rideshareState) { mutableStateOf(rideshareState.rideService.ifBlank { "Uber" }) }
    var plateNumber by remember(rideshareState) { mutableStateOf(rideshareState.plateNumber) }
    var driverName by remember(rideshareState) { mutableStateOf(rideshareState.driverName) }
    var vehicleModel by remember(rideshareState) { mutableStateOf(rideshareState.vehicleModelColor) }
    var destination by remember(rideshareState) { mutableStateOf(rideshareState.destination) }

    // Checklist states
    var checkPlateMatch by remember { mutableStateOf(false) }
    var checkAskDriver by remember { mutableStateOf(false) }
    var checkDoorHandles by remember { mutableStateOf(false) }
    var checkSharedWithContact by remember { mutableStateOf(false) }

    val allChecked = checkPlateMatch && checkAskDriver && checkDoorHandles

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("rideshare_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Rideshare SafeCheck",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Verify vehicle, plate, & broadcast trip info",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_rideshare_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Service Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Uber", "Lyft", "Yellow Cab", "Carpool").forEach { s ->
                        val isSelected = service == s
                        FilterChip(
                            selected = isSelected,
                            onClick = { service = s },
                            label = { Text(s, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // License Plate Input Card with Big Visual Plate Badge
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "VEHICLE & DRIVER VERIFICATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual License Plate Graphic
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(2.dp, Color(0xFF334155))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = service.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF64748B),
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (plateNumber.isNotBlank()) plateNumber.uppercase() else "ENTER PLATE",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (plateNumber.isNotBlank()) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                    letterSpacing = 3.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = plateNumber,
                            onValueChange = {
                                plateNumber = it
                                onUpdateDetails(plateNumber, driverName, vehicleModel, service, destination)
                            },
                            label = { Text("Vehicle License Plate") },
                            placeholder = { Text("e.g. 7XYZ890") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("rideshare_plate_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = driverName,
                                onValueChange = {
                                    driverName = it
                                    onUpdateDetails(plateNumber, driverName, vehicleModel, service, destination)
                                },
                                label = { Text("Driver Name") },
                                placeholder = { Text("e.g. Carlos") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = vehicleModel,
                                onValueChange = {
                                    vehicleModel = it
                                    onUpdateDetails(plateNumber, driverName, vehicleModel, service, destination)
                                },
                                label = { Text("Car Make / Model") },
                                placeholder = { Text("e.g. Silver Camry") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = destination,
                            onValueChange = {
                                destination = it
                                onUpdateDetails(plateNumber, driverName, vehicleModel, service, destination)
                            },
                            label = { Text("Drop-off Destination") },
                            placeholder = { Text("e.g. Home, Airport, Campus Dorms") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 4-Point Safety Checklist Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = if (allChecked) SafeContainer else MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (allChecked) SafeGreen else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CRITICAL SAFETY CHECKLIST",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (allChecked) SafeGreen else Slate500,
                                letterSpacing = 1.sp
                            )
                            if (allChecked) {
                                Surface(shape = RoundedCornerShape(8.dp), color = SafeGreen) {
                                    Text(
                                        text = "VERIFIED SAFE TO ENTER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ChecklistItem(
                            checked = checkAskDriver,
                            onCheckedChange = { checkAskDriver = it },
                            title = "Ask: \"Who are you picking up?\"",
                            subtitle = "Do not state your own name first; make the driver verify you."
                        )

                        ChecklistItem(
                            checked = checkPlateMatch,
                            onCheckedChange = { checkPlateMatch = it },
                            title = "Confirm License Plate Matches",
                            subtitle = "Check that the rear plate matches the app character by character."
                        )

                        ChecklistItem(
                            checked = checkDoorHandles,
                            onCheckedChange = { checkDoorHandles = it },
                            title = "Inspect Interior Door Handles",
                            subtitle = "Verify inside door handle functions and child-safety lock is off."
                        )

                        ChecklistItem(
                            checked = checkSharedWithContact,
                            onCheckedChange = { checkSharedWithContact = it },
                            title = "Share Ride Details with Contact",
                            subtitle = "Broadcast your ride details & live location before car moves."
                        )
                    }
                }

                // 1-Tap Share Ride Action
                Button(
                    onClick = {
                        checkSharedWithContact = true
                        val shareText = buildString {
                            appendLine("POCKET GUARDIAN RIDESHARE SAFETY ALERT:")
                            appendLine("I am in a $service ride heading to: ${destination.ifBlank { "Destination" }}")
                            appendLine("Vehicle: ${vehicleModel.ifBlank { "Vehicle" }}")
                            appendLine("License Plate: ${plateNumber.ifBlank { "Not specified" }}")
                            appendLine("Driver: ${driverName.ifBlank { "Not specified" }}")
                            appendLine("Pickup Location: ${locationState.locationName}")
                            appendLine("Live GPS Link: ${locationState.mapsUrl}")
                        }
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Ride Safety Details"))
                    },
                    modifier = Modifier.fillMaxWidth().testTag("share_ride_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("1-Tap Share Ride with Trusted Contacts", fontWeight = FontWeight.Bold)
                }

                // In-Ride Safety Timer Shortcut
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ride Duration Safety Check-in", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Set an automated check-in timer. If not cancelled on arrival, your contacts will be alerted.", fontSize = 11.sp, color = Slate500)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onStartSafetyTimer(15, "$service Ride (15m)")
                                    Toast.makeText(context, "15m Ride Safety Timer Started", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("15 min", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    onStartSafetyTimer(25, "$service Ride (25m)")
                                    Toast.makeText(context, "25m Ride Safety Timer Started", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("25 min", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    onStartSafetyTimer(40, "$service Ride (40m)")
                                    Toast.makeText(context, "40m Ride Safety Timer Started", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("40 min", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ChecklistItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = SafeGreen)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate500
            )
        }
    }
}
