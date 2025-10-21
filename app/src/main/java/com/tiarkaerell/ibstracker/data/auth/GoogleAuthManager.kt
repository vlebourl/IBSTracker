package com.tiarkaerell.ibstracker.data.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Hybrid Google authentication and authorization manager
 *
 * Phase 2 - Step 2 (Current):
 * - Uses Credential Manager for authentication (user identity) - NEW ✅
 * - Has AuthorizationManager ready for authorization - NEW (not yet active)
 * - Still uses GoogleSignIn for authorization (scopes/permissions) - OLD (active, deprecated)
 *
 * This step wires up AuthorizationManager but keeps both old and new systems.
 * Next step will switch to using AuthorizationManager.
 */
class GoogleAuthManager(private val context: Context) {

    // NEW: Credential Manager for authentication (already active)
    private val credentialAuth = CredentialManagerAuth(context)

    // NEW: AuthorizationManager for Drive scopes (ready, not yet active)
    private val authorizationManager = AuthorizationManager(context)

    // OLD: GoogleSignIn for authorization (still active, will be removed later)
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(com.tiarkaerell.ibstracker.R.string.default_web_client_id))
            .requestScopes(
                Scope(DriveScopes.DRIVE_FILE),
                Scope("https://www.googleapis.com/auth/fitness.activity.read"),
                Scope("https://www.googleapis.com/auth/fitness.sleep.read")
            )
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.NotSignedIn)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Store the credential for account info
    private var currentCredential: GoogleIdTokenCredential? = null

    init {
        checkExistingSignIn()
    }

    private fun checkExistingSignIn() {
        // Check using old GoogleSignIn for now (includes scopes)
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null && GoogleSignIn.hasPermissions(account, *getRequiredScopes().toTypedArray())) {
            _authState.value = AuthState.SignedIn(account)
        } else {
            _authState.value = AuthState.NotSignedIn
        }
    }
    
    /**
     * Sign in with Google
     *
     * Phase 1: Uses Credential Manager for authentication, then GoogleSignIn for authorization
     * This is a hybrid approach during migration.
     */
    suspend fun signIn(): Result<GoogleSignInAccount> {
        return try {
            _authState.value = AuthState.Loading

            // Step 1: Authenticate with Credential Manager (NEW)
            val webClientId = context.getString(com.tiarkaerell.ibstracker.R.string.default_web_client_id)
            val credentialResult = credentialAuth.signIn(webClientId, filterByAuthorizedAccounts = false)

            if (credentialResult.isFailure) {
                _authState.value = AuthState.Error(credentialResult.exceptionOrNull()?.message ?: "Authentication failed")
                return Result.failure(credentialResult.exceptionOrNull()!!)
            }

            currentCredential = credentialResult.getOrNull()

            // Step 2: Silent sign-in with GoogleSignIn for scopes (OLD - still needed for now)
            // This should succeed if the same account was selected
            val account = try {
                googleSignInClient.silentSignIn().await()
            } catch (e: Exception) {
                // If silent sign-in fails, that's okay - user will use getSignInIntent()
                _authState.value = AuthState.Error("Please complete authorization")
                return Result.failure(Exception("Authorization required - call getSignInIntent()"))
            }

            _authState.value = AuthState.SignedIn(account)
            Result.success(account)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            Result.failure(e)
        }
    }
    
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }
    
    suspend fun handleSignInResult(data: Intent?): Result<GoogleSignInAccount> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            _authState.value = AuthState.SignedIn(account)
            Result.success(account)
        } catch (e: ApiException) {
            _authState.value = AuthState.Error("Sign in failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Sign out from both authentication and authorization
     */
    suspend fun signOut() {
        try {
            // Sign out from old GoogleSignIn (authorization)
            googleSignInClient.signOut().await()

            // Sign out from Credential Manager (authentication)
            credentialAuth.signOut()

            // Clear stored credential
            currentCredential = null

            _authState.value = AuthState.NotSignedIn
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Sign out failed: ${e.message}")
        }
    }
    
    fun getCurrentAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    
    fun hasPermissions(): Boolean {
        val account = getCurrentAccount()
        return account != null && GoogleSignIn.hasPermissions(account, *getRequiredScopes().toTypedArray())
    }
    
    private fun getRequiredScopes(): List<Scope> {
        return listOf(
            Scope(DriveScopes.DRIVE_FILE),
            Scope("https://www.googleapis.com/auth/fitness.activity.read"),
            Scope("https://www.googleapis.com/auth/fitness.sleep.read")
        )
    }
    
    sealed class AuthState {
        object NotSignedIn : AuthState()
        object Loading : AuthState()
        data class SignedIn(val account: GoogleSignInAccount) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}

@Composable
fun rememberGoogleAuthManager(): GoogleAuthManager {
    val context = LocalContext.current
    return remember { GoogleAuthManager(context) }
}