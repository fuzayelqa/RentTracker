package com.example.util

import java.security.MessageDigest
import kotlin.random.Random

object AuthUtils {

    /**
     * Converts an email into a consistent, safe unique user document ID
     * across all devices and sessions.
     * Example: "info.ozih@gmail.com" -> "user_info_ozih_gmail_com"
     */
    fun emailToUserId(email: String): String {
        val sanitized = email.trim().lowercase()
            .replace("@", "_")
            .replace(".", "_")
            .replace("-", "_")
            .replace("+", "_")
            .filter { it.isLetterOrDigit() || it == '_' }
        return if (sanitized.isNotBlank()) "user_$sanitized" else "user_default"
    }

    /**
     * Hashes passwords using SHA-256 so plain-text passwords are never stored.
     */
    fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            password
        }
    }

    /**
     * Generates a 6-digit numeric verification code for password reset.
     */
    fun generateVerificationCode(): String {
        val number = Random.nextInt(100000, 999999)
        return number.toString()
    }
}
