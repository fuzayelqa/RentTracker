package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PropertyEntity
import com.example.data.local.entity.RentMonthEntity
import com.example.ui.components.RentStatusBadge
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    rentMonths: List<RentMonthEntity>,
    properties: List<PropertyEntity>,
    payments: List<PaymentEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    selectedPropertyId: String?,
    onPropertyFilterChange: (String?) -> Unit,
    onOpenAddPayment: (RentMonthEntity) -> Unit,
    onViewReceipt: (PaymentEntity) -> Unit,
    onDeletePayment: (PaymentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val propMap = remember(properties) { properties.associateBy { it.id } }
    val paymentsByMonth = remember(payments) { payments.groupBy { it.rentMonthId } }

    val filteredMonths = remember(rentMonths, propMap, paymentsByMonth, searchQuery, statusFilter, selectedPropertyId) {
        rentMonths.filter { month ->
            val prop = propMap[month.propertyId]
            val propName = prop?.name.orEmpty()
            val monthPayments = paymentsByMonth[month.id] ?: emptyList()

            // Property filter
            val matchesProperty = selectedPropertyId == null || month.propertyId == selectedPropertyId

            // Status filter
            val matchesStatus = when (statusFilter) {
                "ALL" -> true
                else -> month.status.equals(statusFilter, ignoreCase = true)
            }

            // Search query
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                propName.lowercase().contains(q) ||
                        month.notes.lowercase().contains(q) ||
                        monthPayments.any { p ->
                            p.reference.lowercase().contains(q) ||
                                    p.note.lowercase().contains(q) ||
                                    p.receiptNumber.lowercase().contains(q) ||
                                    p.method.lowercase().contains(q)
                        }
            }

            matchesProperty && matchesStatus && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rent Ledger & History",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search property, TrxID, note, reference...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("history_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Status Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    "ALL" to "All",
                    "UNPAID" to "Unpaid",
                    "PARTIALLY_PAID" to "Partial",
                    "PAID" to "Paid",
                    "OVERDUE" to "Overdue"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = statusFilter == key,
                        onClick = { onStatusFilterChange(key) },
                        label = { Text(label, fontSize = 12.sp) },
                        modifier = Modifier.testTag("status_filter_$key")
                    )
                }
            }

            // Property Filter Chips
            if (properties.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedPropertyId == null,
                            onClick = { onPropertyFilterChange(null) },
                            label = { Text("All Properties", fontSize = 11.sp) }
                        )
                    }
                    items(properties) { prop ->
                        FilterChip(
                            selected = selectedPropertyId == prop.id,
                            onClick = { onPropertyFilterChange(prop.id) },
                            label = { Text(prop.name, fontSize = 11.sp) }
                        )
                    }
                }
            }

            if (filteredMonths.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Empty History",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No rent records match your filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMonths, key = { it.id }) { month ->
                        val prop = propMap[month.propertyId]
                        val monthPayments = paymentsByMonth[month.id] ?: emptyList()

                        HistoryMonthCard(
                            month = month,
                            property = prop,
                            payments = monthPayments,
                            onAddPayment = { onOpenAddPayment(month) },
                            onViewReceipt = onViewReceipt,
                            onDeletePayment = onDeletePayment
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryMonthCard(
    month: RentMonthEntity,
    property: PropertyEntity?,
    payments: List<PaymentEntity>,
    onAddPayment: () -> Unit,
    onViewReceipt: (PaymentEntity) -> Unit,
    onDeletePayment: (PaymentEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currency = property?.currency ?: "BDT"
    val monthName = DateUtils.getMonthName(month.month)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_card_${month.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Month, Year, Property, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$monthName ${month.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = property?.name ?: "Unknown Property",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RentStatusBadge(status = month.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Due vs Paid breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Due: ${CurrencyUtils.format(month.totalDue, currency)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Due Date: ${DateUtils.formatDisplayDate(month.dueDate)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Paid: ${CurrencyUtils.format(month.totalPaid, currency)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF15803D)
                    )
                    Text(
                        text = "Balance: ${CurrencyUtils.format(month.balance, currency)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (month.balance > 0) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Toggle Expand Button & Add Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${payments.size} Payment ${if (payments.size == 1) "Record" else "Records"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (month.balance > 0) {
                    Button(
                        onClick = onAddPayment,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("history_pay_btn_${month.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Payment", fontSize = 12.sp)
                    }
                }
            }

            // Expanded Payment Ledger
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (payments.isEmpty()) {
                        Text(
                            text = "No payments recorded for this month yet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        payments.forEach { pay ->
                            PaymentLedgerItem(
                                payment = pay,
                                currency = currency,
                                onViewReceipt = { onViewReceipt(pay) },
                                onDelete = { onDeletePayment(pay) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentLedgerItem(
    payment: PaymentEntity,
    currency: String,
    onViewReceipt: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = CurrencyUtils.format(payment.amount, currency),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF15803D)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${payment.method}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Receipt: ${payment.receiptNumber.ifBlank { "RT-OFFICIAL" }} • Date: ${DateUtils.formatDisplayDate(payment.paidDate)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (payment.reference.isNotBlank()) {
                    Text(
                        text = "Ref: ${payment.reference}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onViewReceipt,
                    modifier = Modifier.testTag("view_receipt_btn_${payment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "View Receipt",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_payment_btn_${payment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Payment",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
