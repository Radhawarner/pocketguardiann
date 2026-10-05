package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.SosState
import com.example.ui.PocketGuardianViewModel
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedLight
import com.example.ui.theme.SafeContainer
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    GUARDIAN("Guardian", Icons.AutoMirrored.Filled.DirectionsWalk),
    CONTACTS("Contacts", Icons.Default.Group),
    INCIDENTS("Incidents", Icons.Default.History),
    AI("AI", Icons.Default.AutoAwesome),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: PocketGuardianViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }

    val sosState by viewModel.sosUiState.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val guardianState by viewModel.guardianUiState.collectAsStateWithLifecycle()
    val safetyTimerState by viewModel.safetyTimerState.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val incidents by viewModel.incidents.collectAsStateWithLifecycle()
    val safeZones by viewModel.safeZones.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val safetyScore by viewModel.safetyScore.collectAsStateWithLifecycle()
    val quickTools by viewModel.quickTools.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val generatedEmergencyMessage by viewModel.generatedEmergencyMessage.collectAsStateWithLifecycle()
    val safeHavens by viewModel.safeHavens.collectAsStateWithLifecycle()
    val audioEvidences by viewModel.audioEvidences.collectAsStateWithLifecycle()
    val deterrentState by viewModel.deterrentState.collectAsStateWithLifecycle()
    val audioRecorderState by viewModel.audioRecorderState.collectAsStateWithLifecycle()
    val rideshareState by viewModel.rideshareState.collectAsStateWithLifecycle()
    val mongoDbConfig by viewModel.mongoDbConfig.collectAsStateWithLifecycle()
    val mongoDbSyncStatus by viewModel.mongoDbSyncStatus.collectAsStateWithLifecycle()
    val selectedMongoCollection by viewModel.selectedMongoCollection.collectAsStateWithLifecycle()
    val mongoDocuments by viewModel.mongoDocuments.collectAsStateWithLifecycle()

    var showSafeHavensSheet by remember { mutableStateOf(false) }
    var showMedicalIdSheet by remember { mutableStateOf(false) }
    var showRideshareSheet by remember { mutableStateOf(false) }
    var showDeterrentSheet by remember { mutableStateOf(false) }
    var showMongoDbSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = EmergencyRed,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Logo",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "Pocket Guardian",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Your safety. One tap away.",
                                        fontSize = 10.sp,
                                        color = Slate500
                                    )
                                }
                            }
                        },
                        actions = {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEE2E2),
                                modifier = Modifier
                                    .clickable { showMedicalIdSheet = true }
                                    .padding(end = 6.dp)
                                    .testTag("top_bar_ice_btn")
                            ) {
                                Text(
                                    text = "ICE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmergencyRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SafeContainer,
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(SafeGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ARMED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SafeGreen
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                }
            },
            bottomBar = {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 4.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        NavigationTab.values().forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = Slate500,
                                    unselectedTextColor = Slate600,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    NavigationTab.HOME -> HomeScreen(
                        sosState = sosState,
                        locationState = locationState,
                        guardianState = guardianState,
                        safetyTimerState = safetyTimerState,
                        contacts = contacts,
                        lastIncident = incidents.firstOrNull(),
                        userProfile = userProfile,
                        safetyScore = safetyScore,
                        onStartSosHold = { viewModel.sosService.startHolding() },
                        onReleaseSosHold = { viewModel.sosService.releaseHolding() },
                        onRefreshLocation = { viewModel.locationService.requestLocationUpdate() },
                        onNavigateToGuardian = { selectedTab = NavigationTab.GUARDIAN },
                        onNavigateToContacts = { selectedTab = NavigationTab.CONTACTS },
                        onNavigateToIncidents = { selectedTab = NavigationTab.INCIDENTS },
                        onTriggerFakeCall = { viewModel.toggleFakeCall() },
                        onTriggerSiren = { viewModel.toggleSiren() },
                        onTriggerStealthCalculator = { viewModel.toggleStealthCalculator() },
                        onStartSafetyTimer = { mins, activity -> viewModel.startSafetyTimer(mins, activity) },
                        onExtendSafetyTimer = { viewModel.extendSafetyTimer(10) },
                        onCancelSafetyTimer = { viewModel.cancelSafetyTimer() },
                        onToggleDemoMode = { viewModel.toggleDemoMode() },
                        onOpenSafeHavens = { showSafeHavensSheet = true },
                        onOpenMedicalId = { showMedicalIdSheet = true },
                        onOpenRideshare = { showRideshareSheet = true },
                        onOpenDeterrentConsole = { showDeterrentSheet = true },
                        onOpenMongoDb = { showMongoDbSheet = true },
                        onSyncMongoDb = { viewModel.syncToMongoDb() },
                        mongoDbSyncStatus = mongoDbSyncStatus,
                        nearestHavenName = safeHavens.firstOrNull()?.name ?: "Downtown Police Precinct & Safe Haven",
                        nearestHavenDistance = safeHavens.firstOrNull()?.distanceMiles ?: 0.4f
                    )

                    NavigationTab.GUARDIAN -> GuardianScreen(
                        guardianState = guardianState,
                        allSessions = emptyList(),
                        safeZones = safeZones,
                        onStartJourney = { dest, arrival, interval ->
                            viewModel.guardianService.startJourney(dest, arrival, interval)
                        },
                        onConfirmSafe = { viewModel.guardianService.confirmSafe() },
                        onRequestHelp = { viewModel.guardianService.requestHelpFromPrompt() },
                        onCompleteJourney = { viewModel.guardianService.completeJourney() },
                        onSimulateCheckInPrompt = { viewModel.guardianService.triggerCheckInPrompt() },
                        onAddSafeZone = { name, cat, addr, lat, lng, radius ->
                            viewModel.addSafeZone(name, cat, addr, lat, lng, radius)
                        }
                    )

                    NavigationTab.CONTACTS -> ContactsScreen(
                        contacts = contacts,
                        onSaveContact = { id, name, phone, email, rel, priority, notify ->
                            viewModel.saveContact(id, name, phone, email, rel, priority, notify)
                        },
                        onDeleteContact = { viewModel.deleteContact(it) },
                        onToggleNotify = { viewModel.toggleContactNotify(it) }
                    )

                    NavigationTab.INCIDENTS -> IncidentsScreen(
                        incidents = incidents,
                        onGenerateAiSummary = { viewModel.generateIncidentSummary(it) },
                        onDeleteIncident = { viewModel.deleteIncident(it) },
                        onClearAll = { viewModel.clearAllIncidents() }
                    )

                    NavigationTab.AI -> AIAssistantScreen(
                        messages = chatMessages,
                        isThinking = isAiThinking,
                        generatedMessage = generatedEmergencyMessage,
                        emergencyResources = viewModel.emergencyResources,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onGenerateCustomMessage = { viewModel.generateCustomEmergencyMessage(it) }
                    )

                    NavigationTab.SETTINGS -> SettingsScreen(
                        userProfile = userProfile,
                        onUpdateProfile = { viewModel.updateProfile(it) },
                        onClearAllHistory = { viewModel.clearAllIncidents() },
                        onToggleDemoMode = { viewModel.toggleDemoMode() },
                        onOpenMongoDb = { showMongoDbSheet = true }
                    )
                }
            }
        }

        // Fullscreen Emergency Mode Active Overlay
        if (sosState.state == SosState.EMERGENCY_ACTIVE ||
            sosState.state == SosState.ALERTING ||
            sosState.state == SosState.ALERT_SENT
        ) {
            EmergencyOverlayScreen(
                sosState = sosState,
                onDialEmergency = { viewModel.sosService.dialEmergency() },
                onResolveEmergency = { pin, notes -> viewModel.sosService.resolveEmergency(pin, notes) }
            )
        }

        // Fullscreen Fake Call Overlay
        if (quickTools.isFakeCallActive) {
            FakeCallOverlay(
                callerName = "Mom (Mobile)",
                onDismiss = { viewModel.toggleFakeCall() }
            )
        }

        // Fullscreen Stealth Calculator Disguise
        if (quickTools.isStealthCalculatorActive) {
            StealthCalculatorOverlay(
                safetyPin = userProfile?.safetyPin ?: "1234",
                onTriggerDiscreetSos = {
                    viewModel.toggleStealthCalculator()
                    viewModel.sosService.triggerEmergency(type = "Stealth Disarm SOS")
                },
                onExitStealth = { viewModel.toggleStealthCalculator() }
            )
        }

        // Siren Modal Banner
        if (quickTools.isSirenActive) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = EmergencyRed
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Siren",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "LOUD SIREN & STROBE ACTIVE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "High-decibel acoustic deterrent",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleSiren() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("SILENCE", color = EmergencyRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Fullscreen Safe Havens & Radar Screen
        if (showSafeHavensSheet) {
            SafeHavensScreen(
                havens = safeHavens,
                locationState = locationState,
                onDismiss = { showSafeHavensSheet = false }
            )
        }

        // Fullscreen Medical ID & ICE Screen
        if (showMedicalIdSheet) {
            MedicalIdScreen(
                userProfile = userProfile,
                contacts = contacts,
                onDismiss = { showMedicalIdSheet = false }
            )
        }

        // Fullscreen Rideshare SafeCheck Screen
        if (showRideshareSheet) {
            RideshareSafeCheckScreen(
                rideshareState = rideshareState,
                locationState = locationState,
                onUpdateDetails = { plate, driver, vehicle, service, dest ->
                    viewModel.updateRideshareDetails(plate, driver, vehicle, service, dest)
                },
                onStartSafetyTimer = { mins, act ->
                    viewModel.startSafetyTimer(mins, act)
                },
                onDismiss = { showRideshareSheet = false }
            )
        }

        // Fullscreen Deterrent & Blackbox Screen
        if (showDeterrentSheet) {
            DeterrentConsoleScreen(
                deterrentState = deterrentState,
                audioRecorderState = audioRecorderState,
                audioEvidences = audioEvidences,
                onToggleSound = { viewModel.toggleDeterrentSound(it) },
                onToggleStrobe = { viewModel.toggleFlashlightStrobe() },
                onStopAll = { viewModel.stopAllDeterrents() },
                onStartAudioRecording = { viewModel.startAudioEvidence(it) },
                onStopAudioRecording = { viewModel.stopAudioEvidence(it) },
                onDeleteAudioEvidence = { viewModel.deleteAudioEvidence(it) },
                onDismiss = { showDeterrentSheet = false }
            )
        }

        // Fullscreen MongoDB Atlas Cloud Sync Screen
        if (showMongoDbSheet) {
            MongoDbSyncScreen(
                syncStatus = mongoDbSyncStatus,
                config = mongoDbConfig,
                selectedCollection = selectedMongoCollection ?: "incidents",
                documents = mongoDocuments,
                onSelectCollection = { viewModel.selectMongoCollection(it) },
                onInsertTestDocument = { col, sum, json -> viewModel.insertTestMongoDocument(col, sum, json) },
                onDeleteDocument = { col, id -> viewModel.deleteMongoDocument(col, id) },
                onSyncNow = { viewModel.syncToMongoDb() },
                onRestoreNow = { viewModel.syncToMongoDb() },
                onTestConnection = { viewModel.testMongoDbConnection() },
                onAutoProvisionKey = { viewModel.generateDemoAtlasCredentials() },
                onUpdateConfig = { viewModel.updateMongoDbConfig(it) },
                onDismiss = { showMongoDbSheet = false }
            )
        }
    }
}
