package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.PropertyEntity
import com.example.data.local.entity.RentMonthEntity
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddPaymentDialog(
    rentMonth: RentMonthEntity,
    property: PropertyEntity?,
    onDismiss: () -> Unit,
    onSavePayment: (amount: Double, paidDate: String, method: String, reference: String, note: String) -> Unit
) {
    val currency = property?.currency ?: "BDT"
    val defaultAmount = if (rentMonth.balance > 0) rentMonth.balance.toString() else rentMonth.totalDue.toString()

    var amountText by remember { mutableStateOf(defaultAmount) }
    var selectedMethod by remember { mutableStateOf("bKash") }
    var referenceText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var paidDateText by remember { mutableStateOf(DateUtils.getCurrentDateString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val methods = listOf("bKash", "Nagad", "Bank Transfer", "Cash", "Card", "Cheque", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_payment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = "Payment Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Record Rent Payment",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("payment_close_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "${property?.name.orEmpty()} — ${DateUtils.getMonthName(rentMonth.month)} ${rentMonth.year}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Due & Balance Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Due: ${CurrencyUtils.format(rentMonth.totalDue, currency)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Remaining: ${CurrencyUtils.format(rentMonth.balance, currency)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Payment Amount (${CurrencyUtils.getSymbol(currency)})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input"),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Enter full or partial payment amount")
                        }
                    }
                )

                // Quick presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { amountText = rentMonth.balance.coerceAtLeast(0.0).toString() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preset_full_balance_button")
                    ) {
                        Text("Full Balance", fontSize = 12.sp)
                    }
                    if (rentMonth.balance > 0) {
                        OutlinedButton(
                            onClick = { amountText = (rentMonth.balance / 2.0).toInt().toString() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preset_half_balance_button")
                        ) {
                            Text("50% Partial", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method Selector
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    methods.forEach { m ->
                        FilterChip(
                            selected = selectedMethod == m,
                            onClick = { selectedMethod = m },
                            label = { Text(m, fontSize = 12.sp) },
                            modifier = Modifier.testTag("method_chip_$m")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reference Input
                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { referenceText = it },
                    label = { Text("Transaction Reference / TrxID") },
                    placeholder = { Text("e.g. 9BK459201 or DBBL-Ref") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_reference_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date
                OutlinedTextField(
                    value = paidDateText,
                    onValueChange = { paidDateText = it },
                    label = { Text("Payment Date (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_date_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Paid by brother, includes gas bill") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_note_input"),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0.0) {
                            errorMessage = "Please enter a valid positive amount"
                            return@Button
                        }
                        onSavePayment(
                            amount,
                            paidDateText.ifBlank { DateUtils.getCurrentDateString() },
                            selectedMethod,
                            referenceText.trim(),
                            noteText.trim()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_payment_confirm_button")
                ) {
                    Text("Confirm Payment Record", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
