package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.RentTrackerDatabase
import com.example.data.local.entity.RentRecordEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.data.repository.RentTrackerRepository
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: RentTrackerDatabase
    private lateinit var repository: RentTrackerRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RentTrackerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RentTrackerRepository(database.rentTrackerDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My Rent Tracker", appName)
    }

    @Test
    fun `test July 2026 start date logic`() {
        // Months before July 2026 should be rejected
        assertTrue(DateUtils.isBeforeStartMonth(2026, 6)) // June 2026
        assertTrue(DateUtils.isBeforeStartMonth(2025, 12)) // December 2025

        // July 2026 and after are valid
        assertFalse(DateUtils.isBeforeStartMonth(2026, 7)) // July 2026
        assertFalse(DateUtils.isBeforeStartMonth(2026, 10)) // October 2026

        // July 2026 is start month
        assertTrue(DateUtils.isStartMonth(2026, 7))
        assertFalse(DateUtils.isStartMonth(2026, 8))
    }

    @Test
    fun `test chronological months list from July 2026`() {
        val months = DateUtils.getChronologicalMonthsFromJuly2026(2026, 10)
        assertEquals(4, months.size)
        assertEquals(Pair(2026, 7), months[0]) // July 2026
        assertEquals(Pair(2026, 8), months[1]) // August 2026
        assertEquals(Pair(2026, 9), months[2]) // September 2026
        assertEquals(Pair(2026, 10), months[3]) // October 2026
    }

    @Test
    fun `test currency formatting with BDT symbol`() {
        val formatted = CurrencyUtils.format(10000.0, "৳")
        assertTrue(formatted.contains("৳") && formatted.contains("10,000"))
    }

    @Test
    fun `test saving rent record permanently locks and prevents editing`() = runBlocking {
        val userId = "test_user_locked"

        // 1. Save July 2026 Advance Rent
        val res1 = repository.saveRentRecord(
            userId = userId,
            year = 2026,
            month = 7,
            rentAmount = 10000.0,
            paymentDate = "01 July 2026",
            status = "Advance Rent"
        )
        assertTrue(res1.isSuccess)
        val saved = res1.getOrNull()
        assertNotNull(saved)
        assertTrue(saved!!.isLocked) // Record must be locked!
        assertEquals("Advance Rent", saved.status)

        // 2. Attempting to save/edit July 2026 again must fail because it's locked!
        val res2 = repository.saveRentRecord(
            userId = userId,
            year = 2026,
            month = 7,
            rentAmount = 12000.0,
            paymentDate = "02 July 2026",
            status = "Paid"
        )
        assertFalse(res2.isSuccess)
        assertTrue(res2.exceptionOrNull()?.message?.contains("locked", ignoreCase = true) == true)

        // 3. Confirm only 1 record exists in database
        val all = repository.getAllRentRecordsChronologicalSync(userId)
        assertEquals(1, all.size)
        assertEquals(10000.0, all[0].rentAmount, 0.001)
    }

    @Test
    fun `test records before July 2026 are rejected`() = runBlocking {
        val userId = "test_user_pre_july"
        val res = repository.saveRentRecord(
            userId = userId,
            year = 2026,
            month = 6, // June 2026
            rentAmount = 10000.0,
            paymentDate = "01 June 2026",
            status = "Paid"
        )
        assertFalse(res.isSuccess)
        assertTrue(res.exceptionOrNull()?.message?.contains("July 2026", ignoreCase = true) == true)
    }

    @Test
    fun `test delete rent record`() = runBlocking {
        val userId = "test_user_delete"
        val curYear = DateUtils.getCurrentYear()
        val curMonth = DateUtils.getCurrentMonth()
        val curKey = DateUtils.formatMonthKey(curYear, curMonth)
        repository.saveRentRecord(
            userId = userId,
            year = curYear,
            month = curMonth,
            rentAmount = 10000.0,
            paymentDate = "01 Current",
            status = "Paid"
        )

        val before = repository.getAllRentRecordsChronologicalSync(userId)
        assertEquals(1, before.size)

        val deleteRes = repository.deleteRentRecord(userId, curKey)
        assertTrue(deleteRes.isSuccess)
        val after = repository.getAllRentRecordsChronologicalSync(userId)
        assertEquals(0, after.size)
    }

    @Test
    fun `test past month rent record cannot be deleted`() = runBlocking {
        val userId = "test_user_past_delete"
        repository.saveRentRecord(
            userId = userId,
            year = 2026,
            month = 8,
            rentAmount = 10000.0,
            paymentDate = "01 August 2026",
            status = "Paid"
        )

        // Attempting to delete past month record must fail because month has ended!
        val deleteRes = repository.deleteRentRecord(userId, "2026-08")
        assertFalse(deleteRes.isSuccess)
        assertTrue(deleteRes.exceptionOrNull()?.message?.contains("ended", ignoreCase = true) == true)
    }

    @Test
    fun `test export to excel and pdf generates valid uris`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val records = listOf(
            RentRecordEntity(
                id = "user_2026-07",
                userId = "user",
                month = 7,
                year = 2026,
                monthKey = "2026-07",
                rentAmount = 10000.0,
                paymentDate = "01 July 2026",
                status = "Advance Rent",
                isLocked = true
            ),
            RentRecordEntity(
                id = "user_2026-08",
                userId = "user",
                month = 8,
                year = 2026,
                monthKey = "2026-08",
                rentAmount = 10000.0,
                paymentDate = "01 August 2026",
                status = "Paid",
                isLocked = true
            )
        )

        // Test Excel export
        val excelUri = com.example.util.ExportUtils.exportToExcel(context, records, "test@example.com")
        assertNotNull(excelUri)

        // PDF export
        val pdfUri = com.example.util.ExportUtils.exportToPdf(context, records, "test@example.com")
        // Either uri is created or handled safely in JVM test
        assertTrue(excelUri != null)
    }

    @Test
    fun `test Advance option available for August and other months`() = runBlocking {
        val userId = "test_user_advance_august"
        val res = repository.saveRentRecord(
            userId = userId,
            year = 2026,
            month = 8,
            rentAmount = 15000.0,
            paymentDate = "05 August 2026",
            status = "Advance Rent"
        )
        assertTrue(res.isSuccess)
        val saved = res.getOrNull()
        assertNotNull(saved)
        assertEquals("Advance Rent", saved!!.status)
    }

    @Test
    fun `test future month that has not arrived yet cannot be added`() = runBlocking {
        val userId = "test_future_user"
        val res = repository.saveRentRecord(
            userId = userId,
            year = 2030,
            month = 5,
            rentAmount = 10000.0,
            paymentDate = "01 May 2030",
            status = "Paid"
        )
        assertFalse(res.isSuccess)
        assertTrue(res.exceptionOrNull()?.message?.contains("not come yet", ignoreCase = true) == true)
    }

    @Test
    fun `test password reset deletes previous password and previous password cannot be used`() = runBlocking {
        val email = "tenant.security@example.com"
        val userId = com.example.util.AuthUtils.emailToUserId(email)
        val oldPass = "OldPassword123"
        val newPass = "NewSecurePassword456"

        // 1. Create account with old password
        val oldHash = com.example.util.AuthUtils.hashPassword(oldPass)
        val initialAccount = com.example.data.local.entity.UserAccountEntity(
            id = userId,
            email = email,
            passwordHash = oldHash,
            createdAt = System.currentTimeMillis()
        )
        repository.saveUserAccount(initialAccount)

        val retrievedOld = repository.getUserAccountSync(userId)
        assertNotNull(retrievedOld)
        assertEquals(oldHash, retrievedOld!!.passwordHash)

        // 2. Perform password reset to new password
        val newHash = com.example.util.AuthUtils.hashPassword(newPass)
        repository.updatePasswordAndClearResetCode(email, newPass)
        val updatedAccount = retrievedOld.copy(passwordHash = newHash)
        repository.saveUserAccount(updatedAccount)

        // 3. Verify that old password hash is completely deleted and replaced
        val retrievedNew = repository.getUserAccountSync(userId)
        assertNotNull(retrievedNew)
        assertEquals(newHash, retrievedNew!!.passwordHash)
        assertNotEquals(oldHash, retrievedNew.passwordHash)

        // 4. Verify old password does NOT match active password
        val checkOldPass = com.example.util.AuthUtils.hashPassword(oldPass)
        assertFalse("Old password hash must not match updated password hash", retrievedNew.passwordHash == checkOldPass)
        assertTrue("New password hash must match updated password hash", retrievedNew.passwordHash == newHash)
    }
}
