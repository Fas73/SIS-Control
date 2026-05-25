package com.siscontrol.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dismissed_alerts")
data class DismissedAlertEntity(
    @PrimaryKey
    val alertId: Long
)
