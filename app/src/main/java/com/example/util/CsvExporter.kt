package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PropertyEntity
import com.example.data.local.entity.RentMonthEntity

object CsvExporter {

    fun generateCsv(
        properties: Map<String, PropertyEntity>,
        rentMonths: List<RentMonthEntity>,
        payments: List<PaymentEntity>
    ): String {
        val paymentsByMonth = payments.groupBy { it.rentMonthId }
        val sb = StringBuilder()
        sb.append("Property,Month,Year,Due Date,Rent,Extras,Discount,Total Due,Total Paid,Balance,Status,Payment Count,Payment Methods,References\n")

        for (month in rentMonths) {
            val prop = properties[month.propertyId]
            val propName = prop?.name ?: "Unknown Property"
            val monthName = DateUtils.getMonthName(month.month)
            val monthPayments = paymentsByMonth[month.id] ?: emptyList()
            val methods = monthPayments.joinToString(" | ") { it.method }
            val references = monthPayments.joinToString(" | ") { it.reference.ifBlank { "N/A" } }

            sb.append("\"$propName\",")
            sb.append("\"$monthName\",")
            sb.append("${month.year},")
            sb.append("\"${month.dueDate}\",")
            sb.append("${month.rentAmount},")
            sb.append("${month.extras},")
            sb.append("${month.discount},")
            sb.append("${month.totalDue},")
            sb.append("${month.totalPaid},")
            sb.append("${month.balance},")
            sb.append("\"${month.status}\",")
            sb.append("${monthPayments.size},")
            sb.append("\"$methods\",")
            sb.append("\"$references\"\n")
        }
        return sb.toString()
    }

    fun shareCsv(context: Context, csvData: String, title: String = "RentTracker_Export.csv") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csvData)
            putExtra(Intent.EXTRA_SUBJECT, title)
            type = "text/csv"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Rent Records")
        context.startActivity(shareIntent)
    }

    fun shareText(context: Context, text: String, subject: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, subject)
        context.startActivity(shareIntent)
    }
}
