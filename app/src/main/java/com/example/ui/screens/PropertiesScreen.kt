package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.PropertyEntity
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesScreen(
    properties: List<PropertyEntity>,
    onAddProperty: (PropertyEntity, List<Pair<String, Double>>) -> Unit,
    onArchiveProperty: (String) -> Unit,
    onDeleteProperty: (String) -> Unit,
    onGenerateMonth: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPropertyForDetails by remember { mutableStateOf<PropertyEntity?>(null) }
    var propertyToDelete by remember { mutableStateOf<PropertyEntity?>(null) }
    var propertyFilter by remember { mutableStateOf("ACTIVE") } // ACTIVE, ALL, ARCHIVED

    val filteredList = when (propertyFilter) {
        "ACTIVE" -> properties.filter { it.status == "Active" }
        "ARCHIVED" -> properties.filter { it.status == "Archived" }
        else -> properties
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Properties & Leases",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("fab_add_property"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Property")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = propertyFilter == "ACTIVE",
                    onClick = { propertyFilter = "ACTIVE" },
                    label = { Text("Active (${properties.count { it.status == "Active" }})") },
                    modifier = Modifier.testTag("filter_active_properties")
                )
                FilterChip(
                    selected = propertyFilter == "ALL",
                    onClick = { propertyFilter = "ALL" },
                    label = { Text("All (${properties.size})") },
                    modifier = Modifier.testTag("filter_all_properties")
                )
                FilterChip(
                    selected = propertyFilter == "ARCHIVED",
                    onClick = { propertyFilter = "ARCHIVED" },
                    label = { Text("Archived (${properties.count { it.status == "Archived" }})") },
                    modifier = Modifier.testTag("filter_archived_properties")
                )
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HomeWork,
                            contentDescription = "No Properties",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No properties found in this tab",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("empty_add_property_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Property")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { property ->
                        PropertyCard(
                            property = property,
                            onClick = { selectedPropertyForDetails = property },
                            onArchive = { onArchiveProperty(property.id) },
                            onDelete = { propertyToDelete = property },
                            onGenerateRent = { onGenerateMonth(property.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }

    // Add Property Dialog
    if (showAddDialog) {
        AddPropertyDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newProp, charges ->
                onAddProperty(newProp, charges)
                showAddDialog = false
            }
        )
    }

    // Property Details Dialog
    if (selectedPropertyForDetails != null) {
        PropertyDetailDialog(
            property = selectedPropertyForDetails!!,
            onDismiss = { selectedPropertyForDetails = null },
            onArchive = {
                onArchiveProperty(selectedPropertyForDetails!!.id)
                selectedPropertyForDetails = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (propertyToDelete != null) {
        AlertDialog(
            onDismissRequest = { propertyToDelete = null },
            title = { Text("Delete Property?") },
            text = {
                Text("Are you sure you want to delete '${propertyToDelete!!.name}'? Tip: You can choose Archive Property instead to preserve all historical payment ledger records.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProperty(propertyToDelete!!.id)
                        propertyToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { propertyToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PropertyCard(
    property: PropertyEntity,
    onClick: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onGenerateRent: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("property_card_${property.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = property.propertyType,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = property.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${property.propertyType} • ${property.city}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Generate This Month Rent") },
                            onClick = {
                                menuExpanded = false
                                onGenerateRent()
                            }
                        )
                        if (property.status != "Archived") {
                            DropdownMenuItem(
                                text = { Text("Archive Property") },
                                onClick = {
                                    menuExpanded = false
                                    onArchive()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete Property", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Monthly Rent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.format(property.monthlyRent, property.currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Due Day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Day ${property.dueDay} of month",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (property.landlordName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Landlord",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Landlord: ${property.landlordName} (${property.landlordPhone.ifBlank { "No phone" }})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddPropertyDialog(
    onDismiss: () -> Unit,
    onConfirm: (PropertyEntity, List<Pair<String, Double>>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Dhaka") }
    var country by remember { mutableStateOf("Bangladesh") }
    var propertyType by remember { mutableStateOf("Apartment") }
    var monthlyRentText by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("BDT") }
    var dueDayText by remember { mutableStateOf("5") }
    var landlordName by remember { mutableStateOf("") }
    var landlordPhone by remember { mutableStateOf("") }
    var landlordEmail by remember { mutableStateOf("") }
    var securityDepositText by remember { mutableStateOf("") }
    var advancePaymentText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Recurring charges checkboxes/defaults
    var addServiceCharge by remember { mutableStateOf(true) }
    var serviceChargeAmt by remember { mutableStateOf("2000") }
    var addGasBill by remember { mutableStateOf(true) }
    var gasBillAmt by remember { mutableStateOf("1080") }

    val types = listOf("Apartment", "House", "Room", "Office", "Other")
    val currencies = listOf("BDT", "USD", "EUR", "GBP", "INR")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_property_dialog_surface")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Property",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Property Name *") },
                    placeholder = { Text("e.g. Greenview Flat 4B") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_property_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Property Type Chips
                Text("Property Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    types.forEach { t ->
                        FilterChip(
                            selected = propertyType == t,
                            onClick = { propertyType = t },
                            label = { Text(t, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        placeholder = { Text("Road 12, Banani") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_property_address"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_property_city"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Monthly Rent & Currency
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = monthlyRentText,
                        onValueChange = { monthlyRentText = it },
                        label = { Text("Monthly Rent *") },
                        placeholder = { Text("20000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("input_monthly_rent"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { dueDayText = it },
                        label = { Text("Due Day (1-31)") },
                        placeholder = { Text("5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("input_due_day"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Currency selector
                Text("Currency", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    currencies.forEach { c ->
                        FilterChip(
                            selected = currency == c,
                            onClick = { currency = c },
                            label = { Text("$c (${CurrencyUtils.getSymbol(c)})", fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Landlord Info
                Text("Landlord Information (Optional)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = landlordName,
                    onValueChange = { landlordName = it },
                    label = { Text("Landlord Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = landlordPhone,
                        onValueChange = { landlordPhone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = landlordEmail,
                        onValueChange = { landlordEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Recurring charges option
                Text("Recurring Utility Charges", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = addServiceCharge,
                        onClick = { addServiceCharge = !addServiceCharge },
                        label = { Text("Service Charge") }
                    )
                    if (addServiceCharge) {
                        OutlinedTextField(
                            value = serviceChargeAmt,
                            onValueChange = { serviceChargeAmt = it },
                            label = { Text("Amount") },
                            modifier = Modifier.width(110.dp),
                            singleLine = true
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = addGasBill,
                        onClick = { addGasBill = !addGasBill },
                        label = { Text("Gas Bill") }
                    )
                    if (addGasBill) {
                        OutlinedTextField(
                            value = gasBillAmt,
                            onValueChange = { gasBillAmt = it },
                            label = { Text("Amount") },
                            modifier = Modifier.width(110.dp),
                            singleLine = true
                        )
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val rent = monthlyRentText.toDoubleOrNull()
                        if (name.isBlank()) {
                            errorText = "Please enter a property name"
                            return@Button
                        }
                        if (rent == null || rent < 0.0) {
                            errorText = "Please enter a valid monthly rent"
                            return@Button
                        }
                        val day = dueDayText.toIntOrNull()?.coerceIn(1, 31) ?: 5

                        val newProp = PropertyEntity(
                            name = name.trim(),
                            address = address.trim(),
                            city = city.trim(),
                            country = country.trim(),
                            propertyType = propertyType,
                            landlordName = landlordName.trim(),
                            landlordPhone = landlordPhone.trim(),
                            landlordEmail = landlordEmail.trim(),
                            monthlyRent = rent,
                            currency = currency,
                            dueDay = day
                        )

                        val charges = mutableListOf<Pair<String, Double>>()
                        if (addServiceCharge) {
                            serviceChargeAmt.toDoubleOrNull()?.let { charges.add("Service Charge" to it) }
                        }
                        if (addGasBill) {
                            gasBillAmt.toDoubleOrNull()?.let { charges.add("Gas Bill" to it) }
                        }

                        onConfirm(newProp, charges)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_add_property_btn")
                ) {
                    Text("Save Property", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PropertyDetailDialog(
    property: PropertyEntity,
    onDismiss: () -> Unit,
    onArchive: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = property.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "${property.propertyType} • Status: ${property.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                DetailItem(label = "Location", value = "${property.address}, ${property.city}, ${property.country}")
                DetailItem(label = "Monthly Rent", value = CurrencyUtils.format(property.monthlyRent, property.currency))
                DetailItem(label = "Rent Due Day", value = "Every ${property.dueDay}th of month")
                if (property.securityDeposit > 0) {
                    DetailItem(label = "Security Deposit", value = CurrencyUtils.format(property.securityDeposit, property.currency))
                }
                if (property.advancePayment > 0) {
                    DetailItem(label = "Advance Paid", value = CurrencyUtils.format(property.advancePayment, property.currency))
                }
                if (property.tenancyStart.isNotBlank()) {
                    DetailItem(label = "Lease Start", value = property.tenancyStart)
                }
                if (property.tenancyEnd.isNotBlank()) {
                    DetailItem(label = "Lease End", value = property.tenancyEnd)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Landlord Contact", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                DetailItem(label = "Name", value = property.landlordName.ifBlank { "Not provided" })
                DetailItem(label = "Phone", value = property.landlordPhone.ifBlank { "Not provided" })
                DetailItem(label = "Email", value = property.landlordEmail.ifBlank { "Not provided" })
                if (property.landlordNotes.isNotBlank()) {
                    DetailItem(label = "Notes / Bank", value = property.landlordNotes)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (property.status != "Archived") {
                        OutlinedButton(
                            onClick = onArchive,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Archive, contentDescription = "Archive", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Archive")
                        }
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
