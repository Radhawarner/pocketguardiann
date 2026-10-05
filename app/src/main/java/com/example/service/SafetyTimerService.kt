package com.example.service

import com.example.data.model.SafetyTimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SafetyTimerService(
    private val sosService: SosService,
    private val scope: CoroutineScope
) {
    private val _timerState = MutableStateFlow(SafetyTimerState())
    val timerState: StateFlow<SafetyTimerState> = _timerState.asStateFlow()

    private var countdownJob: Job? = null

    fun startTimer(durationMinutes: Int, activityName: String = "Solo Commute", note: String = "") {
        countdownJob?.cancel()
        val totalSecs = durationMinutes * 60

        _timerState.value = SafetyTimerState(
            isRunning = true,
            remainingSeconds = totalSecs,
            totalSeconds = totalSecs,
            activityName = activityName,
            note = note
        )

        countdownJob = scope.launch(Dispatchers.Main) {
            var secs = totalSecs
            while (secs > 0) {
                delay(1000L)
                secs--
                _timerState.value = _timerState.value.copy(remainingSeconds = secs)
            }
            // Timer expired! Trigger safety alert
            triggerTimerExpiredAlert()
        }
    }

    fun extendTimer(extraMinutes: Int = 10) {
        if (!_timerState.value.isRunning) return
        val newRemaining = _timerState.value.remainingSeconds + (extraMinutes * 60)
        _timerState.value = _timerState.value.copy(
            remainingSeconds = newRemaining,
            totalSeconds = _timerState.value.totalSeconds + (extraMinutes * 60)
        )
    }

    fun cancelTimer() {
        countdownJob?.cancel()
        _timerState.value = SafetyTimerState(isRunning = false, remainingSeconds = 0)
    }

    private fun triggerTimerExpiredAlert() {
        _timerState.value = _timerState.value.copy(isRunning = false)
        sosService.triggerEmergency(type = "Safety Timer Expired")
    }
}
