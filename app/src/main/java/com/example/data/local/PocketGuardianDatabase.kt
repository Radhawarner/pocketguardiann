package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AudioEvidenceEntity
import com.example.data.model.ContactEntity
import com.example.data.model.GuardianSessionEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.SafeZoneEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ContactEntity::class,
        IncidentEntity::class,
        GuardianSessionEntity::class,
        SafeZoneEntity::class,
        UserProfileEntity::class,
        AudioEvidenceEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PocketGuardianDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun incidentDao(): IncidentDao
    abstract fun guardianDao(): GuardianDao
    abstract fun safeZoneDao(): SafeZoneDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun audioEvidenceDao(): AudioEvidenceDao

    companion object {
        @Volatile
        private var INSTANCE: PocketGuardianDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PocketGuardianDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PocketGuardianDatabase::class.java,
                    "pocket_guardian_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: PocketGuardianDatabase) {
                // Populate default profile
                database.userProfileDao().insertOrUpdateProfile(
                    UserProfileEntity(
                        id = 1,
                        displayName = "Alex Morgan",
                        phone = "+1 (555) 234-5678",
                        emergencyNumber = "911",
                        emergencyMessageTemplate = "EMERGENCY: I need immediate assistance. Pocket Guardian safety alert was triggered. Here is my live location:",
                        bloodType = "O+",
                        allergies = "Penicillin (Moderate)",
                        demoMode = true,
                        safetyPin = "1234",
                        duressPin = "9999"
                    )
                )

                // Populate trusted contacts
                database.contactDao().insertContact(
                    ContactEntity(
                        name = "Sarah Morgan",
                        phone = "+1 (555) 345-6789",
                        email = "sarah.m@example.com",
                        relationship = "Parent",
                        priority = 1,
                        notifyEnabled = true,
                        isVerified = true
                    )
                )
                database.contactDao().insertContact(
                    ContactEntity(
                        name = "David Chen",
                        phone = "+1 (555) 456-7890",
                        email = "david.c@example.com",
                        relationship = "Partner",
                        priority = 1,
                        notifyEnabled = true,
                        isVerified = true
                    )
                )
                database.contactDao().insertContact(
                    ContactEntity(
                        name = "Campus Safety Escort",
                        phone = "+1 (555) 789-0123",
                        email = "safety@campus.edu",
                        relationship = "Emergency Contact",
                        priority = 2,
                        notifyEnabled = true,
                        isVerified = true
                    )
                )

                // Populate default safe zones
                database.safeZoneDao().insertSafeZone(
                    SafeZoneEntity(
                        name = "Campus North Quad & Escort Post",
                        category = "Campus",
                        address = "Building A, West Gate",
                        latitude = 37.7749,
                        longitude = -122.4194,
                        radiusMeters = 300,
                        autoNotifyOnArrival = true
                    )
                )
                database.safeZoneDao().insertSafeZone(
                    SafeZoneEntity(
                        name = "Downtown Police Precinct & Safe Haven",
                        category = "Police Station",
                        address = "850 Bryant St, Central District",
                        latitude = 37.7712,
                        longitude = -122.4042,
                        radiusMeters = 250,
                        autoNotifyOnArrival = true
                    )
                )
                database.safeZoneDao().insertSafeZone(
                    SafeZoneEntity(
                        name = "Home Residence",
                        category = "Home",
                        address = "420 Evergreen Terrace",
                        latitude = 37.7833,
                        longitude = -122.4167,
                        radiusMeters = 150,
                        autoNotifyOnArrival = true
                    )
                )

                // Populate sample past incident
                val timelineSample = """
                    [
                        {"title":"SOS Triggered","description":"Hold to SOS activated by Alex Morgan","timestamp":${System.currentTimeMillis() - 86400000},"isAlert":true},
                        {"title":"Location Acquired","description":"GPS fix at 37.7749° N, 122.4194° W (±4m)","timestamp":${System.currentTimeMillis() - 86395000},"isAlert":false},
                        {"title":"Contacts Notified","description":"Alert sent to 2 trusted contacts via SMS and Push","timestamp":${System.currentTimeMillis() - 86390000},"isAlert":true},
                        {"title":"Incident Resolved","description":"Safety PIN entered and user confirmed safe","timestamp":${System.currentTimeMillis() - 85500000},"isAlert":false}
                    ]
                """.trimIndent()

                database.incidentDao().insertIncident(
                    IncidentEntity(
                        type = "SOS",
                        timestamp = System.currentTimeMillis() - 86400000,
                        latitude = 37.7749,
                        longitude = -122.4194,
                        accuracyMeters = 4.2f,
                        locationName = "Market St & 4th Ave, Downtown",
                        status = "RESOLVED",
                        contactsNotifiedCount = 2,
                        timelineJson = timelineSample,
                        aiSummary = "An SOS was activated at 8:42 PM. Location was successfully captured and two trusted contacts were notified. The incident was verified and safely resolved in 15 minutes.",
                        resolvedTimestamp = System.currentTimeMillis() - 85500000,
                        notes = "False alarm triggered during transit walk; safety confirmed."
                    )
                )
            }
        }
    }
}
