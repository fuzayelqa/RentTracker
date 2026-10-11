package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.RentTrackerDao
import com.example.data.local.entity.RentRecordEntity
import com.example.data.local.entity.UserAccountEntity

@Database(
    entities = [
        UserAccountEntity::class,
        RentRecordEntity::class
    ],
    version = 6,
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
                    "my_rent_tracker.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
