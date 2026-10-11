package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RentTrackerViewModel
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.*

enum class Screen(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    HISTORY("Rent History", Icons.Default.ReceiptLong),
    ACCOUNT("Account", Icons.Default.Person)
}

@Composable
fun RentTrackerApp(viewModel: RentTrackerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chronologicalRecords by viewModel.chronologicalRecords.collectAsStateWithLifecycle()
    val currentRecord by viewModel.currentMonthRecord.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearInfoMessage()
        }
    }

    LaunchedEffect(uiState.authError) {
        uiState.authError?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearAuthError()
        }
    }

    // 1. If not authenticated, show AuthScreen (Login / Sign Up)
    if (!uiState.isAuthenticated) {
        AuthScreen(
            isLoading = uiState.authLoading,
            errorMessage = uiState.authError,
            infoMessage = uiState.infoMessage,
            onSignIn = { email, pass -> viewModel.signIn(email, pass) },
            onSignUp = { email, pass -> viewModel.signUp(email, pass) },
            onRequestResetCode = { email, onResult ->
                viewModel.requestPasswordResetCode(email, onResult)
            },
            onResetPassword = { email, code, newPass, onResult ->
                viewModel.verifyCodeAndResetPassword(email, code, newPass, onResult)
            }
        )
        return
    }

    // 2. Main App with Bottom Navigation (Home, Rent History, Account)
    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = Screen.HOME
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .drawBehind {
                        // 3D bottom extrusion shadow
                        drawRoundRect(
                            color = NavyDarkest,
                            topLeft = Offset(0f, 4.dp.toPx()),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
                        )
                    }
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.8.dp, NavyBorder, RoundedCornerShape(22.dp))
            ) {
                NavigationBar(
                    containerColor = NavyPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    Screen.values().forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NavyPrimary,
                                selectedTextColor = YellowAccent,
                                indicatorColor = YellowAccent,
                                unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF94A3B8),
                                unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(280, easing = CubicBezierEasing(0.2f, 0.0f, 0f, 1f))) +
                            scaleIn(
                                initialScale = 0.94f,
                                animationSpec = tween(300, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f))
                            )) togetherWith
                            (fadeOut(animationSpec = tween(200)) +
                                    scaleOut(targetScale = 0.96f, animationSpec = tween(200)))
                },
                label = "3d_screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.HOME -> {
                        HomeScreen(
                            currentMonthRecord = currentRecord,
                            allRecords = chronologicalRecords,
                            userEmail = uiState.currentUserEmail,
                            onAddRecord = { year, month, amount, date, comment, status ->
                                viewModel.addRentRecord(year, month, amount, date, comment, status)
                            },
                            onNavigateToHistory = {
                                currentScreen = Screen.HISTORY
                            }
                        )
                    }

                    Screen.HISTORY -> {
                        HistoryScreen(
                            records = chronologicalRecords,
                            userEmail = uiState.currentUserEmail,
                            onDeleteRecord = { monthKey ->
                                viewModel.deleteRentRecord(monthKey)
                            }
                        )
                    }

                    Screen.ACCOUNT -> {
                        AccountScreen(
                            email = uiState.currentUserEmail,
                            accountCreatedAt = uiState.accountCreatedAt,
                            onSignOut = {
                                viewModel.signOut()
                            }
                        )
                    }
                }
            }
        }
    }
}
