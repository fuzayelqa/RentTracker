package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PropertyEntity
import com.example.data.local.entity.RentMonthEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.ui.DashboardSummary
import com.example.ui.RentMonthWithProperty
import com.example.ui.components.RentStatusBadge
import com.example.ui.components.SummaryStatCard
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    properties: List<PropertyEntity>,
    userSettings: UserSettingsEntity,
    unreadNotifications: Int,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProperties: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToReports: () -> Unit,
    onOpenAddPayment: (RentMonthEntity) -> Unit,
    onOpenAddProperty: () -> Unit,
    onSeedDemoData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = userSettings.defaultCurrency
    val isBangla = userSettings.language == "BN"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBangla) "রেন্ট ট্র্যাকার" else "RentTracker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${if (isBangla) "স্বাগতম, " else "Welcome back, "}${userSettings.tenantName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier.testTag("dashboard_notifications_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(text = "$unreadNotifications")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("dashboard_scroll_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Empty state if no properties
            if (properties.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_properties_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddHome,
                                    contentDescription = "Add Home",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isBangla) "কোন প্রোপার্টি যোগ করা হয়নি" else "You haven't added a property yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBangla) "আপনার প্রথম বাসা বা ফ্ল্যাট যোগ করুন এবং মাসিক ভাড়া ট্র্যাক করুন।"
                                else "Add your apartment or rental home to start tracking monthly rent, recurring charges and payments.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = onOpenAddProperty,
                                    modifier = Modifier.testTag("first_add_property_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBangla) "বাসা যোগ করুন" else "Add Property")
                                }
                                OutlinedButton(
                                    onClick = onSeedDemoData,
                                    modifier = Modifier.testTag("load_demo_data_btn")
                                ) {
                                    Text(if (isBangla) "ডেমো ডেটা" else "Load Demo Data")
                                }
                            }
                        }
                    }
                }
            }

            // Important Alerts Section
            if (summary.overdueAlerts.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("overdue_alert_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Overdue Alert",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBangla) "বকেয়া ভাড়ার নোটিশ" else "Overdue Rent Alert",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB71C1C),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isBangla) "আপনার বকেয়া মোট: ${CurrencyUtils.format(summary.totalOverdue, currency)}"
                                    else "You have ${CurrencyUtils.format(summary.totalOverdue, currency)} in overdue rent.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFC62828)
                                )
                            }
                            OutlinedButton(
                                onClick = onNavigateToHistory,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                modifier = Modifier.testTag("view_overdue_history_btn")
                            ) {
                                Text("View", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Upcoming Alerts
            if (summary.upcomingAlerts.isNotEmpty()) {
                item {
                    val alert = summary.upcomingAlerts.first()
                    val days = DateUtils.getDaysUntilDue(alert.rentMonth.dueDate)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upcoming_due_alert_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Upcoming Due",
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBangla) "ভাড়া পরিশোধের তারিখ কাছাকাছি" else "Rent Due Soon",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${alert.property.name}: ${CurrencyUtils.format(alert.rentMonth.balance, alert.property.currency)} due in $days days.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFBF360C)
                                )
                            }
                            Button(
                                onClick = { onOpenAddPayment(alert.rentMonth) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                modifier = Modifier.testTag("pay_upcoming_due_btn")
                            ) {
                                Text("Pay", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Current Month Cards
            if (summary.currentMonthCards.isNotEmpty()) {
                item {
                    Text(
                        text = if (isBangla) "বর্তমান মাসের ভাড়া" else "Current Month Rent",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(summary.currentMonthCards, key = { it.rentMonth.id }) { itemData ->
                    val rentMonth = itemData.rentMonth
                    val prop = itemData.property
                    val propCurrency = prop.currency
                    val monthName = DateUtils.getMonthName(rentMonth.month, isBangla)
                    val daysUntilDue = DateUtils.getDaysUntilDue(rentMonth.dueDate)
                    val progress = if (rentMonth.totalDue > 0) (rentMonth.totalPaid / rentMonth.totalDue).toFloat().coerceIn(0f, 1f) else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("current_month_card_${prop.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            // Top Row: Property & Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prop.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$monthName ${rentMonth.year} • ${prop.address}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RentStatusBadge(status = rentMonth.status)
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Financial Breakdown Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Rent: ${CurrencyUtils.format(rentMonth.rentAmount, propCurrency)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (rentMonth.extras > 0) {
                                        Text("Extras: ${CurrencyUtils.format(rentMonth.extras, propCurrency)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Total Due: ${CurrencyUtils.format(rentMonth.totalDue, propCurrency)}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Remaining: ${CurrencyUtils.format(rentMonth.balance, propCurrency)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (rentMonth.balance > 0) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress Bar
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (progress >= 1f) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Due Date & Fast Action Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Due Date",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (daysUntilDue < 0) "Overdue by ${-daysUntilDue} days"
                                        else if (daysUntilDue == 0) "Due today!"
                                        else "Due in $daysUntilDue days (${DateUtils.formatDisplayDate(rentMonth.dueDate)})",
                                        fontSize = 12.sp,
                                        fontWeight = if (daysUntilDue <= 0 && rentMonth.balance > 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (daysUntilDue < 0 && rentMonth.balance > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = { onOpenAddPayment(rentMonth) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("record_payment_btn_${prop.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payment,
                                        contentDescription = "Record Payment",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isBangla) "ভাড়া দিন" else "Add Payment")
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions Horizontal Row
            item {
                Text(
                    text = if (isBangla) "দ্রুত কাজ" else "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickActionButton(
                            icon = Icons.Default.AddHome,
                            label = if (isBangla) "বাসা যোগ" else "Add Property",
                            onClick = onOpenAddProperty,
                            tag = "quick_action_add_property"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.History,
                            label = if (isBangla) "ইতিহাস" else "Rent History",
                            onClick = onNavigateToHistory,
                            tag = "quick_action_history"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Assessment,
                            label = if (isBangla) "রিপোর্ট" else "Reports",
                            onClick = onNavigateToReports,
                            tag = "quick_action_reports"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.HomeWork,
                            label = if (isBangla) "প্রোপার্টি" else "Properties",
                            onClick = onNavigateToProperties,
                            tag = "quick_action_properties"
                        )
                    }
                }
            }

            // Summary Statistics Grid
            item {
                Text(
                    text = if (isBangla) "বার্ষিক ও সামগ্রিক সারাংশ" else "Annual & Financial Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        title = if (isBangla) "চলতি বছরে পরিশোধ" else "Paid This Year",
                        value = CurrencyUtils.format(summary.totalPaidThisYear, currency),
                        icon = Icons.Default.AttachMoney,
                        iconBgColor = Color(0xFFD1FAE5),
                        iconTintColor = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = if (isBangla) "মোট বকেয়া" else "Outstanding",
                        value = CurrencyUtils.format(summary.totalOutstanding, currency),
                        icon = Icons.Default.HourglassTop,
                        iconBgColor = Color(0xFFFEF3C7),
                        iconTintColor = Color(0xFFD97706),
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
                        title = if (isBangla) "বকেয়া জরিমানা/দেরি" else "Total Overdue",
                        value = CurrencyUtils.format(summary.totalOverdue, currency),
                        icon = Icons.Default.Error,
                        iconBgColor = Color(0xFFFFE4E6),
                        iconTintColor = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = if (isBangla) "সক্রিয় প্রোপার্টি" else "Active Homes",
                        value = "${summary.activePropertiesCount} ${if (summary.activePropertiesCount == 1) "Property" else "Properties"}",
                        icon = Icons.Default.HomeWork,
                        iconBgColor = Color(0xFFF3E8FF),
                        iconTintColor = Color(0xFF7E22CE),
                        subtitle = if (summary.nextDueDate.isNotBlank()) "Next: ${DateUtils.formatDisplayDate(summary.nextDueDate)}" else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
