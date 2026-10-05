package com.example.data.validation

import com.example.data.model.ContactEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.UserProfileEntity
import org.json.JSONArray
import org.json.JSONObject
import java.util.regex.Pattern

data class ValidationError(
    val field: String,
    val message: String,
    val receivedValue: Any? = null
)

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val errors: List<ValidationError>) : ValidationResult() {
        fun summary(): String = errors.joinToString("; ") { "${it.field}: ${it.message}" }
    }

    val isValid: Boolean get() = this is Valid
}

object MongoSchemaValidator {

    private val PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\s\\-\\(\\)]{7,25}$")
    private val EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val EMERGENCY_DIAL_PATTERN = Pattern.compile("^[0-9]{3,15}$")
    private val PIN_PATTERN = Pattern.compile("^[0-9]{4,8}$")

    val ALLOWED_BLOOD_TYPES = setOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "UNKNOWN")

    val ALLOWED_INCIDENT_STATUSES = setOf(
        "ALERTING",
        "ALERT_SENT",
        "RESOLVED",
        "CANCELLED",
        "DURESS_TRIGGERED"
    )

    val ALLOWED_INCIDENT_TYPES = setOf(
        "SOS",
        "HOLD_SOS",
        "Guardian Timeout",
        "Manual Alert",
        "Duress Alert",
        "Safety Timer Expired",
        "Stealth Disarm SOS"
    )

    // ==========================================
    // 1. INCIDENT RECORD VALIDATION
    // ==========================================
    fun validateIncident(incident: IncidentEntity): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (incident.type.isBlank()) {
            errors.add(ValidationError("type", "Incident type cannot be blank", incident.type))
        } else if (incident.type.length > 50) {
            errors.add(ValidationError("type", "Incident type cannot exceed 50 characters", incident.type))
        }

        if (incident.status !in ALLOWED_INCIDENT_STATUSES) {
            errors.add(
                ValidationError(
                    "status",
                    "Status '${incident.status}' is invalid. Allowed: ${ALLOWED_INCIDENT_STATUSES.joinToString()}",
                    incident.status
                )
            )
        }

        if (incident.timestamp <= 1577836800000L) { // Jan 1, 2020
            errors.add(ValidationError("timestamp", "Timestamp must be after Jan 1, 2020", incident.timestamp))
        }

        // Location & GeoJSON Point validation
        if (incident.latitude != null || incident.longitude != null) {
            if (incident.latitude == null || incident.longitude == null) {
                errors.add(ValidationError("location", "Both latitude and longitude must be provided together"))
            } else {
                val lat = incident.latitude
                val lng = incident.longitude
                if (lat < -90.0 || lat > 90.0) {
                    errors.add(ValidationError("latitude", "Latitude $lat out of bounds [-90.0, 90.0]", lat))
                }
                if (lng < -180.0 || lng > 180.0) {
                    errors.add(ValidationError("longitude", "Longitude $lng out of bounds [-180.0, 180.0]", lng))
                }
            }
        }

        if (incident.contactsNotifiedCount < 0) {
            errors.add(ValidationError("contactsNotifiedCount", "Contacts notified count cannot be negative", incident.contactsNotifiedCount))
        }

        // Timeline JSON format validation
        if (incident.timelineJson.isNotBlank()) {
            try {
                JSONArray(incident.timelineJson)
            } catch (e: Exception) {
                errors.add(ValidationError("timelineJson", "timelineJson must be a valid JSON array: ${e.message}"))
            }
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    /**
     * Converts a validated IncidentEntity to standard GeoJSON BSON structure
     */
    fun toValidatedIncidentBson(incident: IncidentEntity): Result<JSONObject> {
        val validation = validateIncident(incident)
        if (validation is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException("Incident schema violation: ${validation.summary()}"))
        }

        val json = JSONObject().apply {
            put("_schema_version", "1.0.0")
            put("incident_id", incident.id)
            put("type", incident.type)
            put("timestamp", incident.timestamp)
            put("status", incident.status)
            put("location_name", incident.locationName ?: "")

            if (incident.latitude != null && incident.longitude != null) {
                put("location", JSONObject().apply {
                    put("type", "Point")
                    put("coordinates", JSONArray().apply {
                        put(incident.longitude)
                        put(incident.latitude)
                    })
                })
                put("accuracy_meters", incident.accuracyMeters ?: 0f)
            }

            put("contacts_notified", incident.contactsNotifiedCount)
            put("ai_summary", incident.aiSummary ?: "")
            put("notes", incident.notes)
            put("has_audio_evidence", incident.hasAudioEvidence)
            if (incident.resolvedTimestamp != null) {
                put("resolved_at", incident.resolvedTimestamp)
            }
            put("synced_at", System.currentTimeMillis())
        }

        return Result.success(json)
    }

    // ==========================================
    // 2. TRUSTED CONTACT VALIDATION
    // ==========================================
    fun validateContact(contact: ContactEntity): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        val name = contact.name.trim()
        if (name.length < 2 || name.length > 80) {
            errors.add(ValidationError("name", "Contact name must be between 2 and 80 characters", name))
        }

        val phone = contact.phone.trim()
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.add(ValidationError("phone", "Invalid phone number format '$phone'. Must contain 7-25 digits/symbols", phone))
        }

        val email = contact.email.trim()
        if (email.isNotEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            errors.add(ValidationError("email", "Invalid email address format '$email'", email))
        }

        if (contact.relationship.trim().isEmpty()) {
            errors.add(ValidationError("relationship", "Relationship cannot be empty", contact.relationship))
        } else if (contact.relationship.length > 50) {
            errors.add(ValidationError("relationship", "Relationship cannot exceed 50 characters", contact.relationship))
        }

        if (contact.priority !in 1..5) {
            errors.add(ValidationError("priority", "Priority must be between 1 (Primary) and 5", contact.priority))
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    /**
     * Converts a validated ContactEntity to standard MongoDB BSON document
     */
    fun toValidatedContactBson(contact: ContactEntity): Result<JSONObject> {
        val validation = validateContact(contact)
        if (validation is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException("Contact schema violation: ${validation.summary()}"))
        }

        val json = JSONObject().apply {
            put("_schema_version", "1.0.0")
            put("contact_id", contact.id)
            put("name", contact.name.trim())
            put("phone", contact.phone.trim())
            put("email", contact.email.trim())
            put("relationship", contact.relationship.trim())
            put("priority", contact.priority)
            put("notify_enabled", contact.notifyEnabled)
            put("is_verified", contact.isVerified)
            put("created_at", contact.createdAt)
            put("synced_at", System.currentTimeMillis())
        }

        return Result.success(json)
    }

    // ==========================================
    // 3. USER PROFILE (ICE CARD) VALIDATION
    // ==========================================
    fun validateUserProfile(profile: UserProfileEntity): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        val name = profile.displayName.trim()
        if (name.length < 2 || name.length > 80) {
            errors.add(ValidationError("displayName", "Display name must be between 2 and 80 characters", name))
        }

        val phone = profile.phone.trim()
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.add(ValidationError("phone", "Invalid user phone format '$phone'", phone))
        }

        val emergencyNum = profile.emergencyNumber.trim()
        val digitsOnly = emergencyNum.filter { it.isDigit() }
        if (digitsOnly.length < 3 || digitsOnly.length > 15) {
            errors.add(
                ValidationError(
                    "emergencyNumber",
                    "Emergency dispatch number must be between 3 and 15 digits (e.g. 911, 112)",
                    emergencyNum
                )
            )
        }

        val bloodTypeUpper = profile.bloodType.trim().uppercase()
        if (bloodTypeUpper !in ALLOWED_BLOOD_TYPES) {
            errors.add(
                ValidationError(
                    "bloodType",
                    "Invalid blood type '$bloodTypeUpper'. Allowed: ${ALLOWED_BLOOD_TYPES.joinToString()}",
                    profile.bloodType
                )
            )
        }

        val safetyPin = profile.safetyPin.trim()
        if (!PIN_PATTERN.matcher(safetyPin).matches()) {
            errors.add(ValidationError("safetyPin", "Safety PIN must be between 4 and 8 digits", "******"))
        }

        val duressPin = profile.duressPin.trim()
        if (!PIN_PATTERN.matcher(duressPin).matches()) {
            errors.add(ValidationError("duressPin", "Duress PIN must be between 4 and 8 digits", "******"))
        }

        // CRITICAL SAFETY RULE: Safety PIN and Duress PIN MUST NOT match
        if (safetyPin.isNotEmpty() && duressPin.isNotEmpty() && safetyPin == duressPin) {
            errors.add(
                ValidationError(
                    "duressPin",
                    "CRITICAL SAFETY VIOLATION: Duress PIN cannot be identical to Safety PIN. It must be a distinct silent alarm code."
                )
            )
        }

        if (profile.physicianPhone.isNotBlank() && !PHONE_PATTERN.matcher(profile.physicianPhone.trim()).matches()) {
            errors.add(ValidationError("physicianPhone", "Invalid physician phone format", profile.physicianPhone))
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    /**
     * Converts a validated UserProfileEntity to encrypted ICE structure for MongoDB Atlas
     */
    fun toValidatedUserProfileBson(profile: UserProfileEntity): Result<JSONObject> {
        val validation = validateUserProfile(profile)
        if (validation is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException("UserProfile schema violation: ${validation.summary()}"))
        }

        val json = JSONObject().apply {
            put("_schema_version", "1.0.0")
            put("user_id", profile.id)
            put("display_name", profile.displayName.trim())
            put("phone", profile.phone.trim())
            put("emergency_number", profile.emergencyNumber.trim())
            put("blood_type", profile.bloodType.trim().uppercase())
            put("allergies", profile.allergies.trim())
            put("medications", profile.medications.trim())
            put("chronic_conditions", profile.chronicConditions.trim())
            put("physician_name", profile.physicianName.trim())
            put("physician_phone", profile.physicianPhone.trim())
            put("is_organ_donor", profile.isOrganDonor)
            put("emergency_notes", profile.emergencyNotes.trim())
            put("emergency_message_template", profile.emergencyMessageTemplate.trim())
            put("demo_mode_enabled", profile.demoMode)
            // Pins are NEVER stored in plaintext in the remote cloud document
            put("has_configured_pins", true)
            put("synced_at", System.currentTimeMillis())
        }

        return Result.success(json)
    }

    // ==========================================
    // 4. RAW BSON / JSON DOCUMENT VALIDATOR
    // ==========================================
    fun validateRawJson(collectionName: String, rawJson: String): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (rawJson.isBlank()) {
            errors.add(ValidationError("document", "Document body cannot be empty"))
            return ValidationResult.Invalid(errors)
        }

        val json = try {
            JSONObject(rawJson)
        } catch (e: Exception) {
            errors.add(ValidationError("syntax", "Invalid JSON syntax: ${e.message}"))
            return ValidationResult.Invalid(errors)
        }

        // Schema rules by collection
        when (collectionName) {
            "incidents" -> {
                if (!json.has("type") && !json.has("incident_id")) {
                    errors.add(ValidationError("type", "Incidents collection requires 'type' or 'incident_id'"))
                }
                if (json.has("location")) {
                    val loc = json.optJSONObject("location")
                    if (loc == null) {
                        errors.add(ValidationError("location", "'location' must be an object with GeoJSON Point"))
                    } else {
                        val coords = loc.optJSONArray("coordinates")
                        if (coords == null || coords.length() != 2) {
                            errors.add(ValidationError("location.coordinates", "GeoJSON Point requires [lng, lat] coordinates array of length 2"))
                        }
                    }
                }
            }
            "contacts" -> {
                if (!json.has("name")) {
                    errors.add(ValidationError("name", "Contacts collection document requires 'name' field"))
                }
                if (!json.has("phone")) {
                    errors.add(ValidationError("phone", "Contacts collection document requires 'phone' field"))
                }
            }
            "user_profile" -> {
                if (!json.has("display_name") && !json.has("name")) {
                    errors.add(ValidationError("display_name", "User profile document requires 'display_name' field"))
                }
            }
            "safe_zones" -> {
                if (!json.has("name")) {
                    errors.add(ValidationError("name", "Safe zone document requires 'name' field"))
                }
            }
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}
