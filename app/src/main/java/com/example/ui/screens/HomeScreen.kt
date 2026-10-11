package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RentRecordEntity
import com.example.ui.components.Animated3DSection
import com.example.ui.components.Button3D
import com.example.ui.components.Card3D
import com.example.ui.theme.*
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.ExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentMonthRecord: RentRecordEntity?,
    allRecords: List<RentRecordEntity>,
    userEmail: String = "",
    onAddRecord: (year: Int, month: Int, amount: Double, date: String, comment: String, status: String) -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentYear = DateUtils.getCurrentYear()
    val currentMonth = DateUtils.getCurrentMonth()
    val currentMonthTitle = DateUtils.formatMonthYear(currentYear, currentMonth)

    var showAddDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_root"),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // App Header: "My Rent Tracker" with Version 2.0 Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "My Rent Tracker",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = DarkNavyText,
                                modifier = Modifier.testTag("home_header_title")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // v2.1 Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavyPrimary)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "v2.1",
                                    color = YellowAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Text(
                            text = "Tracking monthly house rent starting July 2026",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkSlateText,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Top Action: Export / Download Icon Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary)
                            .border(2.dp, YellowBorder, CircleShape)
                            .clickable { showExportDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Report",
                            tint = YellowAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // MAIN CARD: "Current Month's Rent" with 3D Tactile Card and Floating Animation
            item {
                Animated3DSection(delayMillis = 100) {
                    Card3D(
                        shape = RoundedCornerShape(22.dp),
                        containerColor = YellowCardSurface,
                        borderColor = NavyBorder,
                        borderWidth = 2.dp,
                        depth = 6.dp,
                        enableFloating = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                            .testTag("current_month_card")
                    ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Current Month's Rent",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NavySecondary
                                )
                                Text(
                                    text = currentMonthTitle,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DarkNavyText,
                                    modifier = Modifier.testTag("current_month_title")
                                )
                            }

                            if (currentMonthRecord != null) {
                                StatusPill(status = currentMonthRecord.status)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(YellowBackground)
                                        .border(1.dp, SubtleBorder, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "Not Recorded",
                                        color = DarkNavyText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (currentMonthRecord != null) {
                            // Current month rent details
                            Text(
                                text = "Rent Amount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkSlateText
                            )
                            Text(
                                text = CurrencyUtils.format(currentMonthRecord.rentAmount, currentMonthRecord.currency),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = DarkNavyText,
                                modifier = Modifier.testTag("current_month_amount")
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = SubtleBorder)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Payment Date",
                                        fontSize = 12.sp,
                                        color = DarkSlateText
                                    )
                                    Text(
                                        text = currentMonthRecord.paymentDate.ifBlank { "Recorded" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkNavyText,
                                        modifier = Modifier.testTag("current_month_payment_date")
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Payment Status",
                                        fontSize = 12.sp,
                                        color = DarkSlateText
                                    )
                                    Text(
                                        text = currentMonthRecord.status,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            currentMonthRecord.status.contains("Advance", ignoreCase = true) -> AdvanceNavyText
                                            currentMonthRecord.status.contains("Paid", ignoreCase = true) -> SuccessGreen
                                            else -> PendingAmber
                                        }
                                    )
                                }
                            }

                            // Optional comment after payment date
                            if (currentMonthRecord.comment.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Comment: ${currentMonthRecord.comment}",
                                    fontSize = 13.sp,
                                    color = DarkNavyText,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.testTag("current_month_comment")
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Locked Status Banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LockedBadgeBg)
                                    .border(1.dp, LockedBadgeBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = LockedBadgeText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Record Locked",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = LockedBadgeText
                                    )
                                    Text(
                                        text = "This rent record cannot be edited after saving.",
                                        fontSize = 11.sp,
                                        color = DarkSlateText
                                    )
                                }
                            }
                        } else {
                            // Current month has no record yet
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = "No rent record saved for $currentMonthTitle yet.",
                                    fontSize = 14.sp,
                                    color = DarkSlateText
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { showAddDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NavyPrimary,
                                        contentColor = YellowAccent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("add_current_rent_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = YellowAccent, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Add Rent Record",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = YellowAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

            // Quick Action Buttons: Add Record | Download Excel/PDF | Rent History
            item {
                Animated3DSection(delayMillis = 150) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Button 1: Add Record
                        Button3D(
                            onClick = { showAddDialog = true },
                            backgroundColor = NavyPrimary,
                            borderColor = NavyDarkest,
                            depth = 4.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("quick_add_record_btn")
                        ) {
                            Row(
                                modifier = Modifier.align(Alignment.Center),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = YellowAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = YellowAccent)
                            }
                        }

                        // Button 2: Download Report (Excel/PDF)
                        Button3D(
                            onClick = { showExportDialog = true },
                            backgroundColor = YellowDeep,
                            borderColor = NavyPrimary,
                            shadowColor = NavyDarkest.copy(alpha = 0.8f),
                            depth = 4.dp,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(50.dp)
                                .testTag("download_report_btn")
                        ) {
                            Row(
                                modifier = Modifier.align(Alignment.Center),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = DarkNavyText, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export / PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkNavyText)
                            }
                        }

                        // Button 3: Rent History
                        Button3D(
                            onClick = onNavigateToHistory,
                            backgroundColor = YellowCardElevated,
                            borderColor = NavyPrimary,
                            shadowColor = NavyDarkest.copy(alpha = 0.8f),
                            depth = 4.dp,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp)
                                .testTag("view_rent_history_btn")
                        ) {
                            Row(
                                modifier = Modifier.align(Alignment.Center),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "History", tint = DarkNavyText, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("History", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkNavyText)
                            }
                        }
                    }
                }
            }

            // Previous Rent History Navigation Card (Home focuses solely on Current Month)
            item {
                Animated3DSection(delayMillis = 180) {
                    Card3D(
                        shape = RoundedCornerShape(18.dp),
                        containerColor = YellowCardSurface,
                        borderColor = NavyBorder,
                        borderWidth = 1.6.dp,
                        depth = 5.dp,
                        onClick = onNavigateToHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("home_view_history_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(NavyPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = "History",
                                        tint = YellowAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Previous Rent History",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = DarkNavyText
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "View chronological monthly records starting July 2026",
                                        fontSize = 12.sp,
                                        color = DarkSlateText
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "View History",
                                tint = NavyPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ADD RENT RECORD DIALOG
    if (showAddDialog) {
        AddRentRecordDialog(
            existingRecords = allRecords,
            onDismiss = { showAddDialog = false },
            onConfirm = { year, month, amount, date, comment, status ->
                onAddRecord(year, month, amount, date, comment, status)
                showAddDialog = false
            }
        )
    }

    // DOWNLOAD / EXPORT DIALOG (Excel & PDF)
    if (showExportDialog) {
        ExportReportDialog(
            records = allRecords,
            userEmail = userEmail,
            onDismiss = { showExportDialog = false },
            onExportExcel = {
                if (allRecords.isEmpty()) {
                    Toast.makeText(context, "No rent records to export yet.", Toast.LENGTH_SHORT).show()
                } else {
                    val uri = ExportUtils.exportToExcel(context, allRecords, userEmail)
                    if (uri != null) {
                        ExportUtils.shareExportedFile(context, uri, "text/csv", "Share Rent Records Excel (.csv)")
                        showExportDialog = false
                    } else {
                        Toast.makeText(context, "Failed to generate Excel file.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onExportPdf = {
                if (allRecords.isEmpty()) {
                    Toast.makeText(context, "No rent records to export yet.", Toast.LENGTH_SHORT).show()
                } else {
                    val uri = ExportUtils.exportToPdf(context, allRecords, userEmail)
                    if (uri != null) {
                        ExportUtils.shareExportedFile(context, uri, "application/pdf", "Share Rent Statement PDF (.pdf)")
                        showExportDialog = false
                    } else {
                        Toast.makeText(context, "Failed to generate PDF.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

/**
 * EXPORT REPORT DIALOG: Choose Excel (.csv) or PDF (.pdf) download
 */
@Composable
fun ExportReportDialog(
    records: List<RentRecordEntity>,
    userEmail: String,
    onDismiss: () -> Unit,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download Rent Records",
                    fontWeight = FontWeight.Bold,
                    color = DarkNavyText
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Export your ${records.size} rent record(s) starting from July 2026. Choose your preferred format:",
                    fontSize = 13.sp,
                    color = DarkSlateText
                )

                // Option 1: Download as Excel (.csv)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = YellowCardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NavyBorder, RoundedCornerShape(14.dp))
                        .clickable(onClick = onExportExcel)
                        .testTag("export_excel_option")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.TableChart, contentDescription = "Excel", tint = OnNavyPrimary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Excel Spreadsheet (.csv)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkNavyText)
                            Text("Opens directly in Microsoft Excel, Sheets", fontSize = 11.sp, color = DarkSlateText)
                        }
                    }
                }

                // Option 2: Download as PDF (.pdf)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = YellowCardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NavyBorder, RoundedCornerShape(14.dp))
                        .clickable(onClick = onExportPdf)
                        .testTag("export_pdf_option")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AlertRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = OnNavyPrimary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Printable PDF Document (.pdf)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkNavyText)
                            Text("Official statement with summary & details", fontSize = 11.sp, color = DarkSlateText)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DarkSlateText, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRentRecordDialog(
    existingRecords: List<RentRecordEntity>,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int, amount: Double, date: String, comment: String, status: String) -> Unit
) {
    // Generate chronological months starting from July 2026 up to current arrived month
    val chronologicalMonths = remember {
        DateUtils.getChronologicalMonthsFromJuly2026()
    }

    // Default to the first unrecorded month (e.g. July 2026 if empty, or current month)
    val recordedKeys = existingRecords.map { it.monthKey }.toSet()
    val allRecorded = chronologicalMonths.isNotEmpty() && chronologicalMonths.all { (y, m) ->
        recordedKeys.contains(DateUtils.formatMonthKey(y, m))
    }
    val firstUnrecorded = chronologicalMonths.firstOrNull { (y, m) ->
        !recordedKeys.contains(DateUtils.formatMonthKey(y, m))
    } ?: chronologicalMonths.lastOrNull() ?: Pair(DateUtils.getCurrentYear(), DateUtils.getCurrentMonth())

    var selectedYear by remember { mutableIntStateOf(firstUnrecorded.first) }
    var selectedMonth by remember { mutableIntStateOf(firstUnrecorded.second) }
    var rentAmountText by remember { mutableStateOf("10000") }
    var paymentDate by remember { mutableStateOf(DateUtils.getCurrentDateFormatted()) }
    var commentText by remember { mutableStateOf("") }

    var selectedStatus by remember {
        mutableStateOf(if (DateUtils.isStartMonth(firstUnrecorded.first, firstUnrecorded.second)) "Advance Rent" else "Paid")
    }

    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Rent Record",
                fontWeight = FontWeight.Bold,
                color = DarkNavyText
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (allRecorded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(YellowCardSurface)
                            .border(1.dp, YellowBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All months from July 2026 up to ${DateUtils.formatMonthYear(DateUtils.getCurrentYear(), DateUtils.getCurrentMonth())} are already recorded. The next month will automatically become available once it arrives.",
                            fontSize = 11.sp,
                            color = DarkNavyText,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Month Selector
                Column {
                    Text(
                        text = "Month & Year (Starts July 2026)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Future months cannot be added until that month arrives.",
                        fontSize = 11.sp,
                        color = DarkSlateText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = monthDropdownExpanded,
                        onExpandedChange = { monthDropdownExpanded = !monthDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${DateUtils.formatMonthYear(selectedYear, selectedMonth)}${if (DateUtils.isStartMonth(selectedYear, selectedMonth)) " (Start Month)" else ""}",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = NavyPrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("add_month_dropdown"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = NavyBorder
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false }
                        ) {
                            chronologicalMonths.forEach { (y, m) ->
                                val mKey = DateUtils.formatMonthKey(y, m)
                                val isAlreadyRecorded = recordedKeys.contains(mKey)
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${DateUtils.formatMonthYear(y, m)}${if (isAlreadyRecorded) " (Already Recorded)" else ""}",
                                            fontWeight = if (isAlreadyRecorded) FontWeight.Normal else FontWeight.Bold,
                                            color = if (isAlreadyRecorded) DarkSlateText else DarkNavyText
                                        )
                                    },
                                    enabled = !isAlreadyRecorded,
                                    onClick = {
                                        selectedYear = y
                                        selectedMonth = m
                                        monthDropdownExpanded = false
                                        errorText = null
                                    }
                                )
                            }
                        }
                    }
                }

                // Rent Amount Field
                Column {
                    Text(
                        text = "Rent Amount (৳)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = rentAmountText,
                        onValueChange = {
                            rentAmountText = it.filter { c -> c.isDigit() || c == '.' }
                            errorText = null
                        },
                        placeholder = { Text("10000") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = NavyPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = NavyBorder
                        )
                    )
                }

                // Payment Date Field
                Column {
                    Text(
                        text = "Payment Date",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = paymentDate,
                        onValueChange = {
                            paymentDate = it
                            errorText = null
                        },
                        placeholder = { Text("01 July 2026") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_payment_date_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = NavyBorder
                        )
                    )
                }

                // Comment Field (After Payment Date)
                Column {
                    Text(
                        text = "Comment (Optional Note)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("e.g. Paid via bank transfer, advance for lease") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_comment_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = NavyBorder
                        )
                    )
                }

                // Payment Status Dropdown
                Column {
                    Text(
                        text = "Payment Status",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val statusOptions = listOf("Paid", "Pending", "Advance Rent")

                    ExposedDropdownMenuBox(
                        expanded = statusDropdownExpanded,
                        onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStatus,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("add_status_dropdown"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = NavyBorder
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            statusOptions.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        selectedStatus = st
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Permanent Lock Rule Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LockedBadgeBg)
                        .border(1.dp, LockedBadgeBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Lock",
                        tint = LockedBadgeText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notice: Once saved, this record is permanently locked and cannot be edited.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = LockedBadgeText
                    )
                }

                if (errorText != null) {
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            val isSelectedMonthRecorded = recordedKeys.contains(DateUtils.formatMonthKey(selectedYear, selectedMonth))
            Button(
                onClick = {
                    if (isSelectedMonthRecorded) {
                        errorText = "${DateUtils.formatMonthYear(selectedYear, selectedMonth)} is already recorded."
                        return@Button
                    }
                    val amount = rentAmountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0) {
                        errorText = "Please enter a valid positive rent amount."
                        return@Button
                    }
                    if (paymentDate.isBlank()) {
                        errorText = "Please enter a payment date."
                        return@Button
                    }
                    onConfirm(selectedYear, selectedMonth, amount, paymentDate.trim(), commentText.trim(), selectedStatus)
                },
                enabled = !isSelectedMonthRecorded,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = YellowAccent,
                    disabledContainerColor = SubtleBorder,
                    disabledContentColor = DarkSlateText
                ),
                modifier = Modifier.testTag("save_and_lock_record_btn")
            ) {
                Text(
                    text = if (isSelectedMonthRecorded) "Already Recorded" else "Save & Lock Record",
                    fontWeight = FontWeight.Bold,
                    color = if (isSelectedMonthRecorded) DarkSlateText else YellowAccent
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DarkSlateText)
            }
        }
    )
}

@Composable
fun StatusPill(status: String) {
    val (bgColor, textColor, label) = when {
        status.contains("Advance", ignoreCase = true) -> Triple(AdvanceNavyBg, AdvanceNavyText, "Advance Rent")
        status.contains("Paid", ignoreCase = true) -> Triple(SuccessGreenBg, SuccessGreenText, "Paid")
        else -> Triple(PendingAmberBg, PendingAmberText, "Pending")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RentRecordCard(
    record: RentRecordEntity,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card3D(
        shape = RoundedCornerShape(16.dp),
        containerColor = YellowCardSurface,
        borderColor = NavyBorder,
        borderWidth = 1.2.dp,
        depth = 4.dp,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("rent_record_${record.monthKey}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = DateUtils.formatMonthYear(record.year, record.month),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavyText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (record.paymentDate.isNotBlank()) "Payment Date: ${record.paymentDate}" else "Recorded",
                    fontSize = 12.sp,
                    color = DarkSlateText
                )
                if (record.comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Note: ${record.comment}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = NavySecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = DarkSlateText,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Record Locked",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkSlateText
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.format(record.rentAmount, record.currency),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkNavyText
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatusPill(status = record.status)
            }
        }
    }
}
