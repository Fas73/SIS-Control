package com.siscontrol.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.siscontrol.mobile.data.local.dao.UserSessionDao
import com.siscontrol.mobile.data.local.entities.UserSessionEntity

@Database(entities = [UserSessionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userSessionDao(): UserSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sis_control_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
