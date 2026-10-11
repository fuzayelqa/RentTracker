package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.RentRecordEntity
import com.example.data.local.entity.UserAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RentTrackerDao {

    // User Account
    @Query("SELECT * FROM user_accounts WHERE id = :userId LIMIT 1")
    fun getUserAccount(userId: String): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts WHERE id = :userId LIMIT 1")
    suspend fun getUserAccountSync(userId: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserAccount(account: UserAccountEntity)

    @Query("DELETE FROM user_accounts WHERE id = :userId")
    suspend fun deleteUserAccount(userId: String)

    // Rent Records - Chronological (July 2026, August 2026, September 2026, October 2026...)
    @Query("SELECT * FROM rent_records WHERE userId = :userId ORDER BY year ASC, month ASC")
    fun getAllRentRecordsChronological(userId: String): Flow<List<RentRecordEntity>>

    @Query("SELECT * FROM rent_records WHERE userId = :userId ORDER BY year ASC, month ASC")
    suspend fun getAllRentRecordsChronologicalSync(userId: String): List<RentRecordEntity>

    @Query("SELECT * FROM rent_records WHERE userId = :userId AND monthKey = :monthKey LIMIT 1")
    fun getRentRecordByMonthKey(userId: String, monthKey: String): Flow<RentRecordEntity?>

    @Query("SELECT * FROM rent_records WHERE userId = :userId AND monthKey = :monthKey LIMIT 1")
    suspend fun getRentRecordByMonthKeySync(userId: String, monthKey: String): RentRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRentRecord(record: RentRecordEntity)

    @Query("DELETE FROM rent_records WHERE id = :id")
    suspend fun deleteRentRecord(id: String)

    @Query("DELETE FROM rent_records WHERE userId = :userId AND monthKey = :monthKey")
    suspend fun deleteRentRecordByMonthKey(userId: String, monthKey: String)

    @Query("DELETE FROM rent_records WHERE userId = :userId")
    suspend fun deleteAllRentRecordsForUser(userId: String)
}
