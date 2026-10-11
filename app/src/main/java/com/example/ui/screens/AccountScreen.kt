package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Animated3DSection
import com.example.ui.components.Card3D
import com.example.ui.theme.*
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    email: String,
    accountCreatedAt: Long,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("account_screen_root"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Account",
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkNavyText
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // User Identity Card in 3D Yellow and Navy
            Animated3DSection(delayMillis = 50) {
                Card3D(
                    shape = RoundedCornerShape(20.dp),
                    containerColor = YellowCardSurface,
                    borderColor = NavyBorder,
                    borderWidth = 1.8.dp,
                    depth = 5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary)
                                    .border(2.dp, YellowBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Account",
                                    tint = YellowAccent,
                                    modifier = Modifier.size(50.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Personal Account",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = DarkNavyText
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = NavySecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = email.ifBlank { "Personal Tenant" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkSlateText,
                                    modifier = Modifier.testTag("account_email_display")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = SubtleBorder)
                        Spacer(modifier = Modifier.height(18.dp))

                        // Account Details
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Email", fontSize = 11.sp, color = DarkSlateText, fontWeight = FontWeight.SemiBold)
                                Text(email.ifBlank { "N/A" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavyText)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Account Created", fontSize = 11.sp, color = DarkSlateText, fontWeight = FontWeight.SemiBold)
                                Text(DateUtils.formatTimestamp(accountCreatedAt), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavyText)
                            }
                        }
                    }
                }
            }

            // Version 2.0 Feature Summary Card in 3D
            Animated3DSection(delayMillis = 120) {
                Card3D(
                    shape = RoundedCornerShape(18.dp),
                    containerColor = YellowCardSurface,
                    borderColor = NavyBorder,
                    borderWidth = 1.8.dp,
                    depth = 5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "App Version",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DarkNavyText
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavyPrimary)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Version 2.1",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = YellowAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "• Starts tracking house rent from July 2026\n• Advance Rent option available for all months\n• Future months unlock automatically once each month arrives\n• Focused Current Month Home Dashboard\n• Permanent record locking & past month protection\n• Firebase Cloud Sync across devices\n• Password reset automatically deletes previous password\n• Excel spreadsheet (.csv) & printable PDF export",
                            fontSize = 12.sp,
                            color = DarkNavyText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Security & Privacy Note in 3D
            Animated3DSection(delayMillis = 180) {
                Card3D(
                    shape = RoundedCornerShape(16.dp),
                    containerColor = YellowCardSurface,
                    borderColor = SubtleBorder,
                    depth = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = "Private", tint = NavyPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Your rent records are private and securely linked exclusively to this account.",
                            fontSize = 12.sp,
                            color = DarkSlateText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Logout Button
            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AlertRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("account_logout_btn")
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Logout", fontWeight = FontWeight.Bold, color = DarkNavyText) },
            text = { Text("Are you sure you want to log out of My Rent Tracker?", color = DarkSlateText) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = YellowAccent),
                    modifier = Modifier.testTag("confirm_logout_action_btn")
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold, color = YellowAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel", color = DarkNavyText)
                }
            }
        )
    }
}
