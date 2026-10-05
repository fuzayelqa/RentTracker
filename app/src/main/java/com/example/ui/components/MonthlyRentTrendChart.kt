package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RentMonthEntity
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

data class MonthlyChartItem(
    val monthNumber: Int,
    val monthShortName: String,
    val totalDue: Double,
    val totalPaid: Double
)

@Composable
fun MonthlyRentTrendChart(
    rentMonths: List<RentMonthEntity>,
    year: Int,
    currency: String,
    modifier: Modifier = Modifier
) {
    val monthData = remember(rentMonths, year) {
        val shortNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val yearMonths = rentMonths.filter { it.year == year }.groupBy { it.month }

        (1..12).map { m ->
            val entries = yearMonths[m] ?: emptyList()
            MonthlyChartItem(
                monthNumber = m,
                monthShortName = shortNames[m - 1],
                totalDue = entries.sumOf { it.totalDue },
                totalPaid = entries.sumOf { it.totalPaid }
            )
        }
    }

    var selectedMonthIndex by remember { mutableIntStateOf(DateUtils.getCurrentMonth() - 1) }
    val selectedItem = monthData.getOrNull(selectedMonthIndex)

    val maxVal = remember(monthData) {
        val maxAmount = monthData.maxOfOrNull { maxOf(it.totalDue, it.totalPaid) } ?: 0.0
        if (maxAmount > 0.0) maxAmount else 1000.0
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_trend_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "12-Month Payment Trend ($year)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedItem != null) {
                        Text(
                            text = "${selectedItem.monthShortName}: Due ${CurrencyUtils.format(selectedItem.totalDue, currency)} | Paid ${CurrencyUtils.format(selectedItem.totalPaid, currency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFFCBD5E1), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Due", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF059669), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Paid", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Bar Chart
            val dueColor = Color(0xFFE2E8F0)
            val paidColor = Color(0xFF059669)
            val highlightColor = MaterialTheme.colorScheme.primaryContainer

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height - 24.dp.toPx()
                val columnWidth = canvasWidth / 12f
                val barWidth = columnWidth * 0.32f

                // Draw horizontal guide lines
                val steps = 3
                for (i in 0..steps) {
                    val y = canvasHeight * (i.toFloat() / steps)
                    drawLine(
                        color = Color(0x1F718096),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f
                    )
                }

                monthData.forEachIndexed { index, item ->
                    val xCenter = index * columnWidth + columnWidth / 2f
                    val dueHeight = ((item.totalDue / maxVal) * canvasHeight).toFloat().coerceIn(0f, canvasHeight)
                    val paidHeight = ((item.totalPaid / maxVal) * canvasHeight).toFloat().coerceIn(0f, canvasHeight)

                    // Draw Due Bar (left)
                    val dueX = xCenter - barWidth - 1f
                    val dueY = canvasHeight - dueHeight
                    drawRoundRect(
                        color = dueColor,
                        topLeft = Offset(dueX, dueY),
                        size = Size(barWidth, dueHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Draw Paid Bar (right)
                    val paidX = xCenter + 1f
                    val paidY = canvasHeight - paidHeight
                    drawRoundRect(
                        color = paidColor,
                        topLeft = Offset(paidX, paidY),
                        size = Size(barWidth, paidHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            // Month Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthData.forEachIndexed { idx, item ->
                    val isSelected = idx == selectedMonthIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMonthIndex = idx }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.monthShortName,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
