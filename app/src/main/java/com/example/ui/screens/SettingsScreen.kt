package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    userSettings: UserSettingsEntity,
    auditLogs: List<AuditLogEntity>,
    currentUserEmail: String = "",
    isSyncing: Boolean = false,
    lastSyncTime: String? = null,
    onSyncWithFirebase: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onUpdateSettings: (UserSettingsEntity) -> Unit,
    onLockApp: () -> Unit,
    onSeedDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var showAuditLogsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Editable profile fields
    var tenantName by remember(userSettings) { mutableStateOf(userSettings.tenantName) }
    var email by remember(userSettings) { mutableStateOf(userSettings.email) }
    var phone by remember(userSettings) { mutableStateOf(userSettings.phone) }

    val currencies = listOf("BDT", "USD", "EUR", "GBP", "INR")
    val isBangla = userSettings.language == "BN"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "সেটিংস ও নিরাপত্তা" else "Settings & Security",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("settings_scroll_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Profile & Tenant Info", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        if (currentUserEmail.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Signed In Account", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(currentUserEmail, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                OutlinedButton(
                                    onClick = onSignOut,
                                    modifier = Modifier.testTag("settings_sign_out_btn")
                                ) {
                                    Text("Sign Out", fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        OutlinedTextField(
                            value = tenantName,
                            onValueChange = {
                                tenantName = it
                                onUpdateSettings(userSettings.copy(tenantName = it))
                            },
                            label = { Text("Full Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_tenant_name"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    onUpdateSettings(userSettings.copy(email = it))
                                },
                                label = { Text("Email") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_email"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = {
                                    phone = it
                                    onUpdateSettings(userSettings.copy(phone = it))
                                },
                                label = { Text("Phone") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_phone"),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Preferences (Currency & Language)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = "Preferences", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "App Preferences", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Default Currency", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            currencies.forEach { c ->
                                FilterChip(
                                    selected = userSettings.defaultCurrency == c,
                                    onClick = { onUpdateSettings(userSettings.copy(defaultCurrency = c)) },
                                    label = { Text("$c (${CurrencyUtils.getSymbol(c)})") },
                                    modifier = Modifier.testTag("currency_chip_$c")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Language", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = userSettings.language == "EN",
                                onClick = { onUpdateSettings(userSettings.copy(language = "EN")) },
                                label = { Text("English") },
                                modifier = Modifier.testTag("lang_en_chip")
                            )
                            FilterChip(
                                selected = userSettings.language == "BN",
                                onClick = { onUpdateSettings(userSettings.copy(language = "BN")) },
                                label = { Text("বাংলা (Bangla)") },
                                modifier = Modifier.testTag("lang_bn_chip")
                            )
                        }
                    }
                }
            }

            // Security & PIN Lock
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = "Security", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Privacy & Security", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("PIN Lock Protection", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    text = if (userSettings.pinEnabled) "PIN is active. App requires 4-digit PIN." else "Require 4-digit PIN to access records.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = userSettings.pinEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        showPinDialog = true
                                    } else {
                                        onUpdateSettings(userSettings.copy(pinEnabled = false))
                                    }
                                },
                                modifier = Modifier.testTag("pin_lock_switch")
                            )
                        }

                        if (userSettings.pinEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showPinDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("change_pin_btn")
                                ) {
                                    Text("Change PIN", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = onLockApp,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("lock_now_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lock App", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Notification Reminder Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Reminders", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Automated Reminders", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        ReminderSwitchRow(
                            label = "7 Days Before Due Date",
                            checked = userSettings.reminderSevenDays,
                            onToggle = { onUpdateSettings(userSettings.copy(reminderSevenDays = it)) }
                        )
                        ReminderSwitchRow(
                            label = "3 Days Before Due Date",
                            checked = userSettings.reminderThreeDays,
                            onToggle = { onUpdateSettings(userSettings.copy(reminderThreeDays = it)) }
                        )
                        ReminderSwitchRow(
                            label = "On Due Date",
                            checked = userSettings.reminderDueDay,
                            onToggle = { onUpdateSettings(userSettings.copy(reminderDueDay = it)) }
                        )
                        ReminderSwitchRow(
                            label = "Overdue Rent Alerts",
                            checked = userSettings.reminderOverdue,
                            onToggle = { onUpdateSettings(userSettings.copy(reminderOverdue = it)) }
                        )
                        ReminderSwitchRow(
                            label = "Lease Expiration Alerts",
                            checked = userSettings.reminderLease,
                            onToggle = { onUpdateSettings(userSettings.copy(reminderLease = it)) }
                        )
                    }
                }
            }

            // Audit Trail Button
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAuditLogsDialog = true }
                        .testTag("audit_logs_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = "Audit", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Financial Audit Log", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("View history of all transactions and changes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Open")
                    }
                }
            }

            // Firebase Cloud Sync Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_cloud_sync_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Firebase Cloud Sync", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "Connected to project: renttracker-16415",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (lastSyncTime != null) {
                                    Text(
                                        text = "Last synced: $lastSyncTime",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Button(
                                onClick = onSyncWithFirebase,
                                enabled = !isSyncing,
                                modifier = Modifier.testTag("sync_firebase_btn")
                            ) {
                                Text(if (isSyncing) "Syncing..." else "Sync Now", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Data Actions (Demo Data & Wipe)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Data Management", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onSeedDemoData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_seed_demo_btn")
                        ) {
                            Text("Load Sample Rent & Tenancy Data")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_clear_all_data_btn")
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Wipe", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete All Application Data")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Set PIN Dialog
    if (showPinDialog) {
        SetPinDialog(
            currentPin = userSettings.pinCode,
            onDismiss = { showPinDialog = false },
            onSave = { newPin ->
                onUpdateSettings(userSettings.copy(pinEnabled = true, pinCode = newPin))
                showPinDialog = false
            }
        )
    }

    // Audit Logs Dialog
    if (showAuditLogsDialog) {
        AuditLogsDialog(
            auditLogs = auditLogs,
            onDismiss = { showAuditLogsDialog = false }
        )
    }

    // Delete Confirmation
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete All Data?") },
            text = {
                Text("This will permanently delete all properties, rent months, payments, receipts, and audit history. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Nuclear Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReminderSwitchRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun SetPinDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var pinText by remember { mutableStateOf(currentPin) }
    var confirmPinText by remember { mutableStateOf(currentPin) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Configure 4-Digit PIN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = pinText,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinText = it },
                    label = { Text("Enter 4-Digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_input_field")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPinText,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPinText = it },
                    label = { Text("Confirm PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_confirm_input_field")
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (pinText.length != 4) {
                                errorText = "PIN must be exactly 4 digits"
                                return@Button
                            }
                            if (pinText != confirmPinText) {
                                errorText = "PINs do not match"
                                return@Button
                            }
                            onSave(pinText)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_pin_btn")
                    ) {
                        Text("Save PIN")
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogsDialog(
    auditLogs: List<AuditLogEntity>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Audit Trail (${auditLogs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()

                if (auditLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No audit log records yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 10.dp)
                    ) {
                        items(auditLogs, key = { it.id }) { log ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = log.action,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(log.timestamp)),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = log.description, fontSize = 12.sp)
                                    if (log.newValue.isNotBlank()) {
                                        Text(
                                            text = "Value: ${log.newValue}",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
