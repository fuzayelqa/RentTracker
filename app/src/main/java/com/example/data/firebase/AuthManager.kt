package com.example.data.firebase

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthManager {

    private val tag = "AuthManager"
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(name: String, email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("Account creation failed")

            if (name.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }
            Result.success(user)
        } catch (e: Exception) {
            val friendlyMsg = mapAuthExceptionToMessage(e, isSignUp = true)
            Log.w(tag, "Sign up notice: $friendlyMsg", e)
            Result.failure(Exception(friendlyMsg, e))
        }
    }

    suspend fun signIn(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("Sign in failed")
            Result.success(user)
        } catch (e: Exception) {
            val friendlyMsg = mapAuthExceptionToMessage(e, isSignUp = false)
            Log.w(tag, "Sign in notice: $friendlyMsg", e)
            Result.failure(Exception(friendlyMsg, e))
        }
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.w(tag, "Sign out error", e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val friendlyMsg = mapAuthExceptionToMessage(e, isSignUp = false)
            Result.failure(Exception(friendlyMsg, e))
        }
    }

    private fun mapAuthExceptionToMessage(e: Exception, isSignUp: Boolean): String {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> {
                if (isSignUp) {
                    "The email address format is invalid. Please enter a valid email."
                } else {
                    "Incorrect email or password, or no account exists with this email. If you are new, please tap 'Create Account'."
                }
            }
            is FirebaseAuthInvalidUserException -> {
                "No account found with this email. Please tap 'Create Account' to sign up."
            }
            is FirebaseAuthUserCollisionException -> {
                "An account already exists with this email address. Please switch to 'Sign In'."
            }
            is FirebaseAuthWeakPasswordException -> {
                "Password is too weak. Please use at least 6 characters."
            }
            is FirebaseNetworkException -> {
                "Network connection error. Please verify your internet connection."
            }
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Email/Password sign-in is not enabled in Firebase Console. Please enable it in Firebase Console -> Authentication -> Sign-in method."
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email. Tap 'Create Account' to register."
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again or tap 'Forgot Password'."
                    else -> e.localizedMessage ?: "Authentication error. Please check your credentials."
                }
            }
            else -> e.localizedMessage ?: "Authentication error. Please try again."
        }
    }
}
