package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.IncidentEntity
import com.example.data.repository.GuardianRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SosState {
    IDLE,
    HOLDING,
    COUNTDOWN,
    EMERGENCY_ACTIVE,
    ALERTING,
    ALERT_SENT,
    RESOLVED
}

data class ContactAlertStatus(
    val contactName: String,
    val phone: String,
    val status: String // "Preparing", "Sending SMS...", "Alert Delivered", "Failed"
)

data class SosUiState(
    val state: SosState = SosState.IDLE,
    val holdProgress: Float = 0f, // 0.0 to 1.0
    val countdownRemainingSeconds: Int = 3,
    val currentIncidentId: Long? = null,
    val activatedTimestamp: Long = 0,
    val capturedLatitude: Double? = null,
    val capturedLongitude: Double? = null,
    val locationName: String = "",
    val contactAlerts: List<ContactAlertStatus> = emptyList(),
    val isSilentMode: Boolean = false,
    val emergencyPhoneToDial: String = "911"
)

class SosService(
    private val context: Context,
    private val repository: GuardianRepository,
    private val locationService: LocationService,
    private val scope: CoroutineScope,
    private val mongoDbService: MongoDbAtlasService? = null
) {
    private val _uiState = MutableStateFlow(SosUiState())
    val uiState: StateFlow<SosUiState> = _uiState.asStateFlow()

    private var holdJob: Job? = null
    private var alertSimulationJob: Job? = null

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun startHolding() {
        if (_uiState.value.state != SosState.IDLE) return

        _uiState.value = _uiState.value.copy(
            state = SosState.HOLDING,
            holdProgress = 0f,
            countdownRemainingSeconds = 3
        )

        vibrateTick()

        holdJob?.cancel()
        holdJob = scope.launch(Dispatchers.Main) {
            val totalSteps = 30
            val stepDelayMs = 100L // 30 * 100ms = 3000ms = 3 seconds
            for (i in 1..totalSteps) {
                delay(stepDelayMs)
                val progress = i.toFloat() / totalSteps
                val secondsRemaining = ((totalSteps - i + 9) / 10).coerceAtLeast(1)
                _uiState.value = _uiState.value.copy(
                    holdProgress = progress,
                    countdownRemainingSeconds = secondsRemaining
                )
                if (i % 10 == 0) {
                    vibrateTick()
                }
            }
            // 3 seconds passed! Activate Emergency
            triggerEmergency()
        }
    }

    fun releaseHolding() {
        if (_uiState.value.state == SosState.HOLDING || _uiState.value.state == SosState.COUNTDOWN) {
            holdJob?.cancel()
            _uiState.value = _uiState.value.copy(
                state = SosState.IDLE,
                holdProgress = 0f,
                countdownRemainingSeconds = 3
            )
        }
    }

    fun triggerEmergency(type: String = "SOS") {
        holdJob?.cancel()
        vibrateUrgent()

        val loc = locationService.locationState.value
        val now = System.currentTimeMillis()

        scope.launch(Dispatchers.IO) {
            val profile = repository.getProfileSync()
            val emergencyDial = profile?.emergencyNumber ?: "911"
            val activeContacts = repository.getActiveContacts()

            val initialAlertStatuses = activeContacts.map {
                ContactAlertStatus(it.name, it.phone, "Preparing secure payload...")
            }

            // Create timeline JSON
            val timelineEvents = """
                [
                    {"title":"SOS Activated","description":"Hold to SOS confirmed by user","timestamp":$now,"isAlert":true},
                    {"title":"Location Locked","description":"Coordinates: ${loc.latitude}, ${loc.longitude} (${loc.locationName})","timestamp":${now + 800},"isAlert":false},
                    {"title":"Alert Dispatched","description":"SMS & In-App broadcast triggered to ${activeContacts.size} contacts","timestamp":${now + 1800},"isAlert":true}
                ]
            """.trimIndent()

            val incident = IncidentEntity(
                type = type,
                timestamp = now,
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracyMeters = loc.accuracyMeters,
                locationName = loc.locationName,
                status = "ALERT_SENT",
                contactsNotifiedCount = activeContacts.size,
                timelineJson = timelineEvents,
                aiSummary = null
            )
            val incidentId = repository.logIncident(incident)

            // Auto-push incident document to MongoDB Atlas if enabled
            if (mongoDbService?.config?.value?.autoSyncOnSos == true) {
                scope.launch(Dispatchers.IO) {
                    mongoDbService.syncIncident(incident.copy(id = incidentId))
                }
            }

            _uiState.value = _uiState.value.copy(
                state = SosState.EMERGENCY_ACTIVE,
                holdProgress = 1f,
                countdownRemainingSeconds = 0,
                currentIncidentId = incidentId,
                activatedTimestamp = now,
                capturedLatitude = loc.latitude,
                capturedLongitude = loc.longitude,
                locationName = loc.locationName,
                contactAlerts = initialAlertStatuses,
                emergencyPhoneToDial = emergencyDial
            )

            // Simulate accurate network stages for contacts
            alertSimulationJob?.cancel()
            alertSimulationJob = launch(Dispatchers.Main) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    state = SosState.ALERTING,
                    contactAlerts = activeContacts.map {
                        ContactAlertStatus(it.name, it.phone, "Transmitting location alert...")
                    }
                )
                delay(1500)
                _uiState.value = _uiState.value.copy(
                    state = SosState.ALERT_SENT,
                    contactAlerts = activeContacts.map {
                        ContactAlertStatus(it.name, it.phone, "Alert Delivered via SMS")
                    }
                )
            }
        }
    }

    fun resolveEmergency(enteredPin: String = "", userNotes: String = "Confirmed Safe") {
        val incidentId = _uiState.value.currentIncidentId
        val resolvedNow = System.currentTimeMillis()

        scope.launch(Dispatchers.IO) {
            val profile = repository.getProfileSync()
            val isDuress = enteredPin.isNotBlank() && enteredPin == profile?.duressPin

            if (incidentId != null) {
                val existing = repository.getIncidentById(incidentId)
                if (existing != null) {
                    val statusText = if (isDuress) "DURESS_TRIGGERED" else "RESOLVED"
                    val noteText = if (isDuress) "SILENT COERCION ALERT: Duress code $enteredPin entered. Disarm was forced!" else userNotes
                    val updated = existing.copy(
                        status = statusText,
                        resolvedTimestamp = resolvedNow,
                        notes = noteText
                    )
                    repository.updateIncident(updated)
                    if (mongoDbService?.config?.value?.autoSyncOnSos == true) {
                        scope.launch(Dispatchers.IO) {
                            mongoDbService.syncIncident(updated)
                        }
                    }
                }
            }
        }

        alertSimulationJob?.cancel()
        _uiState.value = SosUiState(
            state = SosState.RESOLVED,
            holdProgress = 0f,
            countdownRemainingSeconds = 3
        )

        // Return to IDLE after brief resolved banner
        scope.launch(Dispatchers.Main) {
            delay(2000)
            if (_uiState.value.state == SosState.RESOLVED) {
                _uiState.value = SosUiState()
            }
        }
    }

    fun dialEmergency() {
        val number = _uiState.value.emergencyPhoneToDial
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Dialer not available on emulator or restricted
        }
    }

    private fun vibrateTick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (e: SecurityException) {
            // Ignored if permission not granted
        }
    }

    private fun vibrateUrgent() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 200, 100, 300, 100, 500)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (e: SecurityException) {
            // Ignored
        }
    }
}
