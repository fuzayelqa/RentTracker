package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RentRecordEntity
import com.example.ui.components.Animated3DSection
import com.example.ui.components.Card3D
import com.example.ui.theme.*
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.ExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    records: List<RentRecordEntity>,
    userEmail: String = "",
    onDeleteRecord: (monthKey: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedRecordForDetail by remember { mutableStateOf<RentRecordEntity?>(null) }
    var showDeleteSafetyFlow by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen_root"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rent History",
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkNavyText
                    )
                },
                actions = {
                    if (records.isNotEmpty()) {
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.testTag("history_download_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Statement",
                                tint = DarkNavyText
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "No Records",
                        tint = NavyPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Rent Records Yet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = DarkNavyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rent records starting from July 2026 will appear here chronologically.",
                        fontSize = 13.sp,
                        color = DarkSlateText,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chronological Rent Records (From July 2026)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "${records.size} Records",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavySecondary
                        )
                    }
                }

                items(records) { record ->
                    Animated3DSection(delayMillis = 60) {
                        RentRecordCard(
                            record = record,
                            onClick = {
                                selectedRecordForDetail = record
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Detail Dialog
    if (selectedRecordForDetail != null && !showDeleteSafetyFlow) {
        val rec = selectedRecordForDetail!!

        AlertDialog(
            onDismissRequest = { selectedRecordForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.formatMonthYear(rec.year, rec.month),
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyText
                    )
                    StatusPill(status = rec.status)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Rent Amount: ${CurrencyUtils.format(rec.rentAmount, rec.currency)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkNavyText
                    )
                    Text(
                        text = "Payment Date: ${rec.paymentDate.ifBlank { "Recorded" }}",
                        fontSize = 14.sp,
                        color = DarkSlateText
                    )

                    // Optional Comment Note
                    if (rec.comment.isNotBlank()) {
                        Text(
                            text = "Comment: ${rec.comment}",
                            fontSize = 13.sp,
                            color = DarkNavyText,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = SubtleBorder)
                    Spacer(modifier = Modifier.height(4.dp))

                    val isPastMonth = DateUtils.isRecordInPastMonth(rec.year, rec.month)

                    // Locked Status Note
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPastMonth) AlertRed.copy(alpha = 0.08f) else LockedBadgeBg)
                            .border(1.dp, if (isPastMonth) AlertRed.copy(alpha = 0.4f) else LockedBadgeBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isPastMonth) AlertRed else LockedBadgeText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPastMonth) {
                                "Month Ended: This previous month's record is permanently locked and cannot be edited or deleted."
                            } else {
                                "Record Locked: This rent record cannot be edited after saving."
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isPastMonth) AlertRed else LockedBadgeText
                        )
                    }
                }
            },
            confirmButton = {
                val isPastMonth = DateUtils.isRecordInPastMonth(rec.year, rec.month)
                if (!isPastMonth) {
                    OutlinedButton(
                        onClick = {
                            showDeleteSafetyFlow = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed),
                        modifier = Modifier.testTag("initiate_delete_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Record", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRecordForDetail = null }) {
                    Text("Close", color = DarkNavyText)
                }
            }
        )
    }

    // MULTI-STEP HIGH SECURITY DELETE WORKFLOW
    if (showDeleteSafetyFlow && selectedRecordForDetail != null) {
        val rec = selectedRecordForDetail!!
        DeleteSecurityWorkflowDialog(
            recordMonthTitle = DateUtils.formatMonthYear(rec.year, rec.month),
            onDismiss = {
                showDeleteSafetyFlow = false
                selectedRecordForDetail = null
            },
            onDeleteConfirmed = {
                onDeleteRecord(rec.monthKey)
                showDeleteSafetyFlow = false
                selectedRecordForDetail = null
            }
        )
    }

    // DOWNLOAD REPORT DIALOG (Excel / PDF)
    if (showExportDialog) {
        ExportReportDialog(
            records = records,
            userEmail = userEmail,
            onDismiss = { showExportDialog = false },
            onExportExcel = {
                val uri = ExportUtils.exportToExcel(context, records, userEmail)
                if (uri != null) {
                    ExportUtils.shareExportedFile(context, uri, "text/csv", "Share Rent Records Excel (.csv)")
                    showExportDialog = false
                } else {
                    Toast.makeText(context, "Failed to export Excel file.", Toast.LENGTH_SHORT).show()
                }
            },
            onExportPdf = {
                val uri = ExportUtils.exportToPdf(context, records, userEmail)
                if (uri != null) {
                    ExportUtils.shareExportedFile(context, uri, "application/pdf", "Share Rent Statement PDF (.pdf)")
                    showExportDialog = false
                } else {
                    Toast.makeText(context, "Failed to export PDF.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

/**
 * DELETE / REMOVE SECURITY WORKFLOW:
 * Multi-step confirmation required by user specification:
 * 1. Warning screen ("WARNING: Deleting this rent record is permanent...")
 * 2. Type "DELETE" verification screen
 * 3. Final confirmation message
 */
@Composable
fun DeleteSecurityWorkflowDialog(
    recordMonthTitle: String,
    onDismiss: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Initial Warning, 2: Type DELETE, 3: Final confirmation
    var deleteInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    when (step) {
        1 -> {
            // STEP 1: WARNING DIALOG
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning", tint = AlertRed, modifier = Modifier.size(36.dp))
                },
                title = {
                    Text(
                        text = "WARNING",
                        fontWeight = FontWeight.Black,
                        color = AlertRed,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Deleting the rent record for $recordMonthTitle is permanent.\n\nThis action cannot be undone.\n\nAre you absolutely sure you want to proceed?",
                            fontSize = 14.sp,
                            color = DarkNavyText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { step = 2 },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        modifier = Modifier.testTag("delete_step1_proceed_btn")
                    ) {
                        Text("Continue Deletion", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = DarkSlateText)
                    }
                }
            )
        }

        2 -> {
            // STEP 2: TYPE "DELETE" CONFIRMATION
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = "Type DELETE to Continue",
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyText
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "To prevent accidental deletion, please type DELETE below to permanently remove the $recordMonthTitle record:",
                            fontSize = 13.sp,
                            color = DarkSlateText
                        )
                        OutlinedTextField(
                            value = deleteInput,
                            onValueChange = {
                                deleteInput = it
                                inputError = null
                            },
                            placeholder = { Text("DELETE") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("type_delete_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AlertRed,
                                unfocusedBorderColor = AlertRed.copy(alpha = 0.5f)
                            )
                        )
                        if (inputError != null) {
                            Text(
                                text = inputError ?: "",
                                color = AlertRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (deleteInput.trim() == "DELETE") {
                                step = 3
                            } else {
                                inputError = "You must type exactly 'DELETE' in capital letters."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        modifier = Modifier.testTag("delete_step2_verify_btn")
                    ) {
                        Text("Verify & Continue", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = DarkSlateText)
                    }
                }
            )
        }

        3 -> {
            // STEP 3: FINAL CONFIRMATION
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Final Delete", tint = AlertRed, modifier = Modifier.size(36.dp))
                },
                title = {
                    Text(
                        text = "Final Confirmation",
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyText
                    )
                },
                text = {
                    Text(
                        text = "Are you completely certain you want to permanently delete $recordMonthTitle from your rent history? All associated data will be removed.",
                        fontSize = 14.sp,
                        color = DarkNavyText
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onDeleteConfirmed,
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        modifier = Modifier.testTag("delete_step3_final_btn")
                    ) {
                        Text("Delete Record", fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = DarkSlateText)
                    }
                }
            )
        }
    }
}
