package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.RentRecordEntity
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

object ExportUtils {

    /**
     * Generates a CSV file (compatible directly with Microsoft Excel, Google Sheets, LibreOffice).
     */
    fun exportToExcel(
        context: Context,
        records: List<RentRecordEntity>,
        userEmail: String
    ): Uri? {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "MyRentTracker_Records.csv")
            val writer = FileWriter(file)

            // UTF-8 BOM for Microsoft Excel to correctly display currency symbols like ৳
            writer.write("\uFEFF")

            // Title / Metadata header
            writer.write("My Rent Tracker - Monthly House Rent Statement (v2.0)\n")
            writer.write("Account:,\"$userEmail\"\n")
            writer.write("Export Date:,\"${DateUtils.getCurrentDateFormatted()}\"\n")
            writer.write("Tracking Start:,\"July 2026\"\n\n")

            // Table Header
            writer.write("Month & Year,Year,Month,Rent Amount,Currency,Payment Date,Status,Record Locked,Recorded At\n")

            // Data rows
            for (rec in records) {
                val monthTitle = DateUtils.formatMonthYear(rec.year, rec.month)
                val recordedAt = DateUtils.formatTimestamp(rec.createdAt)
                val lockedStr = if (rec.isLocked) "Locked" else "Editable"
                writer.write("\"$monthTitle\",${rec.year},${rec.month},${rec.rentAmount},\"${rec.currency}\",\"${rec.paymentDate}\",\"${rec.status}\",\"$lockedStr\",\"$recordedAt\"\n")
            }

            // Summary row
            val totalAmount = records.sumOf { it.rentAmount }
            writer.write("\n\"Total Rent Paid / Recorded: \",,,,${totalAmount},\"৳\",,,\n")

            writer.flush()
            writer.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates an official, beautifully styled PDF document using Android's native PdfDocument.
     * Styled with Yellow & Navy Blue branding.
     */
    fun exportToPdf(
        context: Context,
        records: List<RentRecordEntity>,
        userEmail: String
    ): Uri? {
        return try {
            val pdfDoc = PdfDocument()
            // Standard A4 dimensions: 595 x 842 points
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }

            // 1. Header Banner in Sunflower Yellow (#FACC15)
            paint.color = Color.rgb(250, 204, 21) // Yellow #FACC15
            canvas.drawRect(0f, 0f, 595f, 100f, paint)

            // Header Bottom Navy Line
            paint.color = Color.rgb(15, 23, 42) // Deep Navy #0F172A
            canvas.drawRect(0f, 96f, 595f, 100f, paint)

            // App Title in Bold Midnight Navy
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 24f
            canvas.drawText("My Rent Tracker", 36f, 44f, paint)

            // Subtitle
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Monthly House Rent Statement (v2.0) • Starts July 2026", 36f, 66f, paint)
            canvas.drawText("Account: $userEmail  |  Date: ${DateUtils.getCurrentDateFormatted()}", 36f, 84f, paint)

            // 2. Table Header in Navy Blue
            var currentY = 130f
            paint.color = Color.rgb(15, 23, 42) // Deep Navy #0F172A
            canvas.drawRect(36f, currentY - 18f, 559f, currentY + 10f, paint)

            paint.color = Color.rgb(250, 204, 21) // Yellow text on navy header
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            canvas.drawText("MONTH & YEAR", 46f, currentY, paint)
            canvas.drawText("AMOUNT", 200f, currentY, paint)
            canvas.drawText("PAYMENT DATE", 300f, currentY, paint)
            canvas.drawText("STATUS", 420f, currentY, paint)
            canvas.drawText("LOCK", 510f, currentY, paint)

            currentY += 24f

            // 3. Table Rows
            paint.textSize = 11f
            val totalAmount = records.sumOf { it.rentAmount }

            for ((index, rec) in records.withIndex()) {
                // Alternating row background: light yellow tint
                if (index % 2 == 0) {
                    paint.color = Color.rgb(254, 249, 195) // Very light yellow #FEF9C3
                    canvas.drawRect(36f, currentY - 14f, 559f, currentY + 8f, paint)
                }

                // Row border bottom
                paint.color = Color.rgb(226, 232, 240)
                canvas.drawLine(36f, currentY + 8f, 559f, currentY + 8f, paint)

                // Month
                paint.color = Color.rgb(15, 23, 42)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val monthText = DateUtils.formatMonthYear(rec.year, rec.month)
                canvas.drawText(monthText, 46f, currentY, paint)

                // Amount
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val amountText = CurrencyUtils.format(rec.rentAmount, rec.currency)
                canvas.drawText(amountText, 200f, currentY, paint)

                // Payment Date
                canvas.drawText(rec.paymentDate.ifBlank { "Recorded" }, 300f, currentY, paint)

                // Status
                if (rec.status.contains("Advance", ignoreCase = true)) {
                    paint.color = Color.rgb(30, 58, 138) // Royal Navy
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                } else if (rec.status.contains("Paid", ignoreCase = true)) {
                    paint.color = Color.rgb(22, 163, 74) // Green
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                } else {
                    paint.color = Color.rgb(217, 119, 6) // Amber
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(rec.status, 420f, currentY, paint)

                // Lock icon text
                paint.color = Color.rgb(71, 85, 105)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(if (rec.isLocked) "Locked" else "-", 510f, currentY, paint)

                currentY += 24f

                if (currentY > 740f) break // Fit single page cleanly
            }

            // 4. Summary Box at bottom
            currentY = maxOf(currentY + 16f, 660f)
            paint.color = Color.rgb(250, 204, 21) // Yellow Box #FACC15
            canvas.drawRect(36f, currentY, 559f, currentY + 60f, paint)

            paint.color = Color.rgb(15, 23, 42) // Navy border
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRect(36f, currentY, 559f, currentY + 60f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 13f
            canvas.drawText("Summary:", 50f, currentY + 24f, paint)
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Total Records: ${records.size} months", 50f, currentY + 44f, paint)

            val totalStr = CurrencyUtils.format(totalAmount, "৳")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Total House Rent: $totalStr", 340f, currentY + 34f, paint)

            // Footer
            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Generated by My Rent Tracker v2.0 • Data is permanently locked and protected.", 36f, 800f, paint)

            pdfDoc.finishPage(page)

            // Save PDF file
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "MyRentTracker_Statement.pdf")
            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Triggers the Android system share sheet to share or open the exported Excel or PDF file.
     */
    fun shareExportedFile(
        context: Context,
        uri: Uri,
        mimeType: String,
        chooserTitle: String
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
