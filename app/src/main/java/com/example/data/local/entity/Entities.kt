package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Account entity for the logged-in user.
 * Section "ACCOUNT": User can create only one personal account, logged in via email.
 */
@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: String = "default_user",
    val email: String = "",
    val passwordHash: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Rent Record entity.
 * Section "DATA MODEL":
 * - id
 * - userId
 * - month
 * - year
 * - rentAmount
 * - paymentDate
 * - status (Advance Rent / Paid / Pending)
 * - isLocked (Permanently locked once saved)
 * - createdAt
 *
 * RULE: Each user can have only ONE rent record per month (enforced via unique index).
 */
@Entity(
    tableName = "rent_records",
    indices = [Index(value = ["userId", "monthKey"], unique = true)]
)
data class RentRecordEntity(
    @PrimaryKey val id: String, // Format: "${userId}_${monthKey}" - strictly unique
    val userId: String,
    val month: Int, // 1..12 (Starts from 7 for July 2026)
    val year: Int, // 2026 onwards
    val monthKey: String, // e.g. "2026-07", "2026-10"
    val rentAmount: Double,
    val currency: String = "৳",
    val paymentDate: String = "", // e.g. "01 July 2026"
    val comment: String = "", // Optional comment/note after payment date
    val status: String = "Paid", // "Advance Rent", "Paid", "Pending"
    val isLocked: Boolean = true, // IMPORTANT: Once saved, permanently locked!
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
