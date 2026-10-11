package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    const val START_YEAR = 2026
    const val START_MONTH = 7 // July 2026

    private val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun getMonthName(month: Int): String {
        val index = (month - 1).coerceIn(0, 11)
        return monthNames[index]
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

    /**
     * Formats monthKey as YYYY-MM (e.g. "2026-07", "2026-10")
     */
    fun formatMonthKey(year: Int, month: Int): String {
        return String.format(Locale.US, "%04d-%02d", year, month)
    }

    /**
     * Formats Month and Year (e.g. "July 2026", "October 2026")
     */
    fun formatMonthYear(year: Int, month: Int): String {
        return "${getMonthName(month)} $year"
    }

    /**
     * Formats payment date as "01 July 2026"
     */
    fun formatPaymentDate(day: Int, month: Int, year: Int): String {
        return String.format(Locale.US, "%02d %s %d", day, getMonthName(month), year)
    }

    /**
     * Current formatted date: e.g. "06 October 2026"
     */
    fun getCurrentDateFormatted(): String {
        return formatPaymentDate(getCurrentDay(), getCurrentMonth(), getCurrentYear())
    }

    /**
     * Formats creation timestamp to readable date: e.g. "06 October 2026"
     */
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.US)
        return sdf.format(Date(timestamp))
    }

    /**
     * Returns all chronological (year, month) pairs starting from July 2026 up to current month.
     * RULE: Advance months that have not come yet will not be added.
     * Once a calendar month arrives, that month becomes available in the chronological list.
     */
    fun getChronologicalMonthsFromJuly2026(upToYear: Int? = null, upToMonth: Int? = null): List<Pair<Int, Int>> {
        val list = mutableListOf<Pair<Int, Int>>()
        var curY = START_YEAR
        var curM = START_MONTH

        val curYear = getCurrentYear()
        val curMonth = getCurrentMonth()

        val targetYear = upToYear ?: maxOf(curYear, START_YEAR)
        val targetMonth = upToMonth ?: if (curYear < START_YEAR || (curYear == START_YEAR && curMonth < START_MONTH)) {
            START_MONTH // Minimum baseline is July 2026
        } else if (targetYear == curYear) {
            curMonth // Do not add future months that have not come yet
        } else {
            12
        }

        while (curY < targetYear || (curY == targetYear && curM <= targetMonth)) {
            list.add(Pair(curY, curM))
            curM++
            if (curM > 12) {
                curM = 1
                curY++
            }
        }
        if (list.isEmpty()) {
            list.add(Pair(START_YEAR, START_MONTH))
        }
        return list
    }

    /**
     * Checks if a month has not arrived yet (future month).
     * If the month has not come yet, it cannot be added until the month arrives.
     */
    fun isMonthNotComeYet(year: Int, month: Int): Boolean {
        val curYear = getCurrentYear()
        val curMonth = getCurrentMonth()
        // If current calendar time is before July 2026, July 2026 is always allowed as baseline
        if (year < START_YEAR || (year == START_YEAR && month <= START_MONTH)) {
            return false
        }
        return if (year > curYear) {
            true
        } else if (year == curYear) {
            month > curMonth
        } else {
            false
        }
    }

    /**
     * Checks if a month is before the earliest tracking start date (July 2026).
     * Users can start from July 2026 or any later month (August, November, December, 2027, etc.).
     */
    fun isBeforeStartMonth(year: Int, month: Int): Boolean {
        return year < START_YEAR || (year == START_YEAR && month < START_MONTH)
    }

    /**
     * Checks if this is the initial default start month (July 2026).
     */
    fun isStartMonth(year: Int, month: Int): Boolean {
        return year == START_YEAR && month == START_MONTH
    }

    /**
     * Checks if a given month/year is in a past month compared to current month/year.
     * RULE: If a record was updated/created in a past month, after that month ends the user cannot update or delete it.
     */
    fun isRecordInPastMonth(recordYear: Int, recordMonth: Int): Boolean {
        val curYear = getCurrentYear()
        val curMonth = getCurrentMonth()
        return if (recordYear < curYear) {
            true
        } else if (recordYear == curYear) {
            recordMonth < curMonth
        } else {
            false
        }
    }
}
