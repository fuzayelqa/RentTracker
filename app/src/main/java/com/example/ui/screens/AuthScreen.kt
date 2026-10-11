package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Animated3DSection
import com.example.ui.components.Button3D
import com.example.ui.components.Card3D
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    infoMessage: String? = null,
    onSignIn: (email: String, pass: String) -> Unit,
    onSignUp: (email: String, pass: String) -> Unit,
    onRequestResetCode: (email: String, onResult: (Boolean, String, String?) -> Unit) -> Unit = { _, _ -> },
    onResetPassword: (email: String, code: String, newPass: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Sign Up
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var validationError by remember { mutableStateOf<String?>(null) }
    var resetSuccessBanner by remember { mutableStateOf<String?>(null) }

    // Forgot Password Verification Code States
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordStep by remember { mutableIntStateOf(1) } // 1: Email, 2: Enter Code & New Pass
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordCode by remember { mutableStateOf("") }
    var forgotPasswordNewPass by remember { mutableStateOf("") }
    var forgotPasswordConfirmPass by remember { mutableStateOf("") }
    var showNewPass by remember { mutableStateOf(false) }
    var forgotPasswordMessage by remember { mutableStateOf<String?>(null) }
    var forgotPasswordIsError by remember { mutableStateOf(false) }
    var forgotPasswordLoading by remember { mutableStateOf(false) }

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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo Header with Navy Blue & Yellow Identity in 3D
            Animated3DSection(delayMillis = 0) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(NavyPrimary)
                        .border(3.dp, YellowAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "My Rent Tracker",
                        tint = YellowAccent,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Animated3DSection(delayMillis = 50) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "My Rent Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = DarkNavyText
                    )
                    Text(
                        text = "Simple, lightweight monthly house rent records (v2.0)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkSlateText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Auth Card in 3D
            Animated3DSection(delayMillis = 100) {
                Card3D(
                    shape = RoundedCornerShape(20.dp),
                    containerColor = YellowCardSurface,
                    borderColor = NavyBorder,
                    borderWidth = 1.8.dp,
                    depth = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_card")
                ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = YellowCardSurface,
                        contentColor = NavyPrimary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                validationError = null
                            },
                            text = {
                                Text(
                                    "Log In",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) SkyBluePrimary else DarkSlateText
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_login")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                validationError = null
                            },
                            text = {
                                Text(
                                    "Create Account",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) SkyBluePrimary else DarkSlateText
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_signup")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Success / Info Banner (e.g. Password Reset Confirmation)
                    val activeInfo = resetSuccessBanner ?: infoMessage
                    AnimatedVisibility(visible = activeInfo != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SuccessGreenBg),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, SuccessGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .testTag("auth_success_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Previous Password Deleted",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreenText
                                    )
                                    Text(
                                        text = activeInfo ?: "",
                                        fontSize = 12.sp,
                                        color = DarkNavyText,
                                        lineHeight = 16.sp
                                    )
                                }
                                IconButton(
                                    onClick = { resetSuccessBanner = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = DarkSlateText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Email Field
                    Text(
                        text = "Email Address",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkNavyText,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            validationError = null
                        },
                        placeholder = { Text("your.email@example.com") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email", tint = SkyBluePrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = SkyBlueBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    Text(
                        text = "Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkNavyText,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            validationError = null
                        },
                        placeholder = { Text("At least 6 characters") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password", tint = SkyBluePrimary) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) "Hide Password" else "Show Password",
                                    tint = DarkSlateText
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = SkyBlueBorder
                        )
                    )

                    // Forgot Password option (Only on Log In tab)
                    if (selectedTab == 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            TextButton(
                                onClick = {
                                    forgotPasswordEmail = email.trim()
                                    forgotPasswordMessage = null
                                    forgotPasswordIsError = false
                                    showForgotPasswordDialog = true
                                },
                                modifier = Modifier.testTag("auth_forgot_password_btn")
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    color = NavyPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Confirm Password (Sign Up)
                    if (selectedTab == 1) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Confirm Password",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkNavyText,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                validationError = null
                            },
                            placeholder = { Text("Re-type your password") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Confirm Password", tint = SkyBluePrimary) },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_confirm_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SkyBluePrimary,
                                unfocusedBorderColor = SkyBlueBorder
                            )
                        )
                    }

                    // Error Message
                    val activeError = validationError ?: errorMessage
                    AnimatedVisibility(visible = activeError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text(
                                text = activeError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button in 3D
                    Button3D(
                        onClick = {
                            if (email.isBlank() || !email.contains("@")) {
                                validationError = "Please enter a valid email address."
                                return@Button3D
                            }
                            if (password.length < 6) {
                                validationError = "Password must be at least 6 characters."
                                return@Button3D
                            }
                            if (selectedTab == 1 && password != confirmPassword) {
                                validationError = "Passwords do not match."
                                return@Button3D
                            }

                            if (selectedTab == 0) {
                                onSignIn(email.trim(), password)
                            } else {
                                onSignUp(email.trim(), password)
                            }
                        },
                        backgroundColor = NavyPrimary,
                        borderColor = NavyDarkest,
                        depth = 5.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_btn")
                    ) {
                        Box(
                            modifier = Modifier.align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = YellowAccent,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = if (selectedTab == 0) "Log In" else "Create Account",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = YellowAccent
                                )
                            }
                        }
                    }
                }
            }
        }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // FORGOT PASSWORD VERIFICATION CODE DIALOG
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = {
                    showForgotPasswordDialog = false
                    forgotPasswordLoading = false
                },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary)
                            .border(2.dp, YellowAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Reset Password",
                            tint = YellowAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (forgotPasswordStep == 1) "Reset Password (Step 1/2)" else "New Password (Step 2/2)",
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyText,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (forgotPasswordStep == 1) {
                            Text(
                                text = "Enter your registered email address. A password reset and verification code will be sent directly to your email inbox (not shown in the app):",
                                fontSize = 13.sp,
                                color = DarkSlateText,
                                lineHeight = 18.sp
                            )

                            OutlinedTextField(
                                value = forgotPasswordEmail,
                                onValueChange = {
                                    forgotPasswordEmail = it
                                    forgotPasswordMessage = null
                                },
                                placeholder = { Text("your.email@example.com") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = NavyPrimary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("forgot_password_email_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    unfocusedBorderColor = NavyBorder
                                )
                            )
                        } else {
                            // Step 2: Code sent through email
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = YellowCardSurface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, NavyBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = NavyPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Verification Sent to Email",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkNavyText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Sent to: $forgotPasswordEmail",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NavyPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Please check your email inbox (and spam folder). The code was sent through your email and is NOT shown in this app for your account security.",
                                        fontSize = 11.sp,
                                        color = DarkSlateText,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val emailIntent = Intent(Intent.ACTION_MAIN).apply {
                                                    addCategory(Intent.CATEGORY_APP_EMAIL)
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(Intent.createChooser(emailIntent, "Open Email App"))
                                            } catch (_: Exception) {
                                                try {
                                                    val mailto = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
                                                    context.startActivity(mailto)
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = NavyPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open Email App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Security Notice: Previous password deleted upon reset
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = AlertRedBg.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = AlertRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Security Notice: Once your password is reset, your previous password will be permanently deleted and cannot be used again.",
                                        fontSize = 11.sp,
                                        color = AlertRedText,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // 6-digit code input
                            Text(
                                text = "Enter 6-Digit Code from Email",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavyText
                            )
                            OutlinedTextField(
                                value = forgotPasswordCode,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                        forgotPasswordCode = it
                                        forgotPasswordMessage = null
                                    }
                                },
                                placeholder = { Text("Code from email") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = NavyPrimary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("forgot_password_code_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    unfocusedBorderColor = NavyBorder
                                )
                            )

                            // New Password input
                            Text(
                                text = "New Password (min 6 characters)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavyText
                            )
                            OutlinedTextField(
                                value = forgotPasswordNewPass,
                                onValueChange = {
                                    forgotPasswordNewPass = it
                                    forgotPasswordMessage = null
                                },
                                placeholder = { Text("At least 6 characters") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = NavyPrimary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showNewPass = !showNewPass }) {
                                        Icon(
                                            imageVector = if (showNewPass) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (showNewPass) "Hide" else "Show",
                                            tint = DarkSlateText
                                        )
                                    }
                                },
                                visualTransformation = if (showNewPass) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("forgot_password_new_pass_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    unfocusedBorderColor = NavyBorder
                                )
                            )

                            // Confirm Password
                            Text(
                                text = "Confirm New Password",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavyText
                            )
                            OutlinedTextField(
                                value = forgotPasswordConfirmPass,
                                onValueChange = {
                                    forgotPasswordConfirmPass = it
                                    forgotPasswordMessage = null
                                },
                                placeholder = { Text("Re-enter new password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = NavyPrimary
                                    )
                                },
                                visualTransformation = if (showNewPass) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("forgot_password_confirm_pass_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    unfocusedBorderColor = NavyBorder
                                )
                            )
                        }

                        if (forgotPasswordMessage != null) {
                            Text(
                                text = forgotPasswordMessage ?: "",
                                color = if (forgotPasswordIsError) AlertRed else SuccessGreenText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (forgotPasswordStep == 1) {
                                if (forgotPasswordEmail.isBlank() || !forgotPasswordEmail.contains("@")) {
                                    forgotPasswordMessage = "Please enter a valid email address."
                                    forgotPasswordIsError = true
                                    return@Button
                                }
                                forgotPasswordLoading = true
                                forgotPasswordMessage = null
                                onRequestResetCode(forgotPasswordEmail.trim()) { success, msg, _ ->
                                    forgotPasswordLoading = false
                                    if (success) {
                                        forgotPasswordStep = 2
                                        forgotPasswordMessage = msg
                                        forgotPasswordIsError = false
                                    } else {
                                        forgotPasswordMessage = msg
                                        forgotPasswordIsError = true
                                    }
                                }
                            } else {
                                // Step 2 submission
                                if (forgotPasswordCode.trim().length != 6) {
                                    forgotPasswordMessage = "Please enter the 6-digit verification code sent to your email."
                                    forgotPasswordIsError = true
                                    return@Button
                                }
                                if (forgotPasswordNewPass.length < 6) {
                                    forgotPasswordMessage = "Password must be at least 6 characters."
                                    forgotPasswordIsError = true
                                    return@Button
                                }
                                if (forgotPasswordNewPass != forgotPasswordConfirmPass) {
                                    forgotPasswordMessage = "Passwords do not match."
                                    forgotPasswordIsError = true
                                    return@Button
                                }

                                forgotPasswordLoading = true
                                forgotPasswordMessage = null
                                onResetPassword(
                                    forgotPasswordEmail.trim(),
                                    forgotPasswordCode.trim(),
                                    forgotPasswordNewPass
                                ) { success, msg ->
                                    forgotPasswordLoading = false
                                    if (success) {
                                        email = forgotPasswordEmail.trim()
                                        password = forgotPasswordNewPass
                                        showForgotPasswordDialog = false
                                        forgotPasswordStep = 1
                                        forgotPasswordCode = ""
                                        forgotPasswordNewPass = ""
                                        forgotPasswordConfirmPass = ""
                                        selectedTab = 0
                                        resetSuccessBanner = "Password reset successfully! Your previous password has been deleted. You can now log in with your new password."
                                    } else {
                                        forgotPasswordMessage = msg
                                        forgotPasswordIsError = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            contentColor = YellowAccent
                        ),
                        modifier = Modifier.testTag("forgot_password_confirm_btn")
                    ) {
                        if (forgotPasswordLoading) {
                            CircularProgressIndicator(
                                color = YellowAccent,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (forgotPasswordStep == 1) "Send Code via Email" else "Change Password",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (forgotPasswordStep == 2) {
                                forgotPasswordStep = 1
                                forgotPasswordMessage = null
                            } else {
                                showForgotPasswordDialog = false
                            }
                        }
                    ) {
                        Text(
                            text = if (forgotPasswordStep == 2) "Back" else "Cancel",
                            color = DarkSlateText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}
