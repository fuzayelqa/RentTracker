package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.util.CurrencyUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignIn: (email: String, pass: String) -> Unit,
    onSignUp: (name: String, email: String, pass: String, currency: String) -> Unit,
    onSendResetPassword: (email: String) -> Unit,
    onContinueGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Create Account

    // Form inputs
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("BDT") }
    var localError by remember { mutableStateOf<String?>(null) }
    var showForgotDialog by remember { mutableStateOf(false) }

    val currencies = listOf("BDT", "USD", "EUR", "GBP", "INR")

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_screen_root"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Header
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HomeWork,
                    contentDescription = "RentTracker",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "RentTracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Track property rent, utility bills & payment records",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Auth Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Tabs: Sign In / Create Account
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                localError = null
                            },
                            text = { Text("Sign In", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_sign_in")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                localError = null
                            },
                            text = { Text("Create Account", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_create_account")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sign Up Specific: Name & Currency
                    if (selectedTab == 1) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Full Name") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Default Rent Currency", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            currencies.forEach { c ->
                                FilterChip(
                                    selected = selectedCurrency == c,
                                    onClick = { selectedCurrency = c },
                                    label = { Text("$c (${CurrencyUtils.getSymbol(c)})", fontSize = 12.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localError = null
                        },
                        label = { Text("Email Address") },
                        placeholder = { Text("user@example.com") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                        },
                        label = { Text("Password") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password") },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        singleLine = true
                    )

                    // Sign Up Specific: Confirm Password
                    if (selectedTab == 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                localError = null
                            },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Confirm Password") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_confirm_password_input"),
                            singleLine = true
                        )
                    }

                    // Forgot Password Link (for Sign In)
                    if (selectedTab == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showForgotDialog = true },
                                modifier = Modifier.testTag("auth_forgot_password_btn")
                            ) {
                                Text("Forgot Password?", fontSize = 12.sp)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Display errors if any
                    val displayError = localError ?: errorMessage
                    AnimatedVisibility(visible = displayError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = displayError ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                if (selectedTab == 0 && (displayError?.contains("Create Account", ignoreCase = true) == true || displayError?.contains("no account", ignoreCase = true) == true || displayError?.contains("Incorrect", ignoreCase = true) == true)) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(
                                        onClick = {
                                            selectedTab = 1
                                            localError = null
                                        },
                                        modifier = Modifier.testTag("switch_to_create_account_btn")
                                    ) {
                                        Text("New here? Tap here to Create an Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Button (Sign In / Create Account)
                    Button(
                        onClick = {
                            if (email.isBlank() || !email.contains("@")) {
                                localError = "Please enter a valid email address"
                                return@Button
                            }
                            if (password.length < 6) {
                                localError = "Password must be at least 6 characters"
                                return@Button
                            }

                            if (selectedTab == 1) {
                                if (password != confirmPassword) {
                                    localError = "Passwords do not match"
                                    return@Button
                                }
                                onSignUp(name.ifBlank { "Tenant" }, email, password, selectedCurrency)
                            } else {
                                onSignIn(email, password)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (selectedTab == 0) "Sign In to RentTracker" else "Create My Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Continue as Guest Option
                    OutlinedButton(
                        onClick = onContinueGuest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_guest_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Continue in Offline / Guest Mode", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Forgot Password Dialog
    if (showForgotDialog) {
        var resetEmail by remember { mutableStateOf(email) }
        var resetSent by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showForgotDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Reset Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter your email address and we'll send you a password reset link.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (resetSent) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Reset link sent! Please check your inbox.",
                            color = Color(0xFF15803D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showForgotDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close")
                        }
                        Button(
                            onClick = {
                                if (resetEmail.isNotBlank()) {
                                    onSendResetPassword(resetEmail)
                                    resetSent = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Send Link")
                        }
                    }
                }
            }
        }
    }
}
