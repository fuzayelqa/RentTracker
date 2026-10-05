package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PropertyEntity
import com.example.data.local.entity.RentMonthEntity
import com.example.ui.components.MonthlyRentTrendChart
import com.example.ui.components.RentStatusBadge
import com.example.ui.components.SummaryStatCard
import com.example.util.CsvExporter
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    rentMonths: List<RentMonthEntity>,
    properties: List<PropertyEntity>,
    payments: List<PaymentEntity>,
    defaultCurrency: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentYear = DateUtils.getCurrentYear()
    var selectedYear by remember { mutableIntStateOf(currentYear) }

    val propMap = remember(properties) { properties.associateBy { it.id } }
    val yearRentMonths = remember(rentMonths, selectedYear) {
        rentMonths.filter { it.year == selectedYear }
    }

    // Calculations
    val totalDue = yearRentMonths.sumOf { it.totalDue }
    val totalPaid = yearRentMonths.sumOf { it.totalPaid }
    val totalOutstanding = yearRentMonths.sumOf { it.balance.coerceAtLeast(0.0) }
    val totalOverdue = yearRentMonths.filter { it.status == "OVERDUE" }.sumOf { it.balance.coerceAtLeast(0.0) }
    val paidMonthsCount = yearRentMonths.count { it.status == "PAID" || it.status == "OVERPAID" }
    val partialMonthsCount = yearRentMonths.count { it.status == "PARTIALLY_PAID" }
    val overdueMonthsCount = yearRentMonths.count { it.status == "OVERDUE" }

    val paymentEntriesForYear = remember(payments, yearRentMonths) {
        val monthIds = yearRentMonths.map { it.id }.toSet()
        payments.filter { it.rentMonthId in monthIds }
    }
    val avgPayment = if (paymentEntriesForYear.isNotEmpty()) {
        paymentEntriesForYear.sumOf { it.amount } / paymentEntriesForYear.size
    } else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reports & Analytics",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            val csvData = CsvExporter.generateCsv(propMap, rentMonths, payments)
                            CsvExporter.shareCsv(context, csvData, "RentTracker_Export_$selectedYear.csv")
                        },
                        modifier = Modifier.testTag("export_csv_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export CSV")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("reports_scroll_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Year Selector Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Report Year",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(currentYear - 1, currentYear, currentYear + 1).forEach { yr ->
                            FilterChip(
                                selected = selectedYear == yr,
                                onClick = { selectedYear = yr },
                                label = { Text("$yr", fontSize = 12.sp) },
                                modifier = Modifier.testTag("year_chip_$yr")
                            )
                        }
                    }
                }
            }

            // Summary Card Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        title = "Total Rent Due",
                        value = CurrencyUtils.format(totalDue, defaultCurrency),
                        icon = Icons.Default.Assessment,
                        iconBgColor = Color(0xFFF3E8FF),
                        iconTintColor = Color(0xFF7E22CE),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "Total Paid",
                        value = CurrencyUtils.format(totalPaid, defaultCurrency),
                        icon = Icons.Default.CheckCircle,
                        iconBgColor = Color(0xFFECFDF5),
                        iconTintColor = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        title = "Outstanding",
                        value = CurrencyUtils.format(totalOutstanding, defaultCurrency),
                        icon = Icons.Default.HourglassTop,
                        iconBgColor = Color(0xFFFEF3C7),
                        iconTintColor = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "Overdue",
                        value = CurrencyUtils.format(totalOverdue, defaultCurrency),
                        icon = Icons.Default.Error,
                        iconBgColor = Color(0xFFFFE4E6),
                        iconTintColor = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Status Month Counters Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        CountMetric(label = "Paid Months", count = paidMonthsCount, color = Color(0xFF10B981))
                        CountMetric(label = "Partial", count = partialMonthsCount, color = Color(0xFFF59E0B))
                        CountMetric(label = "Overdue", count = overdueMonthsCount, color = Color(0xFFEF4444))
                        CountMetric(label = "Avg Payment", value = CurrencyUtils.format(avgPayment, defaultCurrency), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // 12-Month Bar Chart
            item {
                MonthlyRentTrendChart(
                    rentMonths = rentMonths,
                    year = selectedYear,
                    currency = defaultCurrency
                )
            }

            // CSV Export Quick Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Records to CSV",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Download or share all properties, rent months, and payment transactions",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                val csvData = CsvExporter.generateCsv(propMap, rentMonths, payments)
                                CsvExporter.shareCsv(context, csvData, "RentTracker_Export.csv")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("export_csv_action_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Export", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export")
                        }
                    }
                }
            }

            // Month by Month Breakdown Header
            item {
                Text(
                    text = "Monthly Breakdown ($selectedYear)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (yearRentMonths.isEmpty()) {
                item {
                    Text(
                        text = "No rent records recorded for year $selectedYear",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(yearRentMonths.sortedByDescending { it.month }, key = { it.id }) { month ->
                    val prop = propMap[month.propertyId]
                    val curr = prop?.currency ?: defaultCurrency
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${DateUtils.getMonthName(month.month)} $selectedYear",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = prop?.name ?: "Unknown Property",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Paid: ${CurrencyUtils.format(month.totalPaid, curr)}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    RentStatusBadge(status = month.status)
                                }
                                Text(
                                    text = "Due: ${CurrencyUtils.format(month.totalDue, curr)} | Bal: ${CurrencyUtils.format(month.balance, curr)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun CountMetric(
    label: String,
    count: Int? = null,
    value: String? = null,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value ?: count.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
