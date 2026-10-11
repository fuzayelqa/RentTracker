package com.example.data.repository

import com.example.data.firebase.FirebaseSyncManager
import com.example.data.local.dao.RentTrackerDao
import com.example.data.local.entity.RentRecordEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow

class RentTrackerRepository(
    private val dao: RentTrackerDao,
    private val syncManager: FirebaseSyncManager = FirebaseSyncManager()
) {

    fun getUserAccount(userId: String): Flow<UserAccountEntity?> =
        dao.getUserAccount(userId)

    suspend fun getUserAccountSync(userId: String): UserAccountEntity? =
        dao.getUserAccountSync(userId)

    suspend fun saveUserAccount(account: UserAccountEntity) {
        dao.insertOrUpdateUserAccount(account)
        syncManager.syncUserAccount(account)
    }

    /**
     * Returns records in chronological order: July 2026, August 2026, September 2026, October 2026...
     */
    fun getAllRentRecordsChronological(userId: String): Flow<List<RentRecordEntity>> =
        dao.getAllRentRecordsChronological(userId)

    suspend fun getAllRentRecordsChronologicalSync(userId: String): List<RentRecordEntity> =
        dao.getAllRentRecordsChronologicalSync(userId)

    fun getRentRecordByMonthKey(userId: String, monthKey: String): Flow<RentRecordEntity?> =
        dao.getRentRecordByMonthKey(userId, monthKey)

    suspend fun getRentRecordByMonthKeySync(userId: String, monthKey: String): RentRecordEntity? =
        dao.getRentRecordByMonthKeySync(userId, monthKey)

    /**
     * IMPORTANT UPDATE RULE & VALIDATION:
     * 1. Rent tracking begins from July 2026. Records before July 2026 are not allowed.
     * 2. Once a record has been saved, it is permanently locked and cannot be edited.
     * 3. Prevents duplicate records for the same month.
     */
    suspend fun saveRentRecord(
        userId: String,
        year: Int,
        month: Int,
        rentAmount: Double,
        paymentDate: String,
        comment: String = "",
        status: String = "Paid",
        currency: String = "৳"
    ): Result<RentRecordEntity> {
        if (DateUtils.isBeforeStartMonth(year, month)) {
            return Result.failure(Exception("Rent tracking starts from July 2026. Cannot create records before July 2026."))
        }

        if (DateUtils.isMonthNotComeYet(year, month)) {
            return Result.failure(Exception("${DateUtils.formatMonthYear(year, month)} has not come yet. Records can only be added once that month arrives."))
        }

        if (rentAmount <= 0) {
            return Result.failure(Exception("Please enter a valid positive rent amount."))
        }

        if (paymentDate.isBlank()) {
            return Result.failure(Exception("Please select a payment date."))
        }

        val monthKey = DateUtils.formatMonthKey(year, month)
        val existing = dao.getRentRecordByMonthKeySync(userId, monthKey)

        if (existing != null) {
            if (existing.isLocked) {
                return Result.failure(Exception("This record is locked and cannot be edited after saving."))
            } else {
                return Result.failure(Exception("A rent record already exists for ${DateUtils.formatMonthYear(year, month)}."))
            }
        }

        // Advance Rent option is available for all months (July 2026 and any subsequent month)
        val finalStatus = if (status.isNotBlank()) status else "Paid"

        val newRecord = RentRecordEntity(
            id = "${userId}_$monthKey",
            userId = userId,
            month = month,
            year = year,
            monthKey = monthKey,
            rentAmount = rentAmount,
            currency = currency,
            paymentDate = paymentDate.trim(),
            comment = comment.trim(),
            status = finalStatus,
            isLocked = true, // Permanently locked upon save!
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        dao.insertRentRecord(newRecord)
        syncManager.saveRentRecord(newRecord)
        return Result.success(newRecord)
    }

    /**
     * DELETE / REMOVE SECURITY:
     * Only executed after the multi-step confirmation ("DELETE" typed).
     * RULE: If a record was updated/created in a past month, after that month ends the user cannot update or delete previous records.
     */
    suspend fun deleteRentRecord(userId: String, monthKey: String): Result<Unit> {
        val existing = dao.getRentRecordByMonthKeySync(userId, monthKey)
        if (existing != null && DateUtils.isRecordInPastMonth(existing.year, existing.month)) {
            val monthName = DateUtils.formatMonthYear(existing.year, existing.month)
            return Result.failure(Exception("$monthName has ended. Previous months' rent records are permanently finalized and cannot be deleted or edited."))
        }

        dao.deleteRentRecordByMonthKey(userId, monthKey)
        syncManager.deleteRentRecord(userId, monthKey)
        return Result.success(Unit)
    }

    /**
     * Pulls data from Firestore down into local Room database upon login,
     * and pushes any local records up to Firestore to ensure permanent cloud backup.
     */
    suspend fun syncWithCloud(userId: String) {
        val accountRes = syncManager.fetchUserAccount(userId)
        if (accountRes.isSuccess) {
            accountRes.getOrNull()?.let { dao.insertOrUpdateUserAccount(it) }
        }

        val recordsRes = syncManager.fetchRentRecords(userId)
        if (recordsRes.isSuccess) {
            val list = recordsRes.getOrNull() ?: emptyList()
            for (record in list) {
                dao.insertRentRecord(record)
            }
        }

        // Also ensure all records for this user are backed up to Firestore
        try {
            val localRecords = dao.getAllRentRecordsChronologicalSync(userId)
            for (localRec in localRecords) {
                syncManager.saveRentRecord(localRec)
            }
        } catch (_: Exception) {}
    }

    /**
     * Deletes user account and all rent records.
     */
    suspend fun deleteAccount(userId: String) {
        dao.deleteAllRentRecordsForUser(userId)
        dao.deleteUserAccount(userId)
        syncManager.deleteUserAccountData(userId)
    }

    suspend fun savePasswordResetCode(email: String, code: String): Result<Unit> =
        syncManager.savePasswordResetCode(email, code)

    suspend fun verifyAndResetPassword(email: String, code: String, newPassword: String): Result<Unit> =
        syncManager.verifyAndResetPassword(email, code, newPassword)

    suspend fun updatePasswordAndClearResetCode(email: String, newPassword: String): Result<Unit> =
        syncManager.updatePasswordAndClearResetCode(email, newPassword)

    suspend fun fetchCloudUserAccount(userId: String): Result<UserAccountEntity?> =
        syncManager.fetchUserAccount(userId)

    suspend fun fetchCloudUserAccountByEmail(email: String): Result<UserAccountEntity?> =
        syncManager.fetchUserAccountByEmail(email)
}
