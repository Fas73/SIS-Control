package com.siscontrol.mobile.data.local.dao

import androidx.room.*
import com.siscontrol.mobile.data.local.entities.DismissedAlertEntity

@Dao
interface DismissedAlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAsDismissed(alert: DismissedAlertEntity)

    @Query("SELECT alertId FROM dismissed_alerts")
    suspend fun getAllDismissedIds(): List<Long>
}
