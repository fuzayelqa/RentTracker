package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.ReceiptGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RentTracker", appName)
    }

    @Test
    fun `test payment status calculation scenarios from PRD`() {
        val futureDueDate = "2099-12-31"

        // Scenario 1: Due 10,000, Paid 0 -> UNPAID
        assertEquals("UNPAID", DateUtils.computeRentStatus(10000.0, 0.0, futureDueDate))

        // Scenario 2: Due 10,000, Paid 5,000 -> PARTIALLY_PAID
        assertEquals("PARTIALLY_PAID", DateUtils.computeRentStatus(10000.0, 5000.0, futureDueDate))

        // Scenario 3: Due 10,000, Paid 10,000 -> PAID
        assertEquals("PAID", DateUtils.computeRentStatus(10000.0, 10000.0, futureDueDate))

        // Scenario 4: Due 10,000, Paid 12,000 -> OVERPAID (Paid with credit)
        assertEquals("OVERPAID", DateUtils.computeRentStatus(10000.0, 12000.0, futureDueDate))

        // Scenario 5: Past due date, Paid 0 -> OVERDUE
        val pastDueDate = "2020-01-01"
        assertEquals("OVERDUE", DateUtils.computeRentStatus(10000.0, 0.0, pastDueDate))
    }

    @Test
    fun `test due day clamp calculation for varying month lengths`() {
        // February clamp (e.g. day 31 in Feb 2026 -> 2026-02-28)
        val febDate = DateUtils.calculateDueDate(2026, 2, 31)
        assertEquals("2026-02-28", febDate)

        // April clamp (day 31 in April -> 2026-04-30)
        val aprDate = DateUtils.calculateDueDate(2026, 4, 31)
        assertEquals("2026-04-30", aprDate)

        // January has 31 days
        val janDate = DateUtils.calculateDueDate(2026, 1, 31)
        assertEquals("2026-01-31", janDate)
    }

    @Test
    fun `test receipt number formatting`() {
        val receiptNumber = ReceiptGenerator.generateReceiptNumber(0, 2026)
        assertEquals("RT-2026-000001", receiptNumber)

        val receiptNumber123 = ReceiptGenerator.generateReceiptNumber(122, 2026)
        assertEquals("RT-2026-000123", receiptNumber123)
    }

    @Test
    fun `test currency formatting`() {
        val bdt = CurrencyUtils.format(18000.0, "BDT")
        assertTrue(bdt.contains("৳"))
        assertTrue(bdt.contains("18,000"))

        val usd = CurrencyUtils.format(1500.50, "USD")
        assertTrue(usd.contains("$"))
        assertTrue(usd.contains("1,500.5"))
    }
}
