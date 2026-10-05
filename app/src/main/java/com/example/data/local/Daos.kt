package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContactEntity
import com.example.data.model.GuardianSessionEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.SafeZoneEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM trusted_contacts ORDER BY priority ASC, name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM trusted_contacts WHERE notifyEnabled = 1 ORDER BY priority ASC")
    suspend fun getActiveContacts(): List<ContactEntity>

    @Query("SELECT * FROM trusted_contacts WHERE id = :id")
    suspend fun getContactById(id: Long): ContactEntity?

    @Query("SELECT COUNT(*) FROM trusted_contacts")
    fun getContactCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)
}

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents ORDER BY timestamp DESC LIMIT 1")
    fun getLatestIncident(): Flow<IncidentEntity?>

    @Query("SELECT * FROM incidents WHERE id = :id")
    suspend fun getIncidentById(id: Long): IncidentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Update
    suspend fun updateIncident(incident: IncidentEntity)

    @Delete
    suspend fun deleteIncident(incident: IncidentEntity)

    @Query("DELETE FROM incidents")
    suspend fun clearAll()
}

@Dao
interface GuardianDao {
    @Query("SELECT * FROM guardian_sessions WHERE status IN ('ACTIVE', 'CHECK_IN_PENDING', 'ESCALATING') ORDER BY startedAt DESC LIMIT 1")
    fun getActiveSession(): Flow<GuardianSessionEntity?>

    @Query("SELECT * FROM guardian_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<GuardianSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GuardianSessionEntity): Long

    @Update
    suspend fun updateSession(session: GuardianSessionEntity)
}

@Dao
interface SafeZoneDao {
    @Query("SELECT * FROM safe_zones ORDER BY name ASC")
    fun getAllSafeZones(): Flow<List<SafeZoneEntity>>

    @Query("SELECT * FROM safe_zones")
    suspend fun getAllSafeZonesList(): List<SafeZoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafeZone(zone: SafeZoneEntity): Long

    @Delete
    suspend fun deleteSafeZone(zone: SafeZoneEntity)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}

@Dao
interface AudioEvidenceDao {
    @Query("SELECT * FROM audio_evidence ORDER BY timestamp DESC")
    fun getAllAudioEvidence(): Flow<List<com.example.data.model.AudioEvidenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioEvidence(evidence: com.example.data.model.AudioEvidenceEntity): Long

    @Delete
    suspend fun deleteAudioEvidence(evidence: com.example.data.model.AudioEvidenceEntity)

    @Query("DELETE FROM audio_evidence")
    suspend fun clearAll()
}
