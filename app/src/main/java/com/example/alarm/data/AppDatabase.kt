package com.example.alarm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.alarm.data.dao.AlarmDao
import com.example.alarm.data.dao.DailyGoalOverrideDao
import com.example.alarm.data.dao.WaterLogDao
import com.example.alarm.data.entity.AlarmEntity
import com.example.alarm.data.entity.DailyGoalOverride
import com.example.alarm.data.entity.WaterLogEntity

@Database(entities = [WaterLogEntity::class, AlarmEntity::class, DailyGoalOverride::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterLogDao(): WaterLogDao
    abstract fun alarmDao(): AlarmDao
    abstract fun dailyGoalOverrideDao(): DailyGoalOverrideDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE water_logs ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE water_logs ADD COLUMN originalAmount INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alarm_database"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration() // For development, use destructive migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
