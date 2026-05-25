package com.siscontrol.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.siscontrol.mobile.data.local.dao.UserSessionDao
import com.siscontrol.mobile.data.local.dao.DismissedAlertDao
import com.siscontrol.mobile.data.local.entities.UserSessionEntity
import com.siscontrol.mobile.data.local.entities.DismissedAlertEntity

@Database(entities = [UserSessionEntity::class, DismissedAlertEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userSessionDao(): UserSessionDao
    abstract fun dismissedAlertDao(): DismissedAlertDao

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
                .fallbackToDestructiveMigration() // Evita crashes al cambiar el esquema de la BD
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
