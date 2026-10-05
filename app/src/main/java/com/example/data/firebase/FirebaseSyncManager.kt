package com.example.data.firebase

import android.util.Log
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PropertyEntity
import com.example.data.local.entity.RentMonthEntity
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

    fun getCurrentUser(): FirebaseUser? {
        return auth?.currentUser
    }

    fun isConfigured(): Boolean {
        return auth != null && firestore != null
    }

    suspend fun syncLocalToCloud(
        properties: List<PropertyEntity>,
        rentMonths: List<RentMonthEntity>,
        payments: List<PaymentEntity>
    ): Result<Int> {
        val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
        val user = auth?.currentUser
        val ownerUid = user?.uid ?: "local_tenant"

        return try {
            var syncedCount = 0

            // Sync properties
            for (prop in properties) {
                val data = hashMapOf(
                    "id" to prop.id,
                    "ownerUid" to ownerUid,
                    "name" to prop.name,
                    "address" to prop.address,
                    "city" to prop.city,
                    "country" to prop.country,
                    "propertyType" to prop.propertyType,
                    "landlordName" to prop.landlordName,
                    "landlordPhone" to prop.landlordPhone,
                    "landlordEmail" to prop.landlordEmail,
                    "monthlyRent" to prop.monthlyRent,
                    "currency" to prop.currency,
                    "dueDay" to prop.dueDay,
                    "status" to prop.status,
                    "updatedAt" to prop.updatedAt
                )
                db.collection("users").document(ownerUid)
                    .collection("properties").document(prop.id)
                    .set(data, SetOptions.merge()).await()
                syncedCount++
            }

            // Sync rent months
            for (month in rentMonths) {
                val data = hashMapOf(
                    "id" to month.id,
                    "ownerUid" to ownerUid,
                    "propertyId" to month.propertyId,
                    "year" to month.year,
                    "month" to month.month,
                    "rentAmount" to month.rentAmount,
                    "extras" to month.extras,
                    "totalDue" to month.totalDue,
                    "totalPaid" to month.totalPaid,
                    "balance" to month.balance,
                    "dueDate" to month.dueDate,
                    "status" to month.status,
                    "updatedAt" to month.updatedAt
                )
                db.collection("users").document(ownerUid)
                    .collection("rentMonths").document(month.id)
                    .set(data, SetOptions.merge()).await()
                syncedCount++
            }

            // Sync payments
            for (pay in payments) {
                val data = hashMapOf(
                    "id" to pay.id,
                    "ownerUid" to ownerUid,
                    "propertyId" to pay.propertyId,
                    "rentMonthId" to pay.rentMonthId,
                    "receiptNumber" to pay.receiptNumber,
                    "amount" to pay.amount,
                    "paidDate" to pay.paidDate,
                    "method" to pay.method,
                    "reference" to pay.reference,
                    "note" to pay.note,
                    "createdAt" to pay.createdAt
                )
                db.collection("users").document(ownerUid)
                    .collection("payments").document(pay.id)
                    .set(data, SetOptions.merge()).await()
                syncedCount++
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
}
