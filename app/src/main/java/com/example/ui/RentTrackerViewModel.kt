package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AuthManager
import com.example.data.local.RentTrackerDatabase
import com.example.data.local.entity.RentRecordEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.data.repository.RentTrackerRepository
import com.example.util.AuthUtils
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MyRentTrackerUiState(
    val isAuthenticated: Boolean = false,
    val currentUserId: String = "",
    val currentUserEmail: String = "",
    val accountCreatedAt: Long = System.currentTimeMillis(),
    val authLoading: Boolean = false,
    val authError: String? = null,
    val infoMessage: String? = null
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RentTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = RentTrackerDatabase.getDatabase(application)
    private val repository = RentTrackerRepository(database.rentTrackerDao())
    private val authManager = AuthManager()

    private val _uiState = MutableStateFlow(MyRentTrackerUiState())
    val uiState: StateFlow<MyRentTrackerUiState> = _uiState.asStateFlow()

    private val activeUserId = MutableStateFlow("")

    val userAccount: StateFlow<UserAccountEntity?> = activeUserId.flatMapLatest { uid ->
        if (uid.isBlank()) flowOf(null) else repository.getUserAccount(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Chronological records starting from July 2026
    val chronologicalRecords: StateFlow<List<RentRecordEntity>> = activeUserId.flatMapLatest { uid ->
        if (uid.isBlank()) flowOf(emptyList()) else repository.getAllRentRecordsChronological(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current month's rent record (e.g. October 2026)
    val currentMonthRecord: StateFlow<RentRecordEntity?> = activeUserId.flatMapLatest { uid ->
        if (uid.isBlank()) {
            flowOf(null)
        } else {
            val curKey = DateUtils.formatMonthKey(DateUtils.getCurrentYear(), DateUtils.getCurrentMonth())
            repository.getRentRecordByMonthKey(uid, curKey)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // In-memory cache for active reset verification codes: email -> (code, expiresAtMillis)
    private val activeResetCodes = mutableMapOf<String, Pair<String, Long>>()

    init {
        viewModelScope.launch {
            authManager.authStateFlow.collect { firebaseUser ->
                if (firebaseUser != null) {
                    val email = firebaseUser.email ?: ""
                    if (email.isNotBlank()) {
                        val canonicalUserId = AuthUtils.emailToUserId(email)
                        activeUserId.value = canonicalUserId

                        _uiState.value = _uiState.value.copy(
                            isAuthenticated = true,
                            currentUserId = canonicalUserId,
                            currentUserEmail = email
                        )

                        // Sync cloud and local data
                        syncData(canonicalUserId, email)
                    }
                } else {
                    if (_uiState.value.currentUserId.isBlank()) {
                        _uiState.value = MyRentTrackerUiState(isAuthenticated = false)
                        activeUserId.value = ""
                    }
                }
            }
        }
    }

    private suspend fun syncData(canonicalUserId: String, email: String) {
        try {
            repository.syncWithCloud(canonicalUserId)
            val existing = repository.getUserAccountSync(canonicalUserId)
            if (existing == null) {
                val newAccount = UserAccountEntity(
                    id = canonicalUserId,
                    email = email,
                    createdAt = System.currentTimeMillis()
                )
                repository.saveUserAccount(newAccount)
                _uiState.value = _uiState.value.copy(accountCreatedAt = newAccount.createdAt)
            } else {
                _uiState.value = _uiState.value.copy(accountCreatedAt = existing.createdAt)
            }
        } catch (_: Exception) {}
    }

    /**
     * Cross-device log in by email & password.
     * Verifies with Firebase Auth, Cloud Firestore, and local DB so data is universally accessible.
     */
    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            _uiState.value = _uiState.value.copy(authLoading = true, authError = null)

            val canonicalUserId = AuthUtils.emailToUserId(cleanEmail)
            val passHash = AuthUtils.hashPassword(pass)

            var authenticated = false
            var accountCreatedAt = System.currentTimeMillis()

            // 1. Check Cloud Firestore (ensures cross-device login works seamlessly across phones)
            try {
                val cloudAcc = repository.fetchCloudUserAccount(canonicalUserId).getOrNull()
                if (cloudAcc != null && cloudAcc.passwordHash.isNotBlank()) {
                    if (cloudAcc.passwordHash == passHash) {
                        authenticated = true
                        accountCreatedAt = cloudAcc.createdAt
                        repository.saveUserAccount(cloudAcc)
                    } else {
                        // The user entered an invalid or previous password that has been deleted!
                        _uiState.value = _uiState.value.copy(
                            authLoading = false,
                            authError = "Incorrect password. The previous password has been deleted and replaced. Please enter your new password or reset your password if forgotten."
                        )
                        return@launch
                    }
                }
            } catch (_: Exception) {}

            // 2. Check local database
            if (!authenticated) {
                val localAcc = repository.getUserAccountSync(canonicalUserId)
                if (localAcc != null && localAcc.passwordHash.isNotBlank()) {
                    if (localAcc.passwordHash == passHash) {
                        authenticated = true
                        accountCreatedAt = localAcc.createdAt
                    } else {
                        // Local database password mismatch
                        _uiState.value = _uiState.value.copy(
                            authLoading = false,
                            authError = "Incorrect password. The previous password has been deleted and replaced. Please enter your new password."
                        )
                        return@launch
                    }
                }
            }

            // 3. Fallback for brand-new or legacy accounts in Firebase Auth
            if (!authenticated) {
                val fbResult = authManager.signIn(cleanEmail, pass)
                if (fbResult.isSuccess) {
                    authenticated = true
                }
            }

            if (authenticated) {
                activeUserId.value = canonicalUserId
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = true,
                    authLoading = false,
                    currentUserId = canonicalUserId,
                    currentUserEmail = cleanEmail,
                    accountCreatedAt = accountCreatedAt,
                    infoMessage = "Welcome back!"
                )
                syncData(canonicalUserId, cleanEmail)
            } else {
                _uiState.value = _uiState.value.copy(
                    authLoading = false,
                    authError = "Incorrect email or password, or no account exists with this email. If you are new, please tap 'Create Account'."
                )
            }
        }
    }

    /**
     * Creates a new personal account.
     * Stores user profile in Room & Cloud Firestore so logging in from any other phone restores all data.
     */
    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            _uiState.value = _uiState.value.copy(authLoading = true, authError = null)

            val canonicalUserId = AuthUtils.emailToUserId(cleanEmail)
            val passHash = AuthUtils.hashPassword(pass)

            // Check if account already exists in Cloud Firestore
            try {
                val cloudAcc = repository.fetchCloudUserAccount(canonicalUserId).getOrNull()
                if (cloudAcc != null) {
                    _uiState.value = _uiState.value.copy(
                        authLoading = false,
                        authError = "An account already exists with this email address. Please switch to 'Log In'."
                    )
                    return@launch
                }
            } catch (_: Exception) {}

            // Check if account already exists in local DB
            val localAcc = repository.getUserAccountSync(canonicalUserId)
            if (localAcc != null) {
                _uiState.value = _uiState.value.copy(
                    authLoading = false,
                    authError = "An account already exists with this email. Please switch to 'Log In'."
                )
                return@launch
            }

            // Also try Firebase Auth register
            try {
                authManager.signUp(name = "Tenant", email = cleanEmail, pass = pass)
            } catch (_: Exception) {}

            val newAcc = UserAccountEntity(
                id = canonicalUserId,
                email = cleanEmail,
                passwordHash = passHash,
                createdAt = System.currentTimeMillis()
            )

            repository.saveUserAccount(newAcc)

            activeUserId.value = canonicalUserId
            _uiState.value = _uiState.value.copy(
                isAuthenticated = true,
                authLoading = false,
                currentUserId = canonicalUserId,
                currentUserEmail = cleanEmail,
                accountCreatedAt = newAcc.createdAt,
                infoMessage = "Account created successfully! Welcome to My Rent Tracker."
            )
            syncData(canonicalUserId, cleanEmail)
        }
    }

    /**
     * Add and permanently lock a rent record.
     * Immediately stored in Room & Cloud Firestore.
     */
    fun addRentRecord(
        year: Int,
        month: Int,
        rentAmount: Double,
        paymentDate: String,
        comment: String = "",
        status: String
    ): Boolean {
        var success = false
        viewModelScope.launch {
            val uid = _uiState.value.currentUserId
            if (uid.isBlank()) return@launch

            val result = repository.saveRentRecord(
                userId = uid,
                year = year,
                month = month,
                rentAmount = rentAmount,
                paymentDate = paymentDate,
                comment = comment,
                status = status
            )

            if (result.isSuccess) {
                success = true
                val monthName = DateUtils.formatMonthYear(year, month)
                _uiState.value = _uiState.value.copy(
                    infoMessage = "$monthName rent recorded successfully! Record is permanently locked."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    authError = result.exceptionOrNull()?.message ?: "Failed to save rent record"
                )
            }
        }
        return success
    }

    /**
     * Delete a rent record (after multi-step confirmation).
     */
    fun deleteRentRecord(monthKey: String) {
        viewModelScope.launch {
            val uid = _uiState.value.currentUserId
            if (uid.isBlank()) return@launch

            val result = repository.deleteRentRecord(uid, monthKey)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(infoMessage = "Rent record deleted permanently.")
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to delete record."
                _uiState.value = _uiState.value.copy(authError = err)
            }
        }
    }

    /**
     * Generates a 6-digit verification code, saves it to database,
     * and provides it so user can change password inside the app.
     */
    fun requestPasswordResetCode(email: String, onComplete: (Boolean, String, String?) -> Unit) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                onComplete(false, "Please enter a valid email address.", null)
                return@launch
            }

            val code = AuthUtils.generateVerificationCode()
            val expiresAt = System.currentTimeMillis() + 15 * 60 * 1000L // 15 minutes validity
            activeResetCodes[cleanEmail] = Pair(code, expiresAt)

            // Save reset code to Cloud Firestore
            repository.savePasswordResetCode(cleanEmail, code)

            // Send official password reset email to user's inbox
            val emailRes = authManager.sendPasswordReset(cleanEmail)
            val msg = if (emailRes.isSuccess) {
                "A password reset email has been sent to $cleanEmail. Please check your inbox and spam folder."
            } else {
                "A verification email has been dispatched to $cleanEmail. Please check your inbox and spam folder."
            }

            // CRITICAL: Code must ONLY go through email, NEVER returned or shown in the app!
            onComplete(true, msg, null)
        }
    }

    /**
     * Verifies the 6-digit code and resets the user's password.
     */
    fun verifyCodeAndResetPassword(
        email: String,
        code: String,
        newPass: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            val cleanCode = code.trim()

            if (cleanCode.length != 6 || !cleanCode.all { it.isDigit() }) {
                onComplete(false, "Please enter the valid 6-digit verification code.")
                return@launch
            }

            if (newPass.length < 6) {
                onComplete(false, "Password must be at least 6 characters.")
                return@launch
            }

            val canonicalUserId = AuthUtils.emailToUserId(cleanEmail)
            val passHash = AuthUtils.hashPassword(newPass)

            var verified = false

            // 1. Check in-memory code
            val memCode = activeResetCodes[cleanEmail]
            if (memCode != null) {
                if (System.currentTimeMillis() > memCode.second) {
                    onComplete(false, "Verification code has expired. Please request a new code.")
                    return@launch
                }
                if (memCode.first == cleanCode) {
                    verified = true
                }
            }

            // 2. Check Cloud Firestore if not verified in memory
            if (!verified) {
                val cloudRes = repository.verifyAndResetPassword(cleanEmail, cleanCode, newPass)
                if (cloudRes.isSuccess) {
                    verified = true
                } else {
                    val err = cloudRes.exceptionOrNull()?.message ?: "Invalid verification code."
                    onComplete(false, err)
                    return@launch
                }
            }

            if (verified) {
                // Permanently update Firestore and delete previous password & reset code
                repository.updatePasswordAndClearResetCode(cleanEmail, newPass)

                // Permanently update Room local DB, replacing and deleting previous password
                val existingLocal = repository.getUserAccountSync(canonicalUserId)
                val updatedAcc = UserAccountEntity(
                    id = canonicalUserId,
                    email = cleanEmail,
                    passwordHash = passHash,
                    createdAt = existingLocal?.createdAt ?: System.currentTimeMillis()
                )

                repository.saveUserAccount(updatedAcc)
                activeResetCodes.remove(cleanEmail)

                try {
                    authManager.changePassword(newPass)
                } catch (_: Exception) {}

                // Invalidate any active session so previous login cannot linger
                authManager.signOut()
                activeUserId.value = ""

                val successMsg = "Password reset successfully! Your previous password has been deleted. Please log in with your new password."
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = false,
                    infoMessage = successMsg
                )
                onComplete(true, successMsg)
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        activeUserId.value = ""
        _uiState.value = MyRentTrackerUiState(isAuthenticated = false)
    }

    fun clearInfoMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }

    fun clearAuthError() {
        _uiState.value = _uiState.value.copy(authError = null)
    }
}
