package com.siscontrol.mobile.data.local.dao

import androidx.room.*
import com.siscontrol.mobile.data.local.entities.PendingIncidentEntity

@Dao
interface PendingIncidentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: PendingIncidentEntity): Long

    @Query("SELECT * FROM pending_incidents WHERE isSynced = 0 ORDER BY clientTimestamp ASC")
    suspend fun getUnsyncedIncidents(): List<PendingIncidentEntity>

    @Delete
    suspend fun deleteIncident(incident: PendingIncidentEntity)
}
