package com.example.data.firebase

import android.util.Log
import com.example.data.local.entity.RentRecordEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.util.AuthUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseSyncManager {

    private val tag = "FirebaseSyncManager"

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "FirebaseAuth initialization exception", e)
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "FirebaseFirestore initialization exception", e)
            null
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth?.currentUser

    /**
     * Syncs user account info to Firestore: users/{userId}
     */
    suspend fun syncUserAccount(account: UserAccountEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
        val userId = account.id.ifBlank {
            AuthUtils.emailToUserId(account.email)
        }

        return try {
            val data = hashMapOf(
                "id" to userId,
                "email" to account.email.trim().lowercase(),
                "passwordHash" to account.passwordHash,
                "createdAt" to account.createdAt,
                "lastActive" to System.currentTimeMillis()
            )
            db.collection("users").document(userId)
                .set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Failed to sync user account: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetches user account info from Firestore: users/{userId}
     */
    suspend fun fetchUserAccount(userId: String): Result<UserAccountEntity?> {
        val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))

        return try {
            val doc = db.collection("users").document(userId).get().await()
            if (!doc.exists()) {
                return Result.success(null)
            }
            val account = UserAccountEntity(
                id = userId,
                email = doc.getString("email") ?: "",
                passwordHash = doc.getString("passwordHash") ?: "",
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
            Result.success(account)
        } catch (e: Exception) {
            Log.w(tag, "Failed to fetch user account: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetches user account info by email from Firestore.
     */
    suspend fun fetchUserAccountByEmail(email: String): Result<UserAccountEntity?> {
        val userId = AuthUtils.emailToUserId(email)
        return fetchUserAccount(userId)
    }

    /**
     * Saves a rent record to Firestore: users/{userId}/rentRecords/{monthKey}
     * RULE: Document key is monthKey (e.g. '2026-07', '2026-10'), preventing any duplicate records.
     */
    suspend fun saveRentRecord(record: RentRecordEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
        val userId = record.userId.ifBlank {
            auth?.currentUser?.uid ?: return Result.failure(Exception("User ID missing"))
        }

        return try {
            val data = hashMapOf(
                "id" to record.id,
                "userId" to userId,
                "month" to record.month,
                "year" to record.year,
                "monthKey" to record.monthKey,
                "rentAmount" to record.rentAmount,
                "currency" to record.currency,
                "paymentDate" to record.paymentDate,
                "comment" to record.comment,
                "status" to record.status,
                "isLocked" to record.isLocked,
                "createdAt" to record.createdAt,
                "updatedAt" to record.updatedAt
            )
            db.collection("users").document(userId)
                .collection("rentRecords").document(record.monthKey)
                .set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Failed to sync rent record (${record.monthKey}): ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetches all rent records for the user from Firestore: users/{userId}/rentRecords
     */
    suspend fun fetchRentRecords(userId: String): Result<List<RentRecordEntity>> {
        val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))

        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("rentRecords").get().await()

            val list = snapshot.documents.mapNotNull { doc ->
                val monthKey = doc.getString("monthKey") ?: doc.id
                RentRecordEntity(
                    id = "${userId}_$monthKey",
                    userId = userId,
                    month = doc.getLong("month")?.toInt() ?: 7,
                    year = doc.getLong("year")?.toInt() ?: 2026,
                    monthKey = monthKey,
                    rentAmount = doc.getDouble("rentAmount") ?: 0.0,
                    currency = doc.getString("currency") ?: "৳",
                    paymentDate = doc.getString("paymentDate") ?: "",
                    comment = doc.getString("comment") ?: "",
                    status = doc.getString("status") ?: "Paid",
                    isLocked = doc.getBoolean("isLocked") ?: true,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.w(tag, "Failed to fetch rent records: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Stores a 6-digit password reset code in Firestore under users/{userId}/security/resetCode
     */
    suspend fun savePasswordResetCode(email: String, code: String): Result<Unit> {
        val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
        val userId = AuthUtils.emailToUserId(email)

        return try {
            val data = hashMapOf(
                "email" to email.trim().lowercase(),
                "resetCode" to code.trim(),
                "expiresAt" to (System.currentTimeMillis() + 15 * 60 * 1000L), // 15 minutes
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("users").document(userId)
                .collection("security").document("resetCode")
                .set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Failed to save reset code: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Updates password hash in Firestore and permanently deletes previous reset codes
     * and invalidates previous passwords.
     */
    suspend fun updatePasswordAndClearResetCode(email: String, newPassword: String): Result<Unit> {
        val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
        val userId = AuthUtils.emailToUserId(email)
        val newHash = AuthUtils.hashPassword(newPassword)

        return try {
            val updates = hashMapOf<String, Any>(
                "passwordHash" to newHash,
                "previousPasswordDeleted" to true,
                "passwordUpdatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(userId)
                .set(updates, SetOptions.merge()).await()

            // Delete the reset code document permanently so it cannot be reused
            try {
                db.collection("users").document(userId)
                    .collection("security").document("resetCode").delete().await()
            } catch (_: Exception) {}

            try {
                db.collection("resetCodes").document(email.trim().lowercase()).delete().await()
            } catch (_: Exception) {}

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Failed to update password and clear reset code: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Verifies the 6-digit code and resets the user's password in Firestore.
     * RULE: Once reset, previous password is permanently deleted and reset code is cleared.
     */
    suspend fun verifyAndResetPassword(email: String, code: String, newPassword: String): Result<Unit> {
        val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
        val userId = AuthUtils.emailToUserId(email)

        return try {
            // Check users/{userId}/security/resetCode first
            var resetDoc = db.collection("users").document(userId)
                .collection("security").document("resetCode").get().await()

            // Fallback to /resetCodes/{email} if not found in subcollection
            if (!resetDoc.exists()) {
                resetDoc = db.collection("resetCodes").document(email.trim().lowercase()).get().await()
            }

            if (!resetDoc.exists()) {
                return Result.failure(Exception("No active reset request found for this email. Please request a new code."))
            }

            val storedCode = resetDoc.getString("resetCode") ?: ""
            val expiresAt = resetDoc.getLong("expiresAt") ?: 0L

            if (System.currentTimeMillis() > expiresAt) {
                return Result.failure(Exception("Verification code has expired. Please request a new code."))
            }

            if (storedCode.trim() != code.trim()) {
                return Result.failure(Exception("Incorrect verification code. Please check the 6-digit code and try again."))
            }

            // Code matches! Update user password hash and delete previous password & reset code
            updatePasswordAndClearResetCode(email, newPassword)
        } catch (e: Exception) {
            Log.w(tag, "Password reset error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Deletes a rent record in Firestore after multi-step confirmation.
     */
    suspend fun deleteRentRecord(userId: String, monthKey: String): Result<Unit> {
        val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))

        return try {
            db.collection("users").document(userId)
                .collection("rentRecords").document(monthKey)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Failed to delete rent record ($monthKey): ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Deletes user data from Firestore on account deletion.
     */
    suspend fun deleteUserAccountData(userId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))

        return try {
            val recordsSnap = db.collection("users").document(userId).collection("rentRecords").get().await()
            for (doc in recordsSnap.documents) {
                doc.reference.delete().await()
            }
            db.collection("users").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Failed to delete user account data: ${e.message}")
            Result.failure(e)
        }
    }
}
