package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.EmergencyDirectory
import com.example.data.model.RiskLevel
import com.example.service.GeminiSafetyService
import com.example.service.LocationState
import com.example.ui.screens.parseTimeline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies Pocket Guardian app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Pocket Guardian", appName)
    }

    @Test
    fun `safety score calculates HIGH risk during active SOS`() {
        val service = GeminiSafetyService()
        val score = service.calculateSafetyScore(
            missedCheckIns = 0,
            hasActiveSos = true,
            locationAvailable = true,
            journeyActive = false
        )
        assertEquals(RiskLevel.HIGH, score.level)
        assertTrue(score.score >= 90)
    }

    @Test
    fun `safety score calculates ATTENTION when check-in is missed`() {
        val service = GeminiSafetyService()
        val score = service.calculateSafetyScore(
            missedCheckIns = 1,
            hasActiveSos = false,
            locationAvailable = true,
            journeyActive = true
        )
        assertEquals(RiskLevel.ATTENTION, score.level)
    }

    @Test
    fun `safety score calculates LOW risk in normal conditions`() {
        val service = GeminiSafetyService()
        val score = service.calculateSafetyScore(
            missedCheckIns = 0,
            hasActiveSos = false,
            locationAvailable = true,
            journeyActive = false
        )
        assertEquals(RiskLevel.LOW, score.level)
    }

    @Test
    fun `location state generates valid Google Maps share link`() {
        val loc = LocationState(
            latitude = 37.7749,
            longitude = -122.4194,
            accuracyMeters = 5f,
            locationName = "Campus Quad"
        )
        assertTrue(loc.mapsUrl.contains("37.7749,-122.4194"))
    }

    @Test
    fun `parseTimeline handles valid json string correctly`() {
        val sampleJson = """
            [
                {"title":"SOS Activated","description":"Hold to SOS triggered","timestamp":1000,"isAlert":true},
                {"title":"Location Locked","description":"GPS fix acquired","timestamp":2000,"isAlert":false}
            ]
        """.trimIndent()
        val steps = parseTimeline(sampleJson)
        assertEquals(2, steps.size)
        assertEquals("SOS Activated", steps[0].title)
        assertTrue(steps[0].isAlert)
    }

    @Test
    fun `emergency directory contains key national lifelines`() {
        val resources = EmergencyDirectory.resources
        assertTrue(resources.any { it.number == "911" })
        assertTrue(resources.any { it.number == "988" })
        assertTrue(resources.any { it.number == "741741" })
    }

    @Test
    fun `safe havens catalog calculates distances and returns sorted results`() {
        val sorted = com.example.data.model.SafeHavensCatalog.calculateSortedHavens(37.7749, -122.4194)
        assertTrue(sorted.isNotEmpty())
        assertTrue(sorted.first().distanceMiles <= sorted.last().distanceMiles)
    }

    @Test
    fun `emergency translation catalog has key languages available`() {
        val languages = com.example.data.model.EmergencyTranslationsCatalog.languages
        assertTrue(languages.contains("Spanish"))
        assertTrue(languages.contains("French"))
        assertTrue(languages.contains("Mandarin"))
        assertTrue(languages.contains("Hindi"))
        assertTrue(languages.contains("German"))
        assertTrue(languages.contains("Arabic"))
    }

    @Test
    fun `mongodb atlas service initializes and provides valid collections and handshake`() = kotlinx.coroutines.test.runTest {
        val service = com.example.service.MongoDbAtlasService()
        val connected = service.testConnection()
        assertTrue(connected)
        val status = service.syncStatus.value
        assertTrue(status.collectionStats.containsKey("incidents"))
        assertTrue(status.collectionStats.containsKey("contacts"))
        assertTrue(status.collectionStats.containsKey("safe_zones"))
    }
}
