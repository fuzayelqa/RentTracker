package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val englishMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val banglaMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    fun getMonthName(month: Int, isBangla: Boolean = false): String {
        val index = (month - 1).coerceIn(0, 11)
        return if (isBangla) banglaMonths[index] else englishMonths[index]
    }

    fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    fun getCurrentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH) + 1
    }

    fun getCurrentDay(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsed = sdf.parse(dateStr) ?: return dateStr
            val out = SimpleDateFormat("dd MMM yyyy", Locale.US)
            out.format(parsed)
        } catch (_: Exception) {
            dateStr
        }
    }

    /**
     * Calculates the due date string (yyyy-MM-dd) handling varying month lengths
     * (e.g. Feb 28/29, Apr 30, Jan 31).
     */
    fun calculateDueDate(year: Int, month: Int, dueDay: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val validDay = dueDay.coerceIn(1, maxDays)
        cal.set(Calendar.DAY_OF_MONTH, validDay)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    /**
     * Returns true if today is strictly after the given due date string.
     */
    fun isOverdue(dueDateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val due = sdf.parse(dueDateStr) ?: return false
            val todayStr = sdf.format(Date())
            val today = sdf.parse(todayStr) ?: return false
            today.after(due)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Computes the number of days remaining until dueDate, or negative if overdue.
     */
    fun getDaysUntilDue(dueDateStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val due = sdf.parse(dueDateStr) ?: return 0
            val todayStr = sdf.format(Date())
            val today = sdf.parse(todayStr) ?: return 0
            val diffMs = due.time - today.time
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (_: Exception) {
            0
        }
    }

    /**
     * Calculates payment status according to exact business rules:
     * - totalPaid == 0 -> if overdue: OVERDUE else UNPAID
     * - 0 < totalPaid < totalDue -> if overdue: OVERDUE else PARTIALLY_PAID
     * - totalPaid == totalDue -> PAID
     * - totalPaid > totalDue -> OVERPAID
     */
    fun computeRentStatus(totalDue: Double, totalPaid: Double, dueDateStr: String): String {
        val overdue = isOverdue(dueDateStr)
        return when {
            totalPaid >= totalDue && totalDue > 0 -> {
                if (totalPaid > totalDue) "OVERPAID" else "PAID"
            }
            totalPaid <= 0.0 -> {
                if (overdue) "OVERDUE" else "UNPAID"
            }
            totalPaid < totalDue -> {
                if (overdue) "OVERDUE" else "PARTIALLY_PAID"
            }
            else -> "PAID"
        }
    }
}
