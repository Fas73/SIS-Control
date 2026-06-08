package com.siscontrol.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.siscontrol.mobile.data.local.dao.UserSessionDao
import com.siscontrol.mobile.data.local.dao.DismissedAlertDao
import com.siscontrol.mobile.data.local.dao.PendingScanDao
import com.siscontrol.mobile.data.local.dao.PendingIncidentDao
import com.siscontrol.mobile.data.local.entities.UserSessionEntity
import com.siscontrol.mobile.data.local.entities.DismissedAlertEntity
import com.siscontrol.mobile.data.local.entities.PendingScanEntity
import com.siscontrol.mobile.data.local.entities.PendingIncidentEntity

@Database(entities = [UserSessionEntity::class, DismissedAlertEntity::class, PendingScanEntity::class, PendingIncidentEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userSessionDao(): UserSessionDao
    abstract fun dismissedAlertDao(): DismissedAlertDao
    abstract fun pendingScanDao(): PendingScanDao
    abstract fun pendingIncidentDao(): PendingIncidentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sis_control_db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true) // Evita errores al cambiar el esquema de la BD eliminando tablas antiguas
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
