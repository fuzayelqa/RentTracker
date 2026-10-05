package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.RentTrackerDatabase
import com.example.data.local.entity.AppNotificationEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PropertyEntity
import com.example.data.local.entity.RecurringChargeEntity
import com.example.data.local.entity.RentMonthEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.repository.RentTrackerRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardSummary(
    val totalPaidThisYear: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val totalOverdue: Double = 0.0,
    val nextDueDate: String = "",
    val activePropertiesCount: Int = 0,
    val currentMonthCards: List<RentMonthWithProperty> = emptyList(),
    val overdueAlerts: List<RentMonthWithProperty> = emptyList(),
    val upcomingAlerts: List<RentMonthWithProperty> = emptyList()
)

data class RentMonthWithProperty(
    val rentMonth: RentMonthEntity,
    val property: PropertyEntity,
    val payments: List<PaymentEntity> = emptyList()
)

data class UiState(
    val isAppLocked: Boolean = false,
    val pinError: String? = null,
    val searchQuery: String = "",
    val filterStatus: String = "ALL",
    val filterPropertyId: String? = null,
    val filterYear: Int = DateUtils.getCurrentYear(),
    val paymentDialogMonth: RentMonthEntity? = null,
    val receiptViewingPayment: PaymentEntity? = null,
    val infoMessage: String? = null,
    val isSyncing: Boolean = false,
    val lastSyncTime: String? = null
)

class RentTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RentTrackerRepository
    val syncManager = com.example.data.firebase.FirebaseSyncManager()

    val properties: StateFlow<List<PropertyEntity>>
    val rentMonths: StateFlow<List<RentMonthEntity>>
    val payments: StateFlow<List<PaymentEntity>>
    val notifications: StateFlow<List<AppNotificationEntity>>
    val unreadNotificationsCount: StateFlow<Int>
    val auditLogs: StateFlow<List<AuditLogEntity>>
    val userSettings: StateFlow<UserSettingsEntity>

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val db = RentTrackerDatabase.getDatabase(application)
        repository = RentTrackerRepository(db.rentTrackerDao())

        properties = repository.allProperties.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        rentMonths = repository.allRentMonths.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        payments = repository.allPayments.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        notifications = repository.allNotifications.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        unreadNotificationsCount = repository.unreadNotificationsCount.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )
        auditLogs = repository.allAuditLogs.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        userSettings = repository.userSettings.map { it ?: UserSettingsEntity() }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity()
        )

        // Initial check for lock and ensure current month exists
        viewModelScope.launch {
            val settings = repository.getUserSettingsSync()
            if (settings.pinEnabled && settings.pinCode.isNotBlank()) {
                _uiState.value = _uiState.value.copy(isAppLocked = true)
            }
            // Seed sample data if database is fresh
            val currentProps = repository.getUserSettingsSync()
            val propertyList = repository.allProperties
        }
    }

    // Dynamic combined dashboard summary
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        properties,
        rentMonths,
        payments
    ) { props, months, pays ->
        val propMap = props.associateBy { it.id }
        val payMap = pays.groupBy { it.rentMonthId }
        val activeProps = props.filter { it.status != "Archived" }
        val currentYear = DateUtils.getCurrentYear()
        val currentMonth = DateUtils.getCurrentMonth()

        val yearMonths = months.filter { it.year == currentYear }
        val totalPaidYear = yearMonths.sumOf { it.totalPaid }
        val totalOutstanding = yearMonths.sumOf { it.balance.coerceAtLeast(0.0) }
        val totalOverdue = months.filter { it.status == "OVERDUE" }.sumOf { it.balance.coerceAtLeast(0.0) }

        // Find upcoming due date
        val futureMonths = months.filter { !DateUtils.isOverdue(it.dueDate) && it.balance > 0.0 }
            .sortedBy { it.dueDate }
        val nextDue = futureMonths.firstOrNull()?.dueDate ?: "None upcoming"

        // Current month rent cards
        val currentMonthRentList = months.filter { it.year == currentYear && it.month == currentMonth }
            .mapNotNull { month ->
                propMap[month.propertyId]?.let { prop ->
                    RentMonthWithProperty(month, prop, payMap[month.id] ?: emptyList())
                }
            }

        // Overdue alerts
        val overdueAlerts = months.filter { it.status == "OVERDUE" }
            .mapNotNull { month ->
                propMap[month.propertyId]?.let { prop ->
                    RentMonthWithProperty(month, prop, payMap[month.id] ?: emptyList())
                }
            }

        // Upcoming due alerts (due in <= 7 days)
        val upcomingAlerts = months.filter {
            val days = DateUtils.getDaysUntilDue(it.dueDate)
            days in 0..7 && it.balance > 0.0
        }.mapNotNull { month ->
            propMap[month.propertyId]?.let { prop ->
                RentMonthWithProperty(month, prop, payMap[month.id] ?: emptyList())
            }
        }

        DashboardSummary(
            totalPaidThisYear = totalPaidYear,
            totalOutstanding = totalOutstanding,
            totalOverdue = totalOverdue,
            nextDueDate = nextDue,
            activePropertiesCount = activeProps.size,
            currentMonthCards = currentMonthRentList,
            overdueAlerts = overdueAlerts,
            upcomingAlerts = upcomingAlerts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    fun unlockWithPin(pin: String): Boolean {
        val currentSettings = userSettings.value
        return if (pin == currentSettings.pinCode) {
            _uiState.value = _uiState.value.copy(isAppLocked = false, pinError = null)
            true
        } else {
            _uiState.value = _uiState.value.copy(pinError = "Incorrect PIN. Please try again.")
            false
        }
    }

    fun lockApp() {
        if (userSettings.value.pinEnabled) {
            _uiState.value = _uiState.value.copy(isAppLocked = true, pinError = null)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setFilterStatus(status: String) {
        _uiState.value = _uiState.value.copy(filterStatus = status)
    }

    fun setFilterPropertyId(id: String?) {
        _uiState.value = _uiState.value.copy(filterPropertyId = id)
    }

    fun setFilterYear(year: Int) {
        _uiState.value = _uiState.value.copy(filterYear = year)
    }

    fun openPaymentDialog(rentMonth: RentMonthEntity) {
        _uiState.value = _uiState.value.copy(paymentDialogMonth = rentMonth)
    }

    fun closePaymentDialog() {
        _uiState.value = _uiState.value.copy(paymentDialogMonth = null)
    }

    fun viewReceipt(payment: PaymentEntity) {
        _uiState.value = _uiState.value.copy(receiptViewingPayment = payment)
    }

    fun closeReceiptView() {
        _uiState.value = _uiState.value.copy(receiptViewingPayment = null)
    }

    fun recordPayment(
        propertyId: String,
        rentMonthId: String,
        amount: Double,
        paidDate: String,
        method: String,
        reference: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.recordPayment(
                propertyId = propertyId,
                rentMonthId = rentMonthId,
                amount = amount,
                paidDate = paidDate,
                method = method,
                reference = reference,
                note = note
            )
            closePaymentDialog()
            _uiState.value = _uiState.value.copy(infoMessage = "Payment recorded successfully!")
        }
    }

    fun deletePayment(payment: PaymentEntity) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            _uiState.value = _uiState.value.copy(infoMessage = "Payment record deleted.")
        }
    }

    fun addProperty(property: PropertyEntity, charges: List<Pair<String, Double>> = emptyList()) {
        viewModelScope.launch {
            repository.addProperty(property, charges)
            _uiState.value = _uiState.value.copy(infoMessage = "Property added successfully.")
        }
    }

    fun updateProperty(property: PropertyEntity) {
        viewModelScope.launch {
            repository.updateProperty(property)
            _uiState.value = _uiState.value.copy(infoMessage = "Property updated.")
        }
    }

    fun archiveProperty(propertyId: String) {
        viewModelScope.launch {
            repository.archiveProperty(propertyId)
            _uiState.value = _uiState.value.copy(infoMessage = "Property archived.")
        }
    }

    fun deleteProperty(propertyId: String) {
        viewModelScope.launch {
            repository.deleteProperty(propertyId)
            _uiState.value = _uiState.value.copy(infoMessage = "Property deleted.")
        }
    }

    fun addRecurringCharge(propertyId: String, name: String, amount: Double) {
        viewModelScope.launch {
            repository.addRecurringCharge(
                RecurringChargeEntity(
                    propertyId = propertyId,
                    name = name,
                    amount = amount
                )
            )
        }
    }

    fun deleteRecurringCharge(id: String) {
        viewModelScope.launch {
            repository.deleteRecurringCharge(id)
        }
    }

    fun generateCurrentMonthForProperty(propertyId: String) {
        viewModelScope.launch {
            val y = DateUtils.getCurrentYear()
            val m = DateUtils.getCurrentMonth()
            repository.generateMonthlyRentForProperty(propertyId, y, m)
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun updateUserSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.saveUserSettings(settings)
            _uiState.value = _uiState.value.copy(infoMessage = "Settings updated.")
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedSampleData()
            _uiState.value = _uiState.value.copy(infoMessage = "Sample demo data loaded successfully!")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllUserData()
            _uiState.value = _uiState.value.copy(infoMessage = "All application data cleared.")
        }
    }

    fun syncWithFirebase() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            val result = syncManager.syncLocalToCloud(
                properties = properties.value,
                rentMonths = rentMonths.value,
                payments = payments.value
            )
            val time = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date())
            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    lastSyncTime = time,
                    infoMessage = "Cloud Sync Success: $count records synchronized with renttracker-16415!"
                )
            } else {
                val err = result.exceptionOrNull()?.message ?: "Check network connection"
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    infoMessage = "Sync completed with offline cache: $err"
                )
            }
        }
    }

    fun clearInfoMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }
}
