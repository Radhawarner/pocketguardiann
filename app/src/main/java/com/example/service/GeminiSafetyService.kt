package com.example.service

import com.example.BuildConfig
import com.example.data.model.RiskLevel
import com.example.data.model.SafetyScoreResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiSafetyService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun askGuardianAI(userPrompt: String, situationalContext: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineSafetyGuidance(userPrompt)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val systemInstruction = """
                You are Guardian AI, an empathetic, calm, practical, and highly responsible personal safety assistant in the Pocket Guardian app.
                Safety Rules:
                1. Give direct, actionable, practical personal safety guidance.
                2. If the user feels immediate danger, prioritize: Move to a populated well-lit area, call trusted contacts or dial emergency services (911/112), and activate Pocket Guardian SOS.
                3. Do NOT pretend to be law enforcement or medical staff.
                4. Never guarantee safety or make speculative criminal accusations.
                5. Keep emergency answers concise, clear, and easy to read in 2-4 bullet points.
            """.trimIndent()

            val contentsArray = JSONArray().apply {
                val promptText = if (situationalContext.isNullOrBlank()) {
                    userPrompt
                } else {
                    "Current Safety Context: $situationalContext\n\nUser Question: $userPrompt"
                }

                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", promptText) })
                    })
                })
            }

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("topP", 0.9)
                    put("maxOutputTokens", 800)
                })
            }

            val body = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext getOfflineSafetyGuidance(userPrompt)
                }
                val responseBody = response.body?.string() ?: return@withContext getOfflineSafetyGuidance(userPrompt)
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", getOfflineSafetyGuidance(userPrompt))
                    }
                }
                return@withContext getOfflineSafetyGuidance(userPrompt)
            }
        } catch (e: Exception) {
            return@withContext getOfflineSafetyGuidance(userPrompt)
        }
    }

    suspend fun generateEmergencyMessage(
        userName: String,
        situation: String,
        latitude: Double?,
        longitude: Double?,
        locationName: String?
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val locStr = if (latitude != null && longitude != null) {
                "Location: ${locationName ?: "GPS Coordinates"} (https://maps.google.com/?q=$latitude,$longitude)"
            } else {
                "Location: Updating..."
            }
            return@withContext "EMERGENCY: This is an automated Pocket Guardian alert for $userName. Situation: $situation. $locStr. Please call or send help immediately."
        }

        try {
            val prompt = """
                Generate a concise, high-priority emergency SMS alert for trusted contacts.
                User: $userName
                Situation: $situation
                Coordinates: ${latitude ?: "Unknown"}, ${longitude ?: "Unknown"}
                Location Name: ${locationName ?: "Unknown"}
                Timestamp: ${System.currentTimeMillis()}
                
                Requirements:
                - Concise under 160 characters if possible.
                - Include user name, situation, clear call to action, and maps link if coordinates provided.
                - Do not include hashtags or marketing.
            """.trimIndent()

            val response = askGuardianAI(prompt, "Generating emergency SMS broadcast")
            if (response.isNotBlank() && !response.startsWith("Guardian Safety Protocol")) {
                return@withContext response.trim()
            }
        } catch (e: Exception) {
            // Fallback
        }

        val mapLink = if (latitude != null && longitude != null) " https://maps.google.com/?q=$latitude,$longitude" else ""
        return@withContext "EMERGENCY: $userName has triggered a Pocket Guardian alert ($situation).$mapLink Please check in immediately."
    }

    suspend fun generateIncidentSummary(
        incidentType: String,
        timeFormatted: String,
        location: String,
        contactsNotified: Int,
        resolvedInMinutes: Int?
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val resolvedText = if (resolvedInMinutes != null) "The incident was resolved in $resolvedInMinutes minutes." else "The incident is currently ongoing."
            return@withContext "A $incidentType alert was recorded at $timeFormatted near $location. $contactsNotified trusted contact(s) were notified. $resolvedText"
        }

        try {
            val prompt = """
                Provide a factual, calm 2-sentence summary of this safety incident based ONLY on the provided verified data:
                Type: $incidentType
                Time: $timeFormatted
                Location: $location
                Contacts Notified: $contactsNotified
                Resolution: ${if (resolvedInMinutes != null) "Resolved in $resolvedInMinutes mins" else "Active"}
                
                Never speculate or invent details.
            """.trimIndent()

            val text = askGuardianAI(prompt, "Incident Post-Action Review")
            if (text.isNotBlank() && !text.startsWith("Guardian Safety Protocol")) {
                return@withContext text.trim()
            }
        } catch (e: Exception) {
            // Fallback
        }

        return@withContext "A $incidentType alert was recorded at $timeFormatted near $location. $contactsNotified trusted contact(s) were notified."
    }

    fun calculateSafetyScore(
        missedCheckIns: Int,
        hasActiveSos: Boolean,
        locationAvailable: Boolean,
        journeyActive: Boolean
    ): SafetyScoreResult {
        return when {
            hasActiveSos -> SafetyScoreResult(
                level = RiskLevel.HIGH,
                score = 95,
                explanation = "High attention: Emergency SOS is currently active. Emergency contacts and location sharing engaged.",
                recommendations = listOf(
                    "Stay in your current secure position if safe.",
                    "Keep Pocket Guardian open to transmit real-time telemetry.",
                    "If immediate danger persists, dial emergency authorities immediately."
                )
            )
            missedCheckIns > 1 -> SafetyScoreResult(
                level = RiskLevel.HIGH,
                score = 80,
                explanation = "High attention: Multiple scheduled check-ins were missed during your active Guardian journey.",
                recommendations = listOf(
                    "Respond to the pending 'Are you safe?' prompt immediately.",
                    "Inform your designated guardian contact if you are delayed.",
                    "Check network and battery levels to ensure alert transmission."
                )
            )
            missedCheckIns == 1 -> SafetyScoreResult(
                level = RiskLevel.ATTENTION,
                score = 55,
                explanation = "Attention required: One scheduled check-in was missed. Guardian grace period is currently running.",
                recommendations = listOf(
                    "Tap 'I'm Safe' in Guardian Mode to reset the check-in timer.",
                    "Verify your estimated arrival time if traffic or transit changed."
                )
            )
            !locationAvailable -> SafetyScoreResult(
                level = RiskLevel.ATTENTION,
                score = 45,
                explanation = "Attention: Location telemetry is currently unavailable or permission is restricted.",
                recommendations = listOf(
                    "Enable precise location permissions to allow accurate SOS coordinates.",
                    "Turn on GPS services for continuous safety coverage."
                )
            )
            journeyActive -> SafetyScoreResult(
                level = RiskLevel.LOW,
                score = 15,
                explanation = "Normal status: Guardian Mode is actively tracking your journey. Check-in timers are regular.",
                recommendations = listOf(
                    "Maintain your intended route toward destination.",
                    "Be prepared to tap 'I'm Safe' on the next prompt interval."
                )
            )
            else -> SafetyScoreResult(
                level = RiskLevel.LOW,
                score = 5,
                explanation = "Optimal safety status: All systems ready, trusted contacts connected, SOS standby active.",
                recommendations = listOf(
                    "Keep your phone charged when travelling alone.",
                    "Verify that at least two trusted contacts are enabled."
                )
            )
        }
    }

    private fun getOfflineSafetyGuidance(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("follow") || q.contains("behind") -> """
                • Move toward a well-lit, populated area immediately (a store, restaurant, or transit station).
                • Cross the street or change direction to verify if you are actually being followed without confronting them.
                • Call a trusted contact and speak clearly about your exact location and where you are heading.
                • If you feel an immediate threat, enter an open public establishment and dial 911/112 or activate Pocket Guardian SOS.
            """.trimIndent()

            q.contains("alone") || q.contains("walk") || q.contains("night") -> """
                • Stay on well-lit, populated routes and avoid secluded shortcuts or unlit alleys.
                • Keep your phone accessible in your hand, with Pocket Guardian Hold-To-SOS ready.
                • Keep one ear free from headphones to maintain situational awareness.
                • Share your live Guardian journey with a trusted contact before starting your walk.
            """.trimIndent()

            q.contains("travel") || q.contains("uber") || q.contains("cab") || q.contains("taxi") -> """
                • Always verify the license plate, driver photo, and car model before entering.
                • Ask "Who are you picking up?" rather than stating your own name first.
                • Sit in the back seat to maintain space and multiple exit options.
                • Share your trip details with a trusted contact or start a Pocket Guardian journey.
            """.trimIndent()

            q.contains("discreet") || q.contains("silent") -> """
                • Use Pocket Guardian's 3-second hold to initiate SOS silently without drawing attention.
                • Set up your emergency message template beforehand so contacts know your specific medical or safety needs.
                • Stay on the line if you dial emergency services even if you cannot speak; dispatchers are trained to listen.
            """.trimIndent()

            else -> """
                • Assess your immediate surroundings and trust your instincts—if a situation feels wrong, prioritize removing yourself.
                • Move toward open, populated commercial venues or well-lit spaces.
                • Contact a trusted contact or start Guardian Mode for periodic automated check-ins.
                • For urgent emergencies, use the red SOS button or call local emergency services (911/112) immediately.
            """.trimIndent()
        }
    }
}
