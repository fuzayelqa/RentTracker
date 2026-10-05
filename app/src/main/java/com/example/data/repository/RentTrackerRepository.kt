package com.example.data.repository

import com.example.data.local.dao.RentTrackerDao
import com.example.data.local.entity.AppNotificationEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PropertyEntity
import com.example.data.local.entity.RecurringChargeEntity
import com.example.data.local.entity.RentMonthEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.ReceiptGenerator
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RentTrackerRepository(private val dao: RentTrackerDao) {

    val allProperties: Flow<List<PropertyEntity>> = dao.getAllProperties()
    val activeProperties: Flow<List<PropertyEntity>> = dao.getActiveProperties()
    val allRentMonths: Flow<List<RentMonthEntity>> = dao.getAllRentMonths()
    val allPayments: Flow<List<PaymentEntity>> = dao.getAllPayments()
    val allNotifications: Flow<List<AppNotificationEntity>> = dao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = dao.getUnreadNotificationsCount()
    val allAuditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()
    val userSettings: Flow<UserSettingsEntity?> = dao.getUserSettings()

    fun getPropertyById(id: String): Flow<PropertyEntity?> = dao.getPropertyById(id)
    fun getRecurringCharges(propertyId: String): Flow<List<RecurringChargeEntity>> = dao.getRecurringCharges(propertyId)
    fun getRentMonthsForProperty(propertyId: String): Flow<List<RentMonthEntity>> = dao.getRentMonthsForProperty(propertyId)
    fun getRentMonthById(id: String): Flow<RentMonthEntity?> = dao.getRentMonthById(id)
    fun getPaymentsForRentMonth(rentMonthId: String): Flow<List<PaymentEntity>> = dao.getPaymentsForRentMonth(rentMonthId)
    fun getPaymentsForProperty(propertyId: String): Flow<List<PaymentEntity>> = dao.getPaymentsForProperty(propertyId)

    suspend fun getUserSettingsSync(): UserSettingsEntity {
        return dao.getUserSettingsSync() ?: UserSettingsEntity().also {
            dao.insertOrUpdateSettings(it)
        }
    }

    suspend fun saveUserSettings(settings: UserSettingsEntity) {
        dao.insertOrUpdateSettings(settings)
    }

    suspend fun addProperty(property: PropertyEntity, defaultCharges: List<Pair<String, Double>> = emptyList()) {
        dao.insertProperty(property)
        for ((name, amt) in defaultCharges) {
            dao.insertRecurringCharge(
                RecurringChargeEntity(
                    propertyId = property.id,
                    name = name,
                    amount = amt,
                    type = "Fixed",
                    isActive = true
                )
            )
        }
        dao.insertAuditLog(
            AuditLogEntity(
                action = "CREATE_PROPERTY",
                entityType = "Property",
                entityId = property.id,
                description = "Added property '${property.name}' at ${property.city}",
                newValue = property.name
            )
        )
        // Automatically generate rent month for current month if property is active
        val year = DateUtils.getCurrentYear()
        val month = DateUtils.getCurrentMonth()
        generateMonthlyRentForProperty(property.id, year, month)
    }

    suspend fun updateProperty(property: PropertyEntity) {
        val existing = dao.getPropertyByIdSync(property.id)
        dao.updateProperty(property)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "UPDATE_PROPERTY",
                entityType = "Property",
                entityId = property.id,
                description = "Updated property '${property.name}'",
                previousValue = existing?.monthlyRent.toString(),
                newValue = property.monthlyRent.toString()
            )
        )
    }

    suspend fun archiveProperty(propertyId: String) {
        val prop = dao.getPropertyByIdSync(propertyId) ?: return
        val updated = prop.copy(status = "Archived", updatedAt = System.currentTimeMillis())
        dao.updateProperty(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "ARCHIVE_PROPERTY",
                entityType = "Property",
                entityId = propertyId,
                description = "Archived property '${prop.name}'",
                previousValue = prop.status,
                newValue = "Archived"
            )
        )
    }

    suspend fun deleteProperty(propertyId: String) {
        val prop = dao.getPropertyByIdSync(propertyId) ?: return
        dao.deleteProperty(prop)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "DELETE_PROPERTY",
                entityType = "Property",
                entityId = propertyId,
                description = "Deleted property '${prop.name}'",
                previousValue = prop.name,
                newValue = ""
            )
        )
    }

    suspend fun addRecurringCharge(charge: RecurringChargeEntity) {
        dao.insertRecurringCharge(charge)
    }

    suspend fun deleteRecurringCharge(id: String) {
        dao.deleteRecurringCharge(id)
    }

    suspend fun generateMonthlyRentForProperty(propertyId: String, year: Int, month: Int): RentMonthEntity? {
        val existing = dao.getRentMonthByPropertyAndPeriod(propertyId, year, month)
        if (existing != null) return existing

        val prop = dao.getPropertyByIdSync(propertyId) ?: return null
        if (prop.status == "Archived") return null

        val charges = dao.getActiveRecurringChargesSync(propertyId)
        val extrasSum = charges.sumOf { it.amount }
        val dueDate = DateUtils.calculateDueDate(year, month, prop.dueDay)
        val totalDue = prop.monthlyRent + extrasSum
        val balance = totalDue
        val status = DateUtils.computeRentStatus(totalDue, 0.0, dueDate)

        val rentMonth = RentMonthEntity(
            propertyId = propertyId,
            year = year,
            month = month,
            rentAmount = prop.monthlyRent,
            extras = extrasSum,
            totalDue = totalDue,
            totalPaid = 0.0,
            balance = balance,
            dueDate = dueDate,
            status = status
        )
        dao.insertRentMonth(rentMonth)
        return rentMonth
    }

    suspend fun recordPayment(
        propertyId: String,
        rentMonthId: String,
        amount: Double,
        paidDate: String,
        method: String,
        reference: String,
        note: String,
        receiptUri: String = ""
    ) {
        val count = dao.getPaymentCount()
        val year = DateUtils.getCurrentYear()
        val receiptNumber = ReceiptGenerator.generateReceiptNumber(count, year)

        val payment = PaymentEntity(
            propertyId = propertyId,
            rentMonthId = rentMonthId,
            receiptNumber = receiptNumber,
            amount = amount,
            paidDate = paidDate,
            method = method,
            reference = reference,
            receiptUri = receiptUri,
            note = note
        )
        dao.insertPayment(payment)

        // Recalculate rent month totals
        val rentMonth = dao.getRentMonthByIdSync(rentMonthId)
        if (rentMonth != null) {
            val allPayments = dao.getPaymentsForRentMonthSync(rentMonthId)
            val totalPaid = allPayments.sumOf { it.amount }
            val balance = rentMonth.totalDue - totalPaid
            val newStatus = DateUtils.computeRentStatus(rentMonth.totalDue, totalPaid, rentMonth.dueDate)

            val updatedMonth = rentMonth.copy(
                totalPaid = totalPaid,
                balance = balance,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateRentMonth(updatedMonth)

            // Audit Log
            dao.insertAuditLog(
                AuditLogEntity(
                    action = "CREATE_PAYMENT",
                    entityType = "Payment",
                    entityId = payment.id,
                    description = "Recorded payment of ${payment.amount} via ${payment.method} ($receiptNumber)",
                    newValue = payment.amount.toString()
                )
            )

            // In-app Notification
            val prop = dao.getPropertyByIdSync(propertyId)
            val currency = prop?.currency ?: "BDT"
            dao.insertNotification(
                AppNotificationEntity(
                    type = "PAYMENT_CONFIRMED",
                    title = "Payment Recorded",
                    message = "Payment of ${CurrencyUtils.format(amount, currency)} recorded successfully for ${prop?.name ?: "property"}.",
                    relatedPropertyId = propertyId,
                    relatedRentMonthId = rentMonthId
                )
            )
        }
    }

    suspend fun deletePayment(payment: PaymentEntity) {
        dao.deletePayment(payment)
        val rentMonth = dao.getRentMonthByIdSync(payment.rentMonthId)
        if (rentMonth != null) {
            val remainingPayments = dao.getPaymentsForRentMonthSync(payment.rentMonthId)
            val totalPaid = remainingPayments.sumOf { it.amount }
            val balance = rentMonth.totalDue - totalPaid
            val newStatus = DateUtils.computeRentStatus(rentMonth.totalDue, totalPaid, rentMonth.dueDate)

            dao.updateRentMonth(
                rentMonth.copy(
                    totalPaid = totalPaid,
                    balance = balance,
                    status = newStatus,
                    updatedAt = System.currentTimeMillis()
                )
            )

            dao.insertAuditLog(
                AuditLogEntity(
                    action = "DELETE_PAYMENT",
                    entityType = "Payment",
                    entityId = payment.id,
                    description = "Deleted payment of ${payment.amount} (${payment.receiptNumber})",
                    previousValue = payment.amount.toString(),
                    newValue = "0"
                )
            )
        }
    }

    suspend fun markNotificationAsRead(id: String) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        dao.markAllNotificationsAsRead()
    }

    suspend fun clearAllNotifications() {
        dao.clearAllNotifications()
    }

    suspend fun checkAndRefreshStatusAndReminders() {
        val months = dao.getAllRentMonths()
        // Synchronously check and refresh statuses if dates changed
    }

    suspend fun clearAllUserData() {
        dao.deleteAllPayments()
        dao.deleteAllRentMonths()
        dao.deleteAllRecurringCharges()
        dao.deleteAllProperties()
        dao.deleteAllAuditLogs()
        dao.clearAllNotifications()
    }

    suspend fun seedSampleData() {
        val existingProps = dao.getActiveProperties()
        // If already has properties, don't duplicate
        val prop1 = PropertyEntity(
            name = "Greenview Apartment 4B",
            address = "Road 12, Dhanmondi",
            city = "Dhaka",
            country = "Bangladesh",
            propertyType = "Apartment",
            landlordName = "Mr. Rafiqul Islam",
            landlordPhone = "+880 1711-234567",
            landlordEmail = "rafiqul.islam@email.com",
            landlordAddress = "House 8, Dhanmondi, Dhaka",
            landlordNotes = "Bank account: DBBL 102.120.45892",
            monthlyRent = 25000.0,
            currency = "BDT",
            dueDay = 5,
            tenancyStart = "2025-01-01",
            tenancyEnd = "2026-12-31",
            securityDeposit = 50000.0,
            advancePayment = 25000.0
        )
        dao.insertProperty(prop1)

        val charges = listOf(
            RecurringChargeEntity(propertyId = prop1.id, name = "Service Charge", amount = 3000.0),
            RecurringChargeEntity(propertyId = prop1.id, name = "Gas Bill", amount = 1080.0),
            RecurringChargeEntity(propertyId = prop1.id, name = "Water Bill", amount = 800.0)
        )
        for (c in charges) {
            dao.insertRecurringCharge(c)
        }

        val prop2 = PropertyEntity(
            name = "Sylhet Lakeside Suite",
            address = "Subidbazar",
            city = "Sylhet",
            country = "Bangladesh",
            propertyType = "House",
            landlordName = "Mrs. Nazma Begum",
            landlordPhone = "+880 1819-987654",
            landlordEmail = "nazma.sylhet@email.com",
            monthlyRent = 18000.0,
            currency = "BDT",
            dueDay = 10,
            tenancyStart = "2025-06-01",
            tenancyEnd = "2027-05-31",
            securityDeposit = 36000.0
        )
        dao.insertProperty(prop2)
        dao.insertRecurringCharge(RecurringChargeEntity(propertyId = prop2.id, name = "Security Guard", amount = 1500.0))

        // Create sample months & payments for prop 1
        val currentYear = DateUtils.getCurrentYear()
        val currentMonth = DateUtils.getCurrentMonth()

        // Current Month
        val curMonthDue = 25000.0 + 4880.0
        val monthCur = RentMonthEntity(
            propertyId = prop1.id,
            year = currentYear,
            month = currentMonth,
            rentAmount = 25000.0,
            extras = 4880.0,
            totalDue = curMonthDue,
            totalPaid = 15000.0,
            balance = curMonthDue - 15000.0,
            dueDate = DateUtils.calculateDueDate(currentYear, currentMonth, 5),
            status = "PARTIALLY_PAID"
        )
        dao.insertRentMonth(monthCur)
        dao.insertPayment(
            PaymentEntity(
                propertyId = prop1.id,
                rentMonthId = monthCur.id,
                receiptNumber = "RT-$currentYear-000101",
                amount = 15000.0,
                paidDate = DateUtils.getCurrentDateString(),
                method = "bKash",
                reference = "TrxID: 9BK459201",
                note = "Partial payment (Part 1)"
            )
        )

        // Previous Month (Paid)
        val prevMonth = if (currentMonth > 1) currentMonth - 1 else 12
        val prevYear = if (currentMonth > 1) currentYear else currentYear - 1
        val monthPrev = RentMonthEntity(
            propertyId = prop1.id,
            year = prevYear,
            month = prevMonth,
            rentAmount = 25000.0,
            extras = 4880.0,
            totalDue = curMonthDue,
            totalPaid = curMonthDue,
            balance = 0.0,
            dueDate = DateUtils.calculateDueDate(prevYear, prevMonth, 5),
            status = "PAID"
        )
        dao.insertRentMonth(monthPrev)
        dao.insertPayment(
            PaymentEntity(
                propertyId = prop1.id,
                rentMonthId = monthPrev.id,
                receiptNumber = "RT-$prevYear-000095",
                amount = curMonthDue,
                paidDate = "$prevYear-${String.format("%02d", prevMonth)}-03",
                method = "Bank Transfer",
                reference = "Ref: DBBL-TXN-8821",
                note = "Full rent paid via online banking"
            )
        )

        // Notifications
        dao.insertNotification(
            AppNotificationEntity(
                type = "DUE_REMINDER",
                title = "Rent Payment Due",
                message = "Greenview Apartment 4B has a remaining balance of ৳ 14,880.",
                relatedPropertyId = prop1.id,
                relatedRentMonthId = monthCur.id
            )
        )
        dao.insertNotification(
            AppNotificationEntity(
                type = "LEASE_EXPIRING",
                title = "Lease Expiry Reminder",
                message = "Lease for Greenview Apartment 4B expires in 85 days.",
                relatedPropertyId = prop1.id
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                action = "SYSTEM_INITIALIZE",
                entityType = "System",
                entityId = "init",
                description = "Initialized RentTracker demo data with 2 properties and historical payments."
            )
        )
    }
}
