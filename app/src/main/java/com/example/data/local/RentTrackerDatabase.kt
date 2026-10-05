package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.RentTrackerDao
import com.example.data.local.entity.AppNotificationEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PropertyEntity
import com.example.data.local.entity.RecurringChargeEntity
import com.example.data.local.entity.RentMonthEntity
import com.example.data.local.entity.UserSettingsEntity

@Database(
    entities = [
        PropertyEntity::class,
        RecurringChargeEntity::class,
        RentMonthEntity::class,
        PaymentEntity::class,
        AppNotificationEntity::class,
        AuditLogEntity::class,
        UserSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RentTrackerDatabase : RoomDatabase() {
    abstract fun rentTrackerDao(): RentTrackerDao

    companion object {
        @Volatile
        private var INSTANCE: RentTrackerDatabase? = null

        fun getDatabase(context: Context): RentTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RentTrackerDatabase::class.java,
                    "rent_tracker.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
