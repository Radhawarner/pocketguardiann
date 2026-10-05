package com.example.data.repository

import com.example.data.local.AudioEvidenceDao
import com.example.data.local.ContactDao
import com.example.data.local.GuardianDao
import com.example.data.local.IncidentDao
import com.example.data.local.SafeZoneDao
import com.example.data.local.UserProfileDao
import com.example.data.model.AudioEvidenceEntity
import com.example.data.model.ContactEntity
import com.example.data.model.GuardianSessionEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.SafeZoneEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class GuardianRepository(
    private val contactDao: ContactDao,
    private val incidentDao: IncidentDao,
    private val guardianDao: GuardianDao,
    private val safeZoneDao: SafeZoneDao,
    private val userProfileDao: UserProfileDao,
    private val audioEvidenceDao: AudioEvidenceDao
) {
    // Contacts
    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val contactCount: Flow<Int> = contactDao.getContactCount()

    suspend fun getActiveContacts(): List<ContactEntity> = contactDao.getActiveContacts()
    suspend fun addContact(contact: ContactEntity): Long = contactDao.insertContact(contact)
    suspend fun updateContact(contact: ContactEntity) = contactDao.updateContact(contact)
    suspend fun deleteContact(contact: ContactEntity) = contactDao.deleteContact(contact)

    // Incidents
    val allIncidents: Flow<List<IncidentEntity>> = incidentDao.getAllIncidents()
    val latestIncident: Flow<IncidentEntity?> = incidentDao.getLatestIncident()

    suspend fun getIncidentById(id: Long): IncidentEntity? = incidentDao.getIncidentById(id)
    suspend fun logIncident(incident: IncidentEntity): Long = incidentDao.insertIncident(incident)
    suspend fun updateIncident(incident: IncidentEntity) = incidentDao.updateIncident(incident)
    suspend fun deleteIncident(incident: IncidentEntity) = incidentDao.deleteIncident(incident)
    suspend fun clearAllIncidents() = incidentDao.clearAll()

    // Guardian Sessions
    val activeGuardianSession: Flow<GuardianSessionEntity?> = guardianDao.getActiveSession()
    val allSessions: Flow<List<GuardianSessionEntity>> = guardianDao.getAllSessions()

    suspend fun startGuardianSession(session: GuardianSessionEntity): Long = guardianDao.insertSession(session)
    suspend fun updateGuardianSession(session: GuardianSessionEntity) = guardianDao.updateSession(session)

    // Safe Zones
    val allSafeZones: Flow<List<SafeZoneEntity>> = safeZoneDao.getAllSafeZones()
    suspend fun getSafeZonesList(): List<SafeZoneEntity> = safeZoneDao.getAllSafeZonesList()
    suspend fun addSafeZone(zone: SafeZoneEntity): Long = safeZoneDao.insertSafeZone(zone)
    suspend fun deleteSafeZone(zone: SafeZoneEntity) = safeZoneDao.deleteSafeZone(zone)

    // Profile
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getProfile()
    suspend fun getProfileSync(): UserProfileEntity? = userProfileDao.getProfileSync()
    suspend fun updateProfile(profile: UserProfileEntity) = userProfileDao.insertOrUpdateProfile(profile)

    // Audio Evidence
    val allAudioEvidence: Flow<List<AudioEvidenceEntity>> = audioEvidenceDao.getAllAudioEvidence()
    suspend fun addAudioEvidence(evidence: AudioEvidenceEntity): Long = audioEvidenceDao.insertAudioEvidence(evidence)
    suspend fun deleteAudioEvidence(evidence: AudioEvidenceEntity) = audioEvidenceDao.deleteAudioEvidence(evidence)
    suspend fun clearAllAudioEvidence() = audioEvidenceDao.clearAll()
}
