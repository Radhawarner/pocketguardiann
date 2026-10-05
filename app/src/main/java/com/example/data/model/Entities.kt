package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trusted_contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val relationship: String, // "Parent", "Partner", "Sibling", "Friend", "Emergency Contact"
    val priority: Int = 1, // 1 = Primary, 2 = Secondary
    val notifyEnabled: Boolean = true,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "SOS", "Guardian Timeout", "Manual Alert", "Duress Alert", "Safety Timer Expired"
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val locationName: String? = null,
    val status: String = "ALERT_SENT", // "ALERTING", "ALERT_SENT", "RESOLVED", "CANCELLED", "DURESS_TRIGGERED"
    val contactsNotifiedCount: Int = 0,
    val timelineJson: String = "[]", // JSON serialized timeline steps
    val aiSummary: String? = null,
    val resolvedTimestamp: Long? = null,
    val notes: String = "",
    val hasAudioEvidence: Boolean = false
)

@Entity(tableName = "guardian_sessions")
data class GuardianSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val destination: String,
    val expectedArrivalEpoch: Long,
    val checkInIntervalMinutes: Int = 20,
    val status: String = "ACTIVE", // "ACTIVE", "CHECK_IN_PENDING", "ESCALATING", "COMPLETED", "CANCELLED"
    val startedAt: Long = System.currentTimeMillis(),
    val lastCheckInAt: Long = System.currentTimeMillis(),
    val missedCheckIns: Int = 0,
    val contactName: String = "All Trusted Contacts"
)

@Entity(tableName = "safe_zones")
data class SafeZoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "Home", "Campus", "Work", "Police Station", "Hospital"
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int = 200,
    val autoNotifyOnArrival: Boolean = true
)

@Entity(tableName = "audio_evidence")
data class AudioEvidenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val filePath: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    val incidentId: Long? = null
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val displayName: String = "Alex Morgan",
    val phone: String = "+1 (555) 234-5678",
    val emergencyNumber: String = "911",
    val emergencyMessageTemplate: String = "EMERGENCY: I need help! My Pocket Guardian safety alert was triggered. Here is my current location:",
    val bloodType: String = "O+",
    val allergies: String = "Penicillin (Moderate)",
    val medications: String = "Inhaler (as needed)",
    val chronicConditions: String = "Mild Asthma",
    val physicianName: String = "Dr. Robert Vance, MD",
    val physicianPhone: String = "+1 (555) 888-2345",
    val isOrganDonor: Boolean = true,
    val emergencyNotes: String = "ICE: In Case of Emergency contact Sarah Morgan (+1 555 345-6789)",
    val demoMode: Boolean = true,
    val safetyPin: String = "1234",
    val duressPin: String = "9999" // Silent duress code
)

data class SafeHavenItem(
    val id: String,
    val name: String,
    val category: String, // "Police", "Trauma ER", "24/7 Pharmacy", "Fire Station", "Transit Security"
    val address: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMiles: Float = 0.0f,
    val isOpen24Hours: Boolean = true,
    val securityLevel: String = "24/7 Guarded & Monitored"
)

data class EmergencyPhrase(
    val english: String,
    val language: String,
    val translation: String,
    val phonetic: String
)

data class RideshareSafeCheck(
    val plateNumber: String = "",
    val driverName: String = "",
    val vehicleModelColor: String = "",
    val rideService: String = "Uber / Lyft",
    val destination: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false,
    val isShared: Boolean = false
)

enum class DeterrentSoundType(val title: String, val description: String) {
    POLICE_SIREN("Emergency Siren", "120dB alternating acoustic alarm"),
    POLICE_WHISTLE("High-Pitch Whistle", "Piercing 3-burst deterrent blast"),
    SCREAMER("Ultrasonic Deterrent", "High-frequency disorienting squeal"),
    SOS_MORSE("Morse SOS Beacon", "International acoustic distress code")
}

data class TimelineItem(
    val title: String,
    val description: String,
    val timestamp: Long,
    val isAlert: Boolean = false
)

enum class RiskLevel {
    LOW,
    ATTENTION,
    HIGH
}

data class SafetyScoreResult(
    val level: RiskLevel,
    val score: Int, // 0 - 100
    val explanation: String,
    val recommendations: List<String>
)

data class EmergencyResource(
    val name: String,
    val number: String,
    val description: String,
    val category: String, // "General Emergency", "Crisis Support", "Campus Safety", "Discreet Help"
    val isTextSupported: Boolean = false
)

data class SafetyTimerState(
    val isRunning: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val activityName: String = "Solo Commute",
    val note: String = ""
)
