package com.tiarkaerell.ibstracker.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.tasks.await

/**
 * Modern authentication manager using Android Credential Manager
 *
 * Handles user authentication (identity) using the new Credential Manager API.
 * This replaces the deprecated GoogleSignIn API.
 *
 * Responsibilities:
 * - User authentication (who is the user?)
 * - Get Google ID Token
 * - Get user profile information (email, name, photo)
 *
 * Note: This class handles AUTHENTICATION only, not AUTHORIZATION (scopes/permissions).
 * For Google Drive/API authorization, use AuthorizationClient separately.
 */
class CredentialManagerAuth(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    /**
     * Sign in with Google using Credential Manager
     *
     * @param webClientId The OAuth 2.0 web client ID from Google Cloud Console
     * @param filterByAuthorizedAccounts If true, only show previously signed-in accounts
     * @return GoogleIdTokenCredential containing user info and ID token
     */
    suspend fun signIn(
        webClientId: String,
        filterByAuthorizedAccounts: Boolean = false
    ): Result<GoogleIdTokenCredential> {
        return try {
            // Create Google ID option for Credential Manager
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setAutoSelectEnabled(true)
                .build()

            // Create the credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Get the credential (shows account picker UI if needed)
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )

            // Handle the credential response
            handleSignInResult(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Handle the credential response from Credential Manager
     */
    private fun handleSignInResult(result: GetCredentialResponse): Result<GoogleIdTokenCredential> {
        return try {
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Result.success(googleIdTokenCredential)
                    } else {
                        Result.failure(Exception("Unexpected credential type: ${credential.type}"))
                    }
                }
                else -> {
                    Result.failure(Exception("Unexpected credential type"))
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(Exception("Invalid Google ID token: ${e.message}"))
        }
    }

    /**
     * Sign out - Clear credentials
     *
     * Note: Credential Manager doesn't have a direct sign-out method.
     * To sign out, the next sign-in should use filterByAuthorizedAccounts = false
     * to show all accounts instead of just the previously selected one.
     */
    suspend fun signOut() {
        try {
            // Clear any local state if needed
            // Credential Manager handles account state internally
            // To "sign out", just don't auto-select on next sign-in
        } catch (e: Exception) {
            throw Exception("Sign out failed: ${e.message}")
        }
    }

    /**
     * Check if user is currently signed in
     *
     * @return true if there's an authorized account available
     */
    suspend fun isSignedIn(webClientId: String): Boolean {
        return try {
            // Try to get credential with filterByAuthorizedAccounts = true
            // If this succeeds, user is signed in
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )

            // If we get a credential, user is signed in
            result.credential != null
        } catch (e: Exception) {
            // No authorized accounts available
            false
        }
    }
}
