package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.PropertyEntity
import com.example.ui.RentTrackerViewModel
import com.example.ui.components.AddPaymentDialog
import com.example.ui.components.PaymentReceiptDialog
import com.example.ui.components.PinLockScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PropertiesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen

enum class Screen(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    HISTORY("History", Icons.Default.ReceiptLong),
    PROPERTIES("Properties", Icons.Default.Apartment),
    REPORTS("Reports", Icons.Default.Assessment),
    SETTINGS("Settings", Icons.Default.Settings),
    NOTIFICATIONS("Notifications", Icons.Default.Home)
}

@Composable
fun RentTrackerApp(viewModel: RentTrackerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val properties by viewModel.properties.collectAsStateWithLifecycle()
    val rentMonths by viewModel.rentMonths.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val dashboardSummary by viewModel.dashboardSummary.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var previousScreen by remember { mutableStateOf(Screen.HOME) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearInfoMessage()
        }
    }

    // If PIN lock is active, render PinLockScreen exclusively
    if (uiState.isAppLocked) {
        PinLockScreen(
            errorMessage = uiState.pinError,
            onPinComplete = { pin ->
                viewModel.unlockWithPin(pin)
            }
        )
        return
    }

    // Secondary screen back handler
    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = if (currentScreen == Screen.NOTIFICATIONS) previousScreen else Screen.HOME
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentScreen != Screen.NOTIFICATIONS) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("main_bottom_nav")
                ) {
                    val navItems = listOf(
                        Screen.HOME,
                        Screen.HISTORY,
                        Screen.PROPERTIES,
                        Screen.REPORTS,
                        Screen.SETTINGS
                    )
                    navItems.forEach { screen ->
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
                            label = { Text(screen.title, fontSize = 11.sp) },
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
            when (currentScreen) {
                Screen.HOME -> {
                    DashboardScreen(
                        summary = dashboardSummary,
                        properties = properties,
                        userSettings = userSettings,
                        unreadNotifications = unreadNotifications,
                        onNavigateToNotifications = {
                            previousScreen = Screen.HOME
                            currentScreen = Screen.NOTIFICATIONS
                        },
                        onNavigateToProperties = { currentScreen = Screen.PROPERTIES },
                        onNavigateToHistory = { currentScreen = Screen.HISTORY },
                        onNavigateToReports = { currentScreen = Screen.REPORTS },
                        onOpenAddPayment = { rentMonth -> viewModel.openPaymentDialog(rentMonth) },
                        onOpenAddProperty = { currentScreen = Screen.PROPERTIES },
                        onSeedDemoData = { viewModel.seedDemoData() }
                    )
                }

                Screen.PROPERTIES -> {
                    PropertiesScreen(
                        properties = properties,
                        onAddProperty = { newProp, charges -> viewModel.addProperty(newProp, charges) },
                        onArchiveProperty = { id -> viewModel.archiveProperty(id) },
                        onDeleteProperty = { id -> viewModel.deleteProperty(id) },
                        onGenerateMonth = { id -> viewModel.generateCurrentMonthForProperty(id) }
                    )
                }

                Screen.HISTORY -> {
                    HistoryScreen(
                        rentMonths = rentMonths,
                        properties = properties,
                        payments = payments,
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        statusFilter = uiState.filterStatus,
                        onStatusFilterChange = { viewModel.setFilterStatus(it) },
                        selectedPropertyId = uiState.filterPropertyId,
                        onPropertyFilterChange = { viewModel.setFilterPropertyId(it) },
                        onOpenAddPayment = { rentMonth -> viewModel.openPaymentDialog(rentMonth) },
                        onViewReceipt = { pay -> viewModel.viewReceipt(pay) },
                        onDeletePayment = { pay -> viewModel.deletePayment(pay) }
                    )
                }

                Screen.REPORTS -> {
                    ReportsScreen(
                        rentMonths = rentMonths,
                        properties = properties,
                        payments = payments,
                        defaultCurrency = userSettings.defaultCurrency
                    )
                }

                Screen.SETTINGS -> {
                    SettingsScreen(
                        userSettings = userSettings,
                        auditLogs = auditLogs,
                        isSyncing = uiState.isSyncing,
                        lastSyncTime = uiState.lastSyncTime,
                        onSyncWithFirebase = { viewModel.syncWithFirebase() },
                        onUpdateSettings = { viewModel.updateUserSettings(it) },
                        onLockApp = { viewModel.lockApp() },
                        onSeedDemoData = { viewModel.seedDemoData() },
                        onClearAllData = { viewModel.clearAllData() }
                    )
                }

                Screen.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = notifications,
                        onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
                        onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                        onClearAll = { viewModel.clearAllNotifications() }
                    )
                }
            }
        }
    }

    // Modal: Record Rent Payment Dialog
    if (uiState.paymentDialogMonth != null) {
        val month = uiState.paymentDialogMonth!!
        val prop = properties.find { it.id == month.propertyId }
        AddPaymentDialog(
            rentMonth = month,
            property = prop,
            onDismiss = { viewModel.closePaymentDialog() },
            onSavePayment = { amount, paidDate, method, reference, note ->
                viewModel.recordPayment(
                    propertyId = month.propertyId,
                    rentMonthId = month.id,
                    amount = amount,
                    paidDate = paidDate,
                    method = method,
                    reference = reference,
                    note = note
                )
            }
        )
    }

    // Modal: Digital Payment Receipt Dialog
    if (uiState.receiptViewingPayment != null) {
        val pay = uiState.receiptViewingPayment!!
        val month = rentMonths.find { it.id == pay.rentMonthId }
        val prop = properties.find { it.id == pay.propertyId }
        PaymentReceiptDialog(
            payment = pay,
            rentMonth = month,
            property = prop,
            tenantName = userSettings.tenantName,
            onDismiss = { viewModel.closeReceiptView() }
        )
    }
}
