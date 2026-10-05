package com.example.util

import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PropertyEntity
import com.example.data.local.entity.RentMonthEntity
import java.util.Locale

object ReceiptGenerator {

    fun generateReceiptNumber(sequence: Int, year: Int): String {
        return String.format(Locale.US, "RT-%d-%06d", year, sequence + 1)
    }

    fun buildReceiptText(
        payment: PaymentEntity,
        rentMonth: RentMonthEntity,
        property: PropertyEntity,
        tenantName: String
    ): String {
        val currency = property.currency
        val monthName = DateUtils.getMonthName(rentMonth.month)
        return """
            ====================================================
                        RENT PAYMENT RECEIPT
            ====================================================
            Receipt Number: ${payment.receiptNumber.ifBlank { "RT-OFFICIAL" }}
            Date:           ${payment.paidDate}
            Status:         COMPLETED
            ----------------------------------------------------
            TENANT & PROPERTY DETAILS:
            Tenant:         $tenantName
            Property:       ${property.name}
            Address:        ${property.address}, ${property.city}
            Landlord:       ${property.landlordName.ifBlank { "N/A" }}
            Landlord Phone: ${property.landlordPhone.ifBlank { "N/A" }}
            ----------------------------------------------------
            RENTAL PERIOD:
            Month & Year:   $monthName ${rentMonth.year}
            Rent Amount:    ${CurrencyUtils.format(rentMonth.rentAmount, currency)}
            Utility/Extras: ${CurrencyUtils.format(rentMonth.extras, currency)}
            Total Due:      ${CurrencyUtils.format(rentMonth.totalDue, currency)}
            ----------------------------------------------------
            PAYMENT TRANSACTION:
            Amount Paid:    ${CurrencyUtils.format(payment.amount, currency)}
            Payment Method: ${payment.method}
            Reference / Trx: ${payment.reference.ifBlank { "N/A" }}
            Note:           ${payment.note.ifBlank { "None" }}
            Remaining Bal:  ${CurrencyUtils.format(rentMonth.balance, currency)}
            ----------------------------------------------------
            Authorized Digital Record - RentTracker
            Thank you for your prompt payment!
            ====================================================
        """.trimIndent()
    }
}
