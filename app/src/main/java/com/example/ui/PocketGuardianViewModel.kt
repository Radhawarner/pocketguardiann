package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PocketGuardianDatabase
import com.example.data.model.ContactEntity
import com.example.data.model.EmergencyDirectory
import com.example.data.model.EmergencyResource
import com.example.data.model.IncidentEntity
import com.example.data.model.SafeZoneEntity
import com.example.data.model.SafetyScoreResult
import com.example.data.model.SafetyTimerState
import com.example.data.model.UserProfileEntity
import com.example.data.repository.GuardianRepository
import com.example.service.AudioRecorderState
import com.example.service.DeterrentConsoleState
import com.example.service.GeminiSafetyService
import com.example.service.GuardianService
import com.example.service.GuardianUiState
import com.example.service.LocationService
import com.example.service.LocationState
import com.example.service.SafetyHardwareService
import com.example.service.SafetyTimerService
import com.example.service.SosService
import com.example.service.SosState
import com.example.service.SosUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuickToolsState(
    val isFakeCallActive: Boolean = false,
    val isSirenActive: Boolean = false,
    val isStealthCalculatorActive: Boolean = false
)

class PocketGuardianViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PocketGuardianDatabase.getDatabase(application, viewModelScope)
    val repository = GuardianRepository(
        database.contactDao(),
        database.incidentDao(),
        database.guardianDao(),
        database.safeZoneDao(),
        database.userProfileDao(),
        database.audioEvidenceDao()
    )

    val locationService = LocationService(application)
    val geminiSafetyService = GeminiSafetyService()
    val mongoDbService = com.example.service.MongoDbAtlasService()
    val sosService = SosService(application, repository, locationService, viewModelScope, mongoDbService)
    val guardianService = GuardianService(repository, sosService, viewModelScope)
    val safetyTimerService = SafetyTimerService(sosService, viewModelScope)
    val safetyHardwareService = SafetyHardwareService(application, repository, viewModelScope)

    // Exposed Flows from Services
    val sosUiState: StateFlow<SosUiState> = sosService.uiState
    val locationState: StateFlow<LocationState> = locationService.locationState
    val guardianUiState: StateFlow<GuardianUiState> = guardianService.uiState
    val safetyTimerState: StateFlow<SafetyTimerState> = safetyTimerService.timerState
    val deterrentState: StateFlow<DeterrentConsoleState> = safetyHardwareService.deterrentState
    val audioRecorderState: StateFlow<AudioRecorderState> = safetyHardwareService.audioRecorderState
    val mongoDbConfig = mongoDbService.config
    val mongoDbSyncStatus = mongoDbService.syncStatus

    // MongoDB Document Explorer State
    private val _selectedMongoCollection = MutableStateFlow<String?>("incidents")
    val selectedMongoCollection: StateFlow<String?> = _selectedMongoCollection.asStateFlow()

    private val _mongoDocuments = MutableStateFlow<List<com.example.service.MongoDocumentModel>>(
        mongoDbService.getDocumentsForCollection("incidents")
    )
    val mongoDocuments: StateFlow<List<com.example.service.MongoDocumentModel>> = _mongoDocuments.asStateFlow()

    // Exposed Flows from Repository
    val contacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incidents: StateFlow<List<IncidentEntity>> = repository.allIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val safeZones: StateFlow<List<SafeZoneEntity>> = repository.allSafeZones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val audioEvidences: StateFlow<List<com.example.data.model.AudioEvidenceEntity>> = repository.allAudioEvidence
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Verified Safe Havens sorted by current proximity
    val safeHavens: StateFlow<List<com.example.data.model.SafeHavenItem>> = locationState
        .combine(MutableStateFlow(Unit)) { loc, _ ->
            com.example.data.model.SafeHavensCatalog.calculateSortedHavens(loc.latitude, loc.longitude)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            com.example.data.model.SafeHavensCatalog.calculateSortedHavens(null, null)
        )

    // Rideshare Safety Check State
    private val _rideshareState = MutableStateFlow(com.example.data.model.RideshareSafeCheck())
    val rideshareState: StateFlow<com.example.data.model.RideshareSafeCheck> = _rideshareState.asStateFlow()

    val emergencyResources: List<EmergencyResource> = EmergencyDirectory.resources

    // Quick Tools (Fake Call, Siren, Stealth Calculator)
    private val _quickTools = MutableStateFlow(QuickToolsState())
    val quickTools: StateFlow<QuickToolsState> = _quickTools.asStateFlow()

    // AI Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "Hello, I am Guardian AI. I'm here to provide calm, practical personal safety advice, guide you through unfamiliar environments, or help prepare emergency broadcasts. How can I assist you right now?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Generated Emergency Message
    private val _generatedEmergencyMessage = MutableStateFlow("")
    val generatedEmergencyMessage: StateFlow<String> = _generatedEmergencyMessage.asStateFlow()

    // Calculated Safety Risk Score
    val safetyScore: StateFlow<SafetyScoreResult> = combine(
        guardianUiState,
        sosUiState,
        locationState
    ) { guardian, sos, location ->
        geminiSafetyService.calculateSafetyScore(
            missedCheckIns = guardian.missedCheckInsCount,
            hasActiveSos = sos.state == SosState.EMERGENCY_ACTIVE || sos.state == SosState.ALERTING || sos.state == SosState.ALERT_SENT,
            locationAvailable = location.isAvailable,
            journeyActive = guardian.hasActiveJourney
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        geminiSafetyService.calculateSafetyScore(0, false, true, false)
    )

    // Contact Operations
    fun saveContact(
        id: Long = 0,
        name: String,
        phone: String,
        email: String,
        relationship: String,
        priority: Int,
        notifyEnabled: Boolean
    ) {
        viewModelScope.launch {
            val entity = ContactEntity(
                id = id,
                name = name.trim(),
                phone = phone.trim(),
                email = email.trim(),
                relationship = relationship,
                priority = priority,
                notifyEnabled = notifyEnabled,
                isVerified = true
            )
            if (id == 0L) {
                repository.addContact(entity)
            } else {
                repository.updateContact(entity)
            }
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    fun toggleContactNotify(contact: ContactEntity) {
        viewModelScope.launch {
            repository.updateContact(contact.copy(notifyEnabled = !contact.notifyEnabled))
        }
    }

    // Incident Operations
    fun deleteIncident(incident: IncidentEntity) {
        viewModelScope.launch {
            repository.deleteIncident(incident)
        }
    }

    fun clearAllIncidents() {
        viewModelScope.launch {
            repository.clearAllIncidents()
        }
    }

    fun generateIncidentSummary(incident: IncidentEntity) {
        viewModelScope.launch {
            val durationMinutes = if (incident.resolvedTimestamp != null) {
                ((incident.resolvedTimestamp - incident.timestamp) / 60000).toInt().coerceAtLeast(1)
            } else null

            val timeStr = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                .format(java.util.Date(incident.timestamp))

            val summary = geminiSafetyService.generateIncidentSummary(
                incidentType = incident.type,
                timeFormatted = timeStr,
                location = incident.locationName ?: "GPS coordinates",
                contactsNotified = incident.contactsNotifiedCount,
                resolvedInMinutes = durationMinutes
            )

            repository.updateIncident(incident.copy(aiSummary = summary))
        }
    }

    // AI Chat Operations
    fun sendChatMessage(text: String) {
        if (text.isBlank() || _isAiThinking.value) return

        val userMsg = ChatMessage(isUser = true, text = text.trim())
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            val loc = locationState.value
            val contextNote = "Location: ${loc.locationName} (${loc.coordinatesDisplay}), SOS Status: ${sosUiState.value.state.name}"
            val responseText = geminiSafetyService.askGuardianAI(text, contextNote)

            _chatMessages.value = _chatMessages.value + ChatMessage(isUser = false, text = responseText)
            _isAiThinking.value = false
        }
    }

    fun generateCustomEmergencyMessage(situation: String) {
        viewModelScope.launch {
            val profile = userProfile.value
            val loc = locationState.value
            val msg = geminiSafetyService.generateEmergencyMessage(
                userName = profile?.displayName ?: "User",
                situation = situation,
                latitude = loc.latitude,
                longitude = loc.longitude,
                locationName = loc.locationName
            )
            _generatedEmergencyMessage.value = msg
        }
    }

    // Safe Zones
    fun addSafeZone(name: String, category: String, address: String, lat: Double, lng: Double, radius: Int) {
        viewModelScope.launch {
            repository.addSafeZone(
                SafeZoneEntity(
                    name = name.trim(),
                    category = category,
                    address = address.trim(),
                    latitude = lat,
                    longitude = lng,
                    radiusMeters = radius
                )
            )
        }
    }

    fun deleteSafeZone(zone: SafeZoneEntity) {
        viewModelScope.launch {
            repository.deleteSafeZone(zone)
        }
    }

    // Safety Timer
    fun startSafetyTimer(durationMinutes: Int, activityName: String) {
        safetyTimerService.startTimer(durationMinutes, activityName)
    }

    fun extendSafetyTimer(extraMinutes: Int = 10) {
        safetyTimerService.extendTimer(extraMinutes)
    }

    fun cancelSafetyTimer() {
        safetyTimerService.cancelTimer()
    }

    // Quick Tools
    fun toggleFakeCall() {
        _quickTools.value = _quickTools.value.copy(
            isFakeCallActive = !_quickTools.value.isFakeCallActive
        )
    }

    fun toggleSiren() {
        _quickTools.value = _quickTools.value.copy(
            isSirenActive = !_quickTools.value.isSirenActive
        )
    }

    fun toggleStealthCalculator() {
        _quickTools.value = _quickTools.value.copy(
            isStealthCalculatorActive = !_quickTools.value.isStealthCalculatorActive
        )
    }

    // Deterrent & Hardware Actions
    fun toggleDeterrentSound(type: com.example.data.model.DeterrentSoundType) {
        safetyHardwareService.toggleSound(type)
    }

    fun toggleFlashlightStrobe() {
        safetyHardwareService.toggleStrobe()
    }

    fun stopAllDeterrents() {
        safetyHardwareService.stopAll()
        if (_quickTools.value.isSirenActive) {
            _quickTools.value = _quickTools.value.copy(isSirenActive = false)
        }
    }

    // Audio Evidence Actions
    fun startAudioEvidence(title: String = "Discreet Audio Memo") {
        val loc = locationState.value
        safetyHardwareService.startAudioEvidenceRecording(title, loc.latitude, loc.longitude)
    }

    fun stopAudioEvidence(notes: String = "Tamper-evident record") {
        val loc = locationState.value
        val currentIncident = sosUiState.value.currentIncidentId
        safetyHardwareService.stopAudioEvidenceRecording(notes, loc.latitude, loc.longitude, currentIncident)
    }

    fun deleteAudioEvidence(evidence: com.example.data.model.AudioEvidenceEntity) {
        viewModelScope.launch {
            repository.deleteAudioEvidence(evidence)
        }
    }

    // Rideshare Safety Check
    fun updateRideshareDetails(
        plate: String,
        driver: String,
        vehicle: String,
        service: String,
        destination: String
    ) {
        _rideshareState.value = _rideshareState.value.copy(
            plateNumber = plate.trim(),
            driverName = driver.trim(),
            vehicleModelColor = vehicle.trim(),
            rideService = service,
            destination = destination.trim(),
            timestamp = System.currentTimeMillis()
        )
    }

    fun setRideshareVerified(verified: Boolean) {
        _rideshareState.value = _rideshareState.value.copy(isVerified = verified)
    }

    fun setRideshareShared(shared: Boolean) {
        _rideshareState.value = _rideshareState.value.copy(isShared = shared)
    }

    // Profile & Settings
    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun toggleDemoMode() {
        val current = userProfile.value ?: return
        viewModelScope.launch {
            repository.updateProfile(current.copy(demoMode = !current.demoMode))
        }
    }

    // MongoDB Atlas Cloud Actions
    fun syncToMongoDb() {
        viewModelScope.launch {
            val contactList = contacts.value
            val incidentList = incidents.value
            val safeZoneList = safeZones.value
            val profile = userProfile.value
            mongoDbService.syncAllData(
                contacts = contactList,
                incidents = incidentList,
                sessions = emptyList(),
                safeZones = safeZoneList,
                userProfile = profile
            )
            refreshCurrentMongoCollection()
        }
    }

    fun testMongoDbConnection() {
        viewModelScope.launch {
            mongoDbService.testConnection()
        }
    }

    fun updateMongoDbConfig(config: com.example.service.MongoDbConfig) {
        mongoDbService.updateConfig(config)
    }

    fun generateDemoAtlasCredentials() {
        mongoDbService.generateDemoAtlasCredentials()
        refreshCurrentMongoCollection()
    }

    fun selectMongoCollection(collectionName: String) {
        _selectedMongoCollection.value = collectionName
        _mongoDocuments.value = mongoDbService.getDocumentsForCollection(collectionName)
    }

    fun refreshCurrentMongoCollection() {
        val current = _selectedMongoCollection.value ?: "incidents"
        _mongoDocuments.value = mongoDbService.getDocumentsForCollection(current)
    }

    fun insertTestMongoDocument(collectionName: String, summary: String = "", customJson: String = "") {
        mongoDbService.insertCustomDocument(collectionName, summary, customJson)
        refreshCurrentMongoCollection()
    }

    fun deleteMongoDocument(collectionName: String, docId: String) {
        mongoDbService.deleteDocument(collectionName, docId)
        refreshCurrentMongoCollection()
    }
}
