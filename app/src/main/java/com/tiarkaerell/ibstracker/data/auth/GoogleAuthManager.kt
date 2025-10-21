package com.tiarkaerell.ibstracker.data.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Google authentication manager using modern Credential Manager API
 *
 * MIGRATION COMPLETE ✅:
 * - Authentication: Credential Manager API (who is the user?)
 * - Authorization: Handled separately by AuthorizationManager (Drive permissions)
 *
 * This class handles ONLY authentication (user identity).
 * For Drive access authorization, use AuthorizationManager directly.
 */
class GoogleAuthManager(private val context: Context) {

    private val credentialAuth = CredentialManagerAuth(context)

    private val _authState = MutableStateFlow<AuthState>(AuthState.NotSignedIn)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Store the credential for account info
    private var currentCredential: GoogleIdTokenCredential? = null

    init {
        // Initial state is NotSignedIn
        // User must sign in explicitly with Credential Manager
        _authState.value = AuthState.NotSignedIn
    }

    /**
     * Sign in with Google using Credential Manager
     * Returns the user's email address on success
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

            _authState.value = AuthState.SignedIn(email)
            Result.success(email)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            Result.failure(e)
        }
    }

    /**
     * Sign out from authentication
     * Note: This does NOT revoke Drive authorization - that's managed by AuthorizationManager
     */
    suspend fun signOut() {
        try {
            credentialAuth.signOut()
            currentCredential = null
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