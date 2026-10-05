package com.example

import com.example.data.model.ContactEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.UserProfileEntity
import com.example.data.validation.MongoSchemaValidator
import com.example.data.validation.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MongoSchemaValidatorTest {

    // ==========================================
    // 1. INCIDENT RECORD TESTS
    // ==========================================
    @Test
    fun validIncident_passesValidation() {
        val validIncident = IncidentEntity(
            id = 10,
            type = "SOS",
            timestamp = System.currentTimeMillis(),
            latitude = 37.7749,
            longitude = -122.4194,
            accuracyMeters = 5.0f,
            locationName = "San Francisco, CA",
            status = "ALERT_SENT",
            contactsNotifiedCount = 3
        )

        val result = MongoSchemaValidator.validateIncident(validIncident)
        assertTrue("Valid incident should pass schema validation", result.isValid)

        val bsonResult = MongoSchemaValidator.toValidatedIncidentBson(validIncident)
        assertTrue(bsonResult.isSuccess)
        val json = bsonResult.getOrThrow()
        assertEquals("SOS", json.getString("type"))
        assertEquals("ALERT_SENT", json.getString("status"))
        assertEquals("1.0.0", json.getString("_schema_version"))

        val locationObj = json.getJSONObject("location")
        assertEquals("Point", locationObj.getString("type"))
        val coords = locationObj.getJSONArray("coordinates")
        assertEquals(-122.4194, coords.getDouble(0), 0.0001)
        assertEquals(37.7749, coords.getDouble(1), 0.0001)
    }

    @Test
    fun incidentWithInvalidCoordinates_failsValidation() {
        val invalidLat = IncidentEntity(
            type = "SOS",
            latitude = 95.5, // Latitude cannot exceed 90.0
            longitude = -122.4194,
            status = "ALERT_SENT"
        )
        val resultLat = MongoSchemaValidator.validateIncident(invalidLat)
        assertFalse(resultLat.isValid)
        val latErrors = (resultLat as ValidationResult.Invalid).errors
        assertTrue(latErrors.any { it.field == "latitude" })

        val invalidLng = IncidentEntity(
            type = "SOS",
            latitude = 37.7749,
            longitude = -195.0, // Longitude cannot be < -180.0
            status = "ALERT_SENT"
        )
        val resultLng = MongoSchemaValidator.validateIncident(invalidLng)
        assertFalse(resultLng.isValid)
        val lngErrors = (resultLng as ValidationResult.Invalid).errors
        assertTrue(lngErrors.any { it.field == "longitude" })
    }

    @Test
    fun incidentWithInvalidStatus_failsValidation() {
        val invalidIncident = IncidentEntity(
            type = "SOS",
            status = "INVALID_STATUS_CODE"
        )
        val result = MongoSchemaValidator.validateIncident(invalidIncident)
        assertFalse(result.isValid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == "status" })
    }

    // ==========================================
    // 2. CONTACT RECORD TESTS
    // ==========================================
    @Test
    fun validContact_passesValidation() {
        val contact = ContactEntity(
            name = "Sarah Morgan",
            phone = "+1 (555) 234-5678",
            email = "sarah.morgan@example.com",
            relationship = "Sister",
            priority = 1
        )
        val result = MongoSchemaValidator.validateContact(contact)
        assertTrue(result.isValid)

        val bsonResult = MongoSchemaValidator.toValidatedContactBson(contact)
        assertTrue(bsonResult.isSuccess)
        val json = bsonResult.getOrThrow()
        assertEquals("Sarah Morgan", json.getString("name"))
        assertEquals("+1 (555) 234-5678", json.getString("phone"))
        assertEquals("1.0.0", json.getString("_schema_version"))
    }

    @Test
    fun contactWithInvalidPhone_failsValidation() {
        val contact = ContactEntity(
            name = "Sarah",
            phone = "abc-no-digits",
            relationship = "Sister"
        )
        val result = MongoSchemaValidator.validateContact(contact)
        assertFalse(result.isValid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == "phone" })
    }

    @Test
    fun contactWithInvalidEmail_failsValidation() {
        val contact = ContactEntity(
            name = "Sarah",
            phone = "+15551234567",
            email = "invalid-email-without-domain",
            relationship = "Sister"
        )
        val result = MongoSchemaValidator.validateContact(contact)
        assertFalse(result.isValid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == "email" })
    }

    // ==========================================
    // 3. USER PROFILE (ICE) RECORD TESTS
    // ==========================================
    @Test
    fun validUserProfile_passesValidation() {
        val profile = UserProfileEntity(
            displayName = "Alex Morgan",
            phone = "+1 (555) 234-5678",
            emergencyNumber = "911",
            bloodType = "O+",
            safetyPin = "1234",
            duressPin = "9999"
        )
        val result = MongoSchemaValidator.validateUserProfile(profile)
        assertTrue(result.isValid)

        val bsonResult = MongoSchemaValidator.toValidatedUserProfileBson(profile)
        assertTrue(bsonResult.isSuccess)
        val json = bsonResult.getOrThrow()
        assertEquals("Alex Morgan", json.getString("display_name"))
        assertEquals("O+", json.getString("blood_type"))
        assertFalse("PINs must not be exported in plaintext", json.has("safety_pin"))
    }

    @Test
    fun userProfileWithInvalidBloodType_failsValidation() {
        val profile = UserProfileEntity(
            displayName = "Alex Morgan",
            phone = "+1 (555) 234-5678",
            emergencyNumber = "911",
            bloodType = "INVALID_BLOOD_TYPE"
        )
        val result = MongoSchemaValidator.validateUserProfile(profile)
        assertFalse(result.isValid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == "bloodType" })
    }

    @Test
    fun userProfileWithIdenticalSafetyAndDuressPins_triggersSafetyViolation() {
        val dangerousProfile = UserProfileEntity(
            displayName = "Alex Morgan",
            phone = "+1 (555) 234-5678",
            emergencyNumber = "911",
            bloodType = "O+",
            safetyPin = "1234",
            duressPin = "1234" // Identical pins violate silent alarm safety requirement
        )
        val result = MongoSchemaValidator.validateUserProfile(dangerousProfile)
        assertFalse("Duress PIN cannot be identical to Safety PIN", result.isValid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == "duressPin" })
    }

    // ==========================================
    // 4. RAW JSON SCHEMA VALIDATION TESTS
    // ==========================================
    @Test
    fun rawJson_validatesCorrectly() {
        val validIncidentJson = """
            {
               "type": "SOS",
               "status": "ALERT_SENT",
               "location": {
                  "type": "Point",
                  "coordinates": [-122.4194, 37.7749]
               }
            }
        """.trimIndent()
        val result = MongoSchemaValidator.validateRawJson("incidents", validIncidentJson)
        assertTrue(result.isValid)

        val malformedJson = "{ broken json: ..."
        val malformedResult = MongoSchemaValidator.validateRawJson("incidents", malformedJson)
        assertFalse(malformedResult.isValid)

        val missingContactField = """{"relationship": "Friend"}"""
        val missingResult = MongoSchemaValidator.validateRawJson("contacts", missingContactField)
        assertFalse(missingResult.isValid)
    }
}
