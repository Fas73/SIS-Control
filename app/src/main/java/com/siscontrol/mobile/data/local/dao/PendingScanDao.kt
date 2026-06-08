package com.siscontrol.mobile.data.local.dao

import androidx.room.*
import com.siscontrol.mobile.data.local.entities.PendingScanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingScanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: PendingScanEntity): Long

    @Query("SELECT * FROM pending_scans WHERE isSynced = 0 ORDER BY scannedAt ASC")
    suspend fun getUnsyncedScans(): List<PendingScanEntity>

    @Query("UPDATE pending_scans SET isSynced = 1 WHERE localId = :localId")
    suspend fun markAsSynced(localId: Long)

    @Delete
    suspend fun deleteScan(scan: PendingScanEntity)

    @Query("SELECT COUNT(*) FROM pending_scans WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>
}
