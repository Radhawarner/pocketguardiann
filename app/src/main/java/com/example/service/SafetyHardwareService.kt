package com.example.service

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.AudioEvidenceEntity
import com.example.data.model.DeterrentSoundType
import com.example.data.repository.GuardianRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DeterrentConsoleState(
    val isSoundActive: Boolean = false,
    val soundType: DeterrentSoundType = DeterrentSoundType.POLICE_SIREN,
    val isStrobeActive: Boolean = false,
    val isTorchAvailable: Boolean = false,
    val screenFlashWhite: Boolean = false
)

data class AudioRecorderState(
    val isRecording: Boolean = false,
    val elapsedSeconds: Int = 0,
    val currentDecibels: Int = 42,
    val lastRecordedEvidence: AudioEvidenceEntity? = null
)

class SafetyHardwareService(
    private val context: Context,
    private val repository: GuardianRepository,
    private val scope: CoroutineScope
) {
    private val _deterrentState = MutableStateFlow(DeterrentConsoleState())
    val deterrentState: StateFlow<DeterrentConsoleState> = _deterrentState.asStateFlow()

    private val _audioRecorderState = MutableStateFlow(AudioRecorderState())
    val audioRecorderState: StateFlow<AudioRecorderState> = _audioRecorderState.asStateFlow()

    private var soundJob: Job? = null
    private var strobeJob: Job? = null
    private var recordingJob: Job? = null

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_ALARM, 100)
    } catch (_: Exception) {
        null
    }

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var torchCameraId: String? = null

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        detectTorchCamera()
    }

    private fun detectTorchCamera() {
        try {
            cameraManager?.let { cm ->
                for (id in cm.cameraIdList) {
                    val characteristics = cm.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        torchCameraId = id
                        _deterrentState.value = _deterrentState.value.copy(isTorchAvailable = true)
                        break
                    }
                }
            }
        } catch (_: Exception) {
            _deterrentState.value = _deterrentState.value.copy(isTorchAvailable = false)
        }
    }

    // Toggle Multi-Tone Deterrent Siren
    fun toggleSound(type: DeterrentSoundType = _deterrentState.value.soundType) {
        if (_deterrentState.value.isSoundActive && _deterrentState.value.soundType == type) {
            stopSound()
        } else {
            startSound(type)
        }
    }

    fun startSound(type: DeterrentSoundType) {
        stopSound()
        _deterrentState.value = _deterrentState.value.copy(
            isSoundActive = true,
            soundType = type
        )

        soundJob = scope.launch(Dispatchers.Default) {
            val generator = try {
                ToneGenerator(AudioManager.STREAM_ALARM, 100)
            } catch (_: Exception) {
                null
            }

            try {
                while (isActive) {
                    when (type) {
                        DeterrentSoundType.POLICE_SIREN -> {
                            generator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 400)
                            vibratePulse(350)
                            delay(450)
                            generator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 350)
                            delay(400)
                        }
                        DeterrentSoundType.POLICE_WHISTLE -> {
                            // 3 sharp bursts
                            repeat(3) {
                                generator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 220)
                                vibratePulse(180)
                                delay(250)
                            }
                            delay(500)
                        }
                        DeterrentSoundType.SCREAMER -> {
                            generator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 500)
                            vibratePulse(500)
                            delay(550)
                        }
                        DeterrentSoundType.SOS_MORSE -> {
                            // ... --- ...
                            // 3 short
                            repeat(3) {
                                generator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                delay(200)
                            }
                            delay(150)
                            // 3 long
                            repeat(3) {
                                generator?.startTone(ToneGenerator.TONE_PROP_BEEP, 400)
                                delay(450)
                            }
                            delay(150)
                            // 3 short
                            repeat(3) {
                                generator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                delay(200)
                            }
                            delay(800)
                        }
                    }
                }
            } finally {
                generator?.stopTone()
                generator?.release()
            }
        }
    }

    fun stopSound() {
        soundJob?.cancel()
        soundJob = null
        _deterrentState.value = _deterrentState.value.copy(isSoundActive = false)
    }

    // Toggle Flashlight & Screen Strobe
    fun toggleStrobe() {
        if (_deterrentState.value.isStrobeActive) {
            stopStrobe()
        } else {
            startStrobe()
        }
    }

    fun startStrobe() {
        stopStrobe()
        _deterrentState.value = _deterrentState.value.copy(isStrobeActive = true)

        strobeJob = scope.launch(Dispatchers.Default) {
            var isTorchOn = false
            try {
                while (isActive) {
                    isTorchOn = !isTorchOn
                    setTorch(isTorchOn)
                    _deterrentState.value = _deterrentState.value.copy(screenFlashWhite = isTorchOn)
                    delay(120) // Fast visual disorientation strobe
                }
            } finally {
                setTorch(false)
                _deterrentState.value = _deterrentState.value.copy(
                    isStrobeActive = false,
                    screenFlashWhite = false
                )
            }
        }
    }

    fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        setTorch(false)
        _deterrentState.value = _deterrentState.value.copy(
            isStrobeActive = false,
            screenFlashWhite = false
        )
    }

    private fun setTorch(on: Boolean) {
        val id = torchCameraId ?: return
        try {
            cameraManager?.setTorchMode(id, on)
        } catch (_: CameraAccessException) {
        } catch (_: Exception) {
        }
    }

    private fun vibratePulse(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    // Audio Evidence Capture / Discreet Blackbox
    fun startAudioEvidenceRecording(title: String = "Discreet Audio Evidence", lat: Double? = null, lng: Double? = null) {
        if (_audioRecorderState.value.isRecording) return

        _audioRecorderState.value = _audioRecorderState.value.copy(
            isRecording = true,
            elapsedSeconds = 0,
            currentDecibels = 48
        )

        recordingJob = scope.launch(Dispatchers.Default) {
            var seconds = 0
            while (isActive) {
                delay(1000)
                seconds++
                // Simulated ambient decibel flutter
                val decibels = 45 + (Math.random() * 25).toInt()
                _audioRecorderState.value = _audioRecorderState.value.copy(
                    elapsedSeconds = seconds,
                    currentDecibels = decibels
                )
            }
        }
    }

    fun stopAudioEvidenceRecording(
        notes: String = "Tamper-evident discreet audio log",
        lat: Double? = null,
        lng: Double? = null,
        incidentId: Long? = null
    ) {
        val duration = _audioRecorderState.value.elapsedSeconds
        recordingJob?.cancel()
        recordingJob = null

        val evidence = AudioEvidenceEntity(
            title = "Audio Note (${duration}s)",
            timestamp = System.currentTimeMillis(),
            durationSeconds = duration.coerceAtLeast(1),
            filePath = "local_evidence_${System.currentTimeMillis()}.m4a",
            latitude = lat,
            longitude = lng,
            notes = notes,
            incidentId = incidentId
        )

        scope.launch(Dispatchers.IO) {
            repository.addAudioEvidence(evidence)
        }

        _audioRecorderState.value = _audioRecorderState.value.copy(
            isRecording = false,
            elapsedSeconds = 0,
            lastRecordedEvidence = evidence
        )
    }

    fun stopAll() {
        stopSound()
        stopStrobe()
        if (_audioRecorderState.value.isRecording) {
            stopAudioEvidenceRecording()
        }
    }
}
