package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {

    fun getSymbol(currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            "BDT" -> "৳"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "INR" -> "₹"
            else -> currencyCode
        }
    }

    fun format(amount: Double, currencyCode: String = "BDT"): String {
        val symbol = getSymbol(currencyCode)
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            maximumFractionDigits = 2
        }
        return "$symbol ${formatter.format(amount)}"
    }
}
