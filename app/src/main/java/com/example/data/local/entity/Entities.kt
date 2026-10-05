package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val city: String,
    val country: String = "Bangladesh",
    val propertyType: String = "Apartment", // Apartment, House, Room, Office, Other
    val landlordName: String = "",
    val landlordPhone: String = "",
    val landlordEmail: String = "",
    val landlordAddress: String = "",
    val landlordNotes: String = "",
    val monthlyRent: Double = 0.0,
    val currency: String = "BDT", // BDT, USD, EUR, GBP, INR
    val dueDay: Int = 5, // 1 to 31
    val tenancyStart: String = "", // YYYY-MM-DD
    val tenancyEnd: String = "",
    val securityDeposit: Double = 0.0,
    val advancePayment: Double = 0.0,
    val rentIncreaseDate: String = "",
    val futureRentAmount: Double = 0.0,
    val status: String = "Active", // Active, Vacated, Archived
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recurring_charges")
data class RecurringChargeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val name: String, // Service Charge, Gas, Water, Electricity, Internet, Security, Parking
    val amount: Double = 0.0,
    val type: String = "Fixed", // Fixed, Variable
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "rent_months")
data class RentMonthEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val year: Int,
    val month: Int, // 1 to 12
    val rentAmount: Double = 0.0,
    val extras: Double = 0.0,
    val lateFee: Double = 0.0,
    val previousBalance: Double = 0.0,
    val discount: Double = 0.0,
    val totalDue: Double = 0.0,
    val totalPaid: Double = 0.0,
    val balance: Double = 0.0,
    val dueDate: String = "", // YYYY-MM-DD
    val status: String = "UNPAID", // UNPAID, PARTIALLY_PAID, PAID, OVERDUE, OVERPAID
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val rentMonthId: String,
    val receiptNumber: String = "",
    val amount: Double = 0.0,
    val paidDate: String = "", // YYYY-MM-DD
    val method: String = "Cash", // Cash, bKash, Nagad, Bank Transfer, Cheque, Card, Other
    val reference: String = "",
    val receiptUri: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class AppNotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String, // DUE_REMINDER, OVERDUE, PAYMENT_CONFIRMED, LEASE_EXPIRING, SYSTEM
    val title: String,
    val message: String,
    val relatedPropertyId: String? = null,
    val relatedRentMonthId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val action: String, // CREATE_PAYMENT, UPDATE_PAYMENT, DELETE_PAYMENT, CREATE_PROPERTY, UPDATE_PROPERTY, ARCHIVE_PROPERTY, RENT_CHANGED
    val entityType: String, // Property, Payment, RentMonth
    val entityId: String,
    val description: String,
    val previousValue: String = "",
    val newValue: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val tenantName: String = "Tenant",
    val email: String = "user@renttracker.app",
    val phone: String = "",
    val defaultCurrency: String = "BDT",
    val language: String = "EN", // EN or BN
    val pinEnabled: Boolean = false,
    val pinCode: String = "", // 4-digit PIN
    val sessionTimeoutMinutes: Int = 15,
    val reminderSevenDays: Boolean = true,
    val reminderThreeDays: Boolean = true,
    val reminderDueDay: Boolean = true,
    val reminderOverdue: Boolean = true,
    val reminderLease: Boolean = true,
    val paymentConfirmation: Boolean = true
)
