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
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class GoogleAuthManager(private val context: Context) {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotSignedIn)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
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
    
    init {
        checkExistingSignIn()
    }
    
    private fun checkExistingSignIn() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null && !GoogleSignIn.hasPermissions(account, *getRequiredScopes().toTypedArray())) {
            _authState.value = AuthState.SignedIn(account)
        } else {
            _authState.value = AuthState.NotSignedIn
        }
    }
    
    suspend fun signIn(): Result<GoogleSignInAccount> {
        return try {
            _authState.value = AuthState.Loading
            
            val account = googleSignInClient.silentSignIn().await()
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
    
    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
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