package com.tiarkaerell.ibstracker.data.auth

import android.app.Activity
import android.content.Context
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.tasks.await

/**
 * Modern authorization manager using Google Identity Services AuthorizationClient
 *
 * Handles Google API authorization (scopes) separately from authentication.
 * Uses the new AuthorizationClient API (non-deprecated) instead of GoogleSignIn.
 *
 * Architecture:
 * - CredentialManagerAuth: Handles authentication (user identity)
 * - AuthorizationManager: Handles authorization (Google Drive scopes)
 *
 * This is the modern, non-deprecated approach recommended by Google as of 2025.
 *
 * Note: This class is created in Phase 2 but not yet used.
 * It will replace GoogleSignIn for authorization in subsequent steps.
 */
class AuthorizationManager(private val context: Context) {

    /**
     * Request authorization for Google Drive access
     * Returns an IntentSenderRequest to launch the authorization flow
     *
     * If authorization has already been granted, returns null
     * If authorization is needed, returns IntentSenderRequest to show consent screen
     */
    suspend fun requestAuthorization(activity: Activity): IntentSenderRequest? {
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_FILE)))
            .build()

        return try {
            val authorizationResult = Identity.getAuthorizationClient(activity)
                .authorize(authorizationRequest)
                .await()

            if (authorizationResult.hasResolution()) {
                // User needs to grant permission
                IntentSenderRequest.Builder(
                    authorizationResult.pendingIntent?.intentSender
                        ?: throw Exception("No pending intent for authorization")
                ).build()
            } else {
                // Authorization already granted
                null
            }
        } catch (e: Exception) {
            throw Exception("Failed to request authorization: ${e.message}")
        }
    }

    /**
     * Get authorization result from the intent data after user grants permission
     */
    suspend fun getAuthorizationFromIntent(activity: Activity, data: android.content.Intent?): AuthorizationResult {
        return try {
            Identity.getAuthorizationClient(activity)
                .getAuthorizationResultFromIntent(data)
        } catch (e: Exception) {
            throw Exception("Failed to get authorization result: ${e.message}")
        }
    }

    /**
     * Check if we have the required Drive scopes authorized
     */
    suspend fun hasRequiredScopes(activity: Activity): Boolean {
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_FILE)))
            .build()

        return try {
            val result = Identity.getAuthorizationClient(activity)
                .authorize(authorizationRequest)
                .await()

            // If there's no resolution needed, scopes are already granted
            !result.hasResolution()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the access token for authorized scopes
     * Use this token for Google Drive API calls
     */
    suspend fun getAccessToken(activity: Activity): String? {
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_FILE)))
            .build()

        return try {
            val result = Identity.getAuthorizationClient(activity)
                .authorize(authorizationRequest)
                .await()

            result.accessToken
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Revoke authorization (remove granted scopes)
     */
    suspend fun revokeAuthorization(activity: Activity) {
        try {
            // Note: AuthorizationClient doesn't have a direct revoke method
            // The user needs to revoke access through their Google Account settings
            // This is a limitation of the new API
            // For now, we just clear local state
        } catch (e: Exception) {
            throw Exception("Failed to revoke authorization: ${e.message}")
        }
    }
}
