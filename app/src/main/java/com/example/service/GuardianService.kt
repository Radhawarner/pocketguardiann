package com.example.service

import com.example.data.model.GuardianSessionEntity
import com.example.data.repository.GuardianRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GuardianUiState(
    val hasActiveJourney: Boolean = false,
    val destination: String = "",
    val expectedArrivalEpoch: Long = 0,
    val checkInIntervalMinutes: Int = 20,
    val secondsUntilNextCheckIn: Int = 1200, // 20 mins default
    val isCheckInPromptVisible: Boolean = false,
    val checkInGraceRemainingSeconds: Int = 60,
    val missedCheckInsCount: Int = 0,
    val isEscalating: Boolean = false,
    val currentSessionId: Long? = null
)

class GuardianService(
    private val repository: GuardianRepository,
    private val sosService: SosService,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(GuardianUiState())
    val uiState: StateFlow<GuardianUiState> = _uiState.asStateFlow()

    private var journeyTimerJob: Job? = null
    private var graceTimerJob: Job? = null

    init {
        // Observe repository for active session
        scope.launch(Dispatchers.IO) {
            repository.activeGuardianSession.collect { session ->
                if (session != null) {
                    _uiState.value = _uiState.value.copy(
                        hasActiveJourney = true,
                        destination = session.destination,
                        expectedArrivalEpoch = session.expectedArrivalEpoch,
                        checkInIntervalMinutes = session.checkInIntervalMinutes,
                        missedCheckInsCount = session.missedCheckIns,
                        currentSessionId = session.id
                    )
                    startTimer(session.checkInIntervalMinutes)
                } else {
                    _uiState.value = GuardianUiState()
                    journeyTimerJob?.cancel()
                    graceTimerJob?.cancel()
                }
            }
        }
    }

    fun startJourney(destination: String, arrivalMinutes: Int, checkInIntervalMinutes: Int) {
        val now = System.currentTimeMillis()
        val expectedEpoch = now + (arrivalMinutes * 60 * 1000L)

        scope.launch(Dispatchers.IO) {
            val session = GuardianSessionEntity(
                destination = destination,
                expectedArrivalEpoch = expectedEpoch,
                checkInIntervalMinutes = checkInIntervalMinutes,
                status = "ACTIVE",
                startedAt = now,
                lastCheckInAt = now,
                missedCheckIns = 0
            )
            val sessionId = repository.startGuardianSession(session)
            _uiState.value = _uiState.value.copy(
                hasActiveJourney = true,
                destination = destination,
                expectedArrivalEpoch = expectedEpoch,
                checkInIntervalMinutes = checkInIntervalMinutes,
                secondsUntilNextCheckIn = checkInIntervalMinutes * 60,
                isCheckInPromptVisible = false,
                currentSessionId = sessionId
            )
            startTimer(checkInIntervalMinutes)
        }
    }

    private fun startTimer(intervalMinutes: Int) {
        journeyTimerJob?.cancel()
        journeyTimerJob = scope.launch(Dispatchers.Main) {
            var remaining = intervalMinutes * 60
            while (remaining > 0) {
                _uiState.value = _uiState.value.copy(secondsUntilNextCheckIn = remaining)
                delay(1000L)
                remaining--
            }
            triggerCheckInPrompt()
        }
    }

    fun triggerCheckInPrompt() {
        _uiState.value = _uiState.value.copy(
            isCheckInPromptVisible = true,
            checkInGraceRemainingSeconds = 60,
            isEscalating = false
        )

        graceTimerJob?.cancel()
        graceTimerJob = scope.launch(Dispatchers.Main) {
            var grace = 60
            while (grace > 0) {
                delay(1000L)
                grace--
                _uiState.value = _uiState.value.copy(checkInGraceRemainingSeconds = grace)
            }
            // Grace expired without confirmation!
            escalateMissedCheckIn()
        }
    }

    fun confirmSafe() {
        graceTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isCheckInPromptVisible = false,
            isEscalating = false,
            checkInGraceRemainingSeconds = 60
        )

        // Reset check-in countdown
        val interval = _uiState.value.checkInIntervalMinutes
        startTimer(interval)

        // Update database session
        val sessionId = _uiState.value.currentSessionId
        if (sessionId != null) {
            scope.launch(Dispatchers.IO) {
                repository.updateGuardianSession(
                    GuardianSessionEntity(
                        id = sessionId,
                        destination = _uiState.value.destination,
                        expectedArrivalEpoch = _uiState.value.expectedArrivalEpoch,
                        checkInIntervalMinutes = _uiState.value.checkInIntervalMinutes,
                        status = "ACTIVE",
                        lastCheckInAt = System.currentTimeMillis(),
                        missedCheckIns = 0
                    )
                )
            }
        }
    }

    fun requestHelpFromPrompt() {
        graceTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isCheckInPromptVisible = false,
            isEscalating = true
        )
        // Trigger emergency SOS
        sosService.triggerEmergency(type = "Guardian Alert")
    }

    private fun escalateMissedCheckIn() {
        val newMissed = _uiState.value.missedCheckInsCount + 1
        _uiState.value = _uiState.value.copy(
            isEscalating = true,
            missedCheckInsCount = newMissed
        )

        val sessionId = _uiState.value.currentSessionId
        if (sessionId != null) {
            scope.launch(Dispatchers.IO) {
                repository.updateGuardianSession(
                    GuardianSessionEntity(
                        id = sessionId,
                        destination = _uiState.value.destination,
                        expectedArrivalEpoch = _uiState.value.expectedArrivalEpoch,
                        checkInIntervalMinutes = _uiState.value.checkInIntervalMinutes,
                        status = "ESCALATING",
                        lastCheckInAt = System.currentTimeMillis(),
                        missedCheckIns = newMissed
                    )
                )
            }
        }

        // If multiple check-ins missed, escalate to SOS
        if (newMissed >= 2) {
            sosService.triggerEmergency(type = "Guardian Timeout")
        }
    }

    fun completeJourney() {
        journeyTimerJob?.cancel()
        graceTimerJob?.cancel()

        val sessionId = _uiState.value.currentSessionId
        if (sessionId != null) {
            scope.launch(Dispatchers.IO) {
                repository.updateGuardianSession(
                    GuardianSessionEntity(
                        id = sessionId,
                        destination = _uiState.value.destination,
                        expectedArrivalEpoch = _uiState.value.expectedArrivalEpoch,
                        checkInIntervalMinutes = _uiState.value.checkInIntervalMinutes,
                        status = "COMPLETED",
                        lastCheckInAt = System.currentTimeMillis(),
                        missedCheckIns = _uiState.value.missedCheckInsCount
                    )
                )
            }
        }

        _uiState.value = GuardianUiState()
    }
}
