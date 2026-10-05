package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AppNotificationEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PropertyEntity
import com.example.data.local.entity.RecurringChargeEntity
import com.example.data.local.entity.RentMonthEntity
import com.example.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RentTrackerDao {

    // Properties
    @Query("SELECT * FROM properties ORDER BY createdAt DESC")
    fun getAllProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE status != 'Archived' ORDER BY createdAt DESC")
    fun getActiveProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE id = :id LIMIT 1")
    fun getPropertyById(id: String): Flow<PropertyEntity?>

    @Query("SELECT * FROM properties WHERE id = :id LIMIT 1")
    suspend fun getPropertyByIdSync(id: String): PropertyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity)

    @Update
    suspend fun updateProperty(property: PropertyEntity)

    @Delete
    suspend fun deleteProperty(property: PropertyEntity)

    // Recurring Charges
    @Query("SELECT * FROM recurring_charges WHERE propertyId = :propertyId")
    fun getRecurringCharges(propertyId: String): Flow<List<RecurringChargeEntity>>

    @Query("SELECT * FROM recurring_charges WHERE propertyId = :propertyId AND isActive = 1")
    suspend fun getActiveRecurringChargesSync(propertyId: String): List<RecurringChargeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringCharge(charge: RecurringChargeEntity)

    @Query("DELETE FROM recurring_charges WHERE id = :id")
    suspend fun deleteRecurringCharge(id: String)

    // Rent Months
    @Query("SELECT * FROM rent_months ORDER BY year DESC, month DESC")
    fun getAllRentMonths(): Flow<List<RentMonthEntity>>

    @Query("SELECT * FROM rent_months WHERE propertyId = :propertyId ORDER BY year DESC, month DESC")
    fun getRentMonthsForProperty(propertyId: String): Flow<List<RentMonthEntity>>

    @Query("SELECT * FROM rent_months WHERE id = :id LIMIT 1")
    fun getRentMonthById(id: String): Flow<RentMonthEntity?>

    @Query("SELECT * FROM rent_months WHERE id = :id LIMIT 1")
    suspend fun getRentMonthByIdSync(id: String): RentMonthEntity?

    @Query("SELECT * FROM rent_months WHERE propertyId = :propertyId AND year = :year AND month = :month LIMIT 1")
    suspend fun getRentMonthByPropertyAndPeriod(propertyId: String, year: Int, month: Int): RentMonthEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRentMonth(rentMonth: RentMonthEntity)

    @Update
    suspend fun updateRentMonth(rentMonth: RentMonthEntity)

    @Delete
    suspend fun deleteRentMonth(rentMonth: RentMonthEntity)

    // Payments
    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE rentMonthId = :rentMonthId ORDER BY paidDate DESC, createdAt DESC")
    fun getPaymentsForRentMonth(rentMonthId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE rentMonthId = :rentMonthId")
    suspend fun getPaymentsForRentMonthSync(rentMonthId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE propertyId = :propertyId ORDER BY paidDate DESC, createdAt DESC")
    fun getPaymentsForProperty(propertyId: String): Flow<List<PaymentEntity>>

    @Query("SELECT COUNT(*) FROM payments")
    suspend fun getPaymentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<AppNotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadNotificationsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // User Settings
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsSync(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: UserSettingsEntity)

    // Nuclear Data Deletion (as specified in requirement #34)
    @Query("DELETE FROM properties")
    suspend fun deleteAllProperties()

    @Query("DELETE FROM recurring_charges")
    suspend fun deleteAllRecurringCharges()

    @Query("DELETE FROM rent_months")
    suspend fun deleteAllRentMonths()

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAllAuditLogs()
}
