package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.SafetyScoreResult
import com.example.data.model.SafetyTimerState
import com.example.data.model.UserProfileEntity
import com.example.service.GuardianUiState
import com.example.service.LocationState
import com.example.service.SosState
import com.example.service.SosUiState
import com.example.ui.components.HoldToSosButton
import com.example.ui.components.LocationCard
import com.example.ui.components.SafetyStatusCard
import com.example.ui.theme.EmergencyContainer
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedLight
import com.example.ui.theme.OnEmergencyContainer
import com.example.ui.theme.OnSafeContainer
import com.example.ui.theme.OnWarningContainer
import com.example.ui.theme.SafeContainer
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    sosState: SosUiState,
    locationState: LocationState,
    guardianState: GuardianUiState,
    safetyTimerState: SafetyTimerState,
    contacts: List<ContactEntity>,
    lastIncident: IncidentEntity?,
    userProfile: UserProfileEntity?,
    safetyScore: SafetyScoreResult,
    onStartSosHold: () -> Unit,
    onReleaseSosHold: () -> Unit,
    onRefreshLocation: () -> Unit,
    onNavigateToGuardian: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToIncidents: () -> Unit,
    onTriggerFakeCall: () -> Unit,
    onTriggerSiren: () -> Unit,
    onTriggerStealthCalculator: () -> Unit,
    onStartSafetyTimer: (minutes: Int, activity: String) -> Unit,
    onExtendSafetyTimer: () -> Unit,
    onCancelSafetyTimer: () -> Unit,
    onToggleDemoMode: () -> Unit,
    onOpenSafeHavens: () -> Unit = {},
    onOpenMedicalId: () -> Unit = {},
    onOpenRideshare: () -> Unit = {},
    onOpenDeterrentConsole: () -> Unit = {},
    onOpenMongoDb: () -> Unit = {},
    onSyncMongoDb: () -> Unit = {},
    mongoDbSyncStatus: com.example.service.MongoDbSyncStatus? = null,
    nearestHavenName: String = "Downtown Police Precinct & Safe Haven",
    nearestHavenDistance: Float = 0.4f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Demo Mode / Active alert banner if enabled
        if (userProfile?.demoMode == true) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDemoMode() }
                    .testTag("demo_mode_banner"),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "D",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Demo Mode Active (Simulated Alerts & GPS)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Toggle",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Safety Status & Risk Indicator
        SafetyStatusCard(
            safetyScore = safetyScore,
            onClickExplain = { /* open detail */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large 3-second Circular Hold To SOS Button
        HoldToSosButton(
            progress = sosState.holdProgress,
            isHolding = sosState.state == SosState.HOLDING,
            countdownSeconds = sosState.countdownRemainingSeconds,
            onStartHold = onStartSosHold,
            onReleaseHold = onReleaseSosHold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Safety Tools Row (Fake Call, Siren, Stealth Mode, Share Location)
        Text(
            text = "QUICK SAFETY TOOLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Slate500,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickToolCard(
                icon = Icons.Default.PhoneInTalk,
                title = "Fake Call",
                subtitle = "Exit tool",
                onClick = onTriggerFakeCall,
                modifier = Modifier.weight(1f).testTag("quick_fake_call_btn")
            )
            QuickToolCard(
                icon = Icons.Default.Calculate,
                title = "Stealth",
                subtitle = "Disguise",
                onClick = onTriggerStealthCalculator,
                modifier = Modifier.weight(1f).testTag("quick_stealth_btn")
            )
            QuickToolCard(
                icon = Icons.Default.Campaign,
                title = "Siren & Strobe",
                subtitle = "Deter threat",
                onClick = onOpenDeterrentConsole,
                modifier = Modifier.weight(1f).testTag("quick_siren_btn")
            )
            QuickToolCard(
                icon = Icons.Default.Share,
                title = "GPS Share",
                subtitle = "Send link",
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, "Pocket Guardian Live Location: ${locationState.mapsUrl}")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Location")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.weight(1f).testTag("quick_share_gps_btn")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Standalone Safety Check-in Timer Card
        SafetyTimerModule(
            timerState = safetyTimerState,
            onStartTimer = onStartSafetyTimer,
            onExtend = onExtendSafetyTimer,
            onCancel = onCancelSafetyTimer
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Current Location Status Card
        LocationCard(
            locationState = locationState,
            onRefreshLocation = onRefreshLocation
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Safety Radar & Nearby Safe Havens Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSafeHavens() }
                .testTag("safe_havens_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = "Safe Havens",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Safety Radar & Havens",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = SafeContainer) {
                                Text(
                                    text = "$nearestHavenDistance MI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SafeGreen,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Closest: $nearestHavenName",
                            fontSize = 12.sp,
                            color = Slate500,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "VIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Rideshare SafeCheck Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenRideshare() }
                .testTag("rideshare_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Rideshare",
                                tint = WarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Rideshare & Commute SafeCheck",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Verify vehicle plate & 1-tap broadcast to circle",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = "CHECK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Emergency Medical ID & Travel ICE Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenMedicalId() }
                .testTag("medical_ice_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = "Medical ID",
                                tint = EmergencyRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Emergency Medical Card (ICE)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEE2E2)) {
                                Text(
                                    text = userProfile?.bloodType ?: "O+",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmergencyRed,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "First responder info & 6-language phrasebook",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEE2E2)
                ) {
                    Text(
                        text = "ICE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmergencyRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. MongoDB Atlas Cloud Sync Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenMongoDb() }
                .testTag("mongodb_home_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFF00684A).copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "MongoDB Atlas",
                                tint = Color(0xFF00684A),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MongoDB Atlas Cloud",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE8F5E9)) {
                                Text(
                                    text = if (mongoDbSyncStatus?.isConnected == true) "CONNECTED" else "OFFLINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00684A),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${mongoDbSyncStatus?.totalDocumentsSynced ?: 14} BSON documents • Auto SOS push",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF00684A),
                        modifier = Modifier
                            .clickable { onSyncMongoDb() }
                            .padding(end = 4.dp)
                    ) {
                        Text(
                            text = "SYNC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "EXPLORE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00684A),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Guardian Mode Status Snippet
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToGuardian() }
                .testTag("guardian_snippet_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (guardianState.hasActiveJourney) SafeContainer else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                contentDescription = "Guardian Mode",
                                tint = if (guardianState.hasActiveJourney) OnSafeContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (guardianState.hasActiveJourney) "Active Journey" else "Guardian Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (guardianState.hasActiveJourney)
                                "Heading to: ${guardianState.destination} (${guardianState.secondsUntilNextCheckIn / 60}m to check-in)"
                            else
                                "Auto check-ins during solo travel or commutes",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (guardianState.hasActiveJourney) SafeContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (guardianState.hasActiveJourney) "ACTIVE" else "START",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (guardianState.hasActiveJourney) OnSafeContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Trusted Contacts Summary Snippet
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToContacts() }
                .testTag("contacts_snippet_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Contacts",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Trusted Circle (${contacts.size} Active)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (contacts.isEmpty()) "No emergency contacts configured" else "${contacts.firstOrNull()?.name ?: ""} + ${contacts.size - 1} more ready",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }

                Text(
                    text = "Manage",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SafetyTimerModule(
    timerState: SafetyTimerState,
    onStartTimer: (minutes: Int, activity: String) -> Unit,
    onExtend: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("safety_timer_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (timerState.isRunning) WarningAmber else MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (timerState.isRunning) WarningContainer else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = if (timerState.isRunning) WarningAmber else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (timerState.isRunning) "Safety Timer Running" else "Safety Check-in Timer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (timerState.isRunning) timerState.activityName else "Auto-alerts if you don't disarm before expiry",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }

                if (timerState.isRunning) {
                    val mins = timerState.remainingSeconds / 60
                    val secs = timerState.remainingSeconds % 60
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", mins, secs),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = WarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!timerState.isRunning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onStartTimer(15, "15m Solo Jog") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("15m Run", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { onStartTimer(30, "30m Meeting New Person") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("30m Meet", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { onStartTimer(45, "45m Late Commute") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("45m Walk", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExtend,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+10 Min", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("I'm Safe", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickToolCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Slate500
            )
        }
    }
}
