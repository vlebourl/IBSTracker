package com.tiarkaerell.ibstracker.data.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.tiarkaerell.ibstracker.data.preferences.BackupPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Google authentication manager using modern Credential Manager API
 *
 * MIGRATION COMPLETE ✅:
 * - Authentication: Credential Manager API (who is the user?)
 * - Authorization: Handled separately by AuthorizationManager (Drive permissions)
 * - Session Persistence: EncryptedSharedPreferences (local storage)
 *
 * Following Android best practices:
 * - Stores session locally for instant state restoration
 * - Uses Credential Manager for initial authentication
 * - Eliminates "reconnection" flash on app restart
 *
 * This class handles ONLY authentication (user identity).
 * For Drive access authorization, use AuthorizationManager directly.
 */
class GoogleAuthManager(private val context: Context) {

    private val credentialAuth = CredentialManagerAuth(context)
    private val sessionManager = SessionManager(context)
    private val backupPreferences = BackupPreferences(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _authState = MutableStateFlow<AuthState>(AuthState.NotSignedIn)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Store the credential for account info
    private var currentCredential: GoogleIdTokenCredential? = null

    init {
        // Restore signed-in state from local storage instantly (no network call, no UI flash)
        restoreFromLocalStorage()
    }

    /**
     * Restore authentication state from local EncryptedSharedPreferences
     * This is instant (synchronous) and eliminates the "reconnection" flash
     * Also syncs with BackupPreferences on startup
     */
    private fun restoreFromLocalStorage() {
        val savedEmail = sessionManager.getSignedInEmail()
        if (savedEmail != null) {
            _authState.value = AuthState.SignedIn(savedEmail)
            // Sync with BackupPreferences on startup
            scope.launch {
                backupPreferences.updateGoogleSignIn(savedEmail, isSignedIn = true)
            }
        } else {
            _authState.value = AuthState.NotSignedIn
        }
    }

    /**
     * Sign in with Google using Credential Manager
     * Returns the user's email address on success
     * Saves session locally for instant restoration on app restart
     */
    suspend fun signIn(): Result<String> {
        return try {
            _authState.value = AuthState.Loading

            val webClientId = context.getString(com.tiarkaerell.ibstracker.R.string.default_web_client_id)
            val credentialResult = credentialAuth.signIn(webClientId, filterByAuthorizedAccounts = false)

            if (credentialResult.isFailure) {
                _authState.value = AuthState.Error(credentialResult.exceptionOrNull()?.message ?: "Authentication failed")
                return Result.failure(credentialResult.exceptionOrNull()!!)
            }

            currentCredential = credentialResult.getOrNull()
            val email = currentCredential?.id ?: return Result.failure(Exception("No email found"))

            // Save session locally for instant restoration on app restart
            sessionManager.saveSignedInEmail(email)

            // Update BackupPreferences to enable Cloud Sync toggle
            scope.launch {
                backupPreferences.updateGoogleSignIn(email, isSignedIn = true)
            }

            _authState.value = AuthState.SignedIn(email)
            Result.success(email)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            Result.failure(e)
        }
    }

    /**
     * Sign out from authentication
     * Clears local session and Credential Manager state
     * Note: This does NOT revoke Drive authorization - that's managed by AuthorizationManager
     */
    suspend fun signOut() {
        try {
            credentialAuth.signOut()
            currentCredential = null

            // Clear local session storage
            sessionManager.clearSession()

            // Update BackupPreferences to disable Cloud Sync toggle
            scope.launch {
                backupPreferences.updateGoogleSignIn(null, isSignedIn = false)
            }

            _authState.value = AuthState.NotSignedIn
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Sign out failed: ${e.message}")
        }
    }

    /**
     * Get current user's email
     */
    fun getCurrentUserEmail(): String? {
        return currentCredential?.id
    }

    sealed class AuthState {
        object NotSignedIn : AuthState()
        object Loading : AuthState()
        data class SignedIn(val email: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}

@Composable
fun rememberGoogleAuthManager(): GoogleAuthManager {
    val context = LocalContext.current
    return remember { GoogleAuthManager(context) }
}