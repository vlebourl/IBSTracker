package com.tiarkaerell.ibstracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tiarkaerell.ibstracker.data.model.backup.BackupSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create DataStore instance
private val Context.backupDataStore: DataStore<Preferences> by preferencesDataStore(name = "backup_settings")

/**
 * DataStore repository for backup settings persistence.
 */
class BackupPreferences(private val context: Context) {

    private object Keys {
        val LOCAL_BACKUPS_ENABLED = booleanPreferencesKey("local_backups_enabled")
        val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
        val LAST_LOCAL_BACKUP = longPreferencesKey("last_local_backup")
        val LAST_CLOUD_SYNC = longPreferencesKey("last_cloud_sync")
        val GOOGLE_ACCOUNT_EMAIL = stringPreferencesKey("google_account_email")
        val IS_GOOGLE_SIGNED_IN = booleanPreferencesKey("is_google_signed_in")
    }

    /**
     * Observes backup settings as a reactive Flow.
     */
    val settingsFlow: Flow<BackupSettings> = context.backupDataStore.data.map { prefs ->
        BackupSettings(
            localBackupsEnabled = prefs[Keys.LOCAL_BACKUPS_ENABLED] ?: true,
            cloudSyncEnabled = prefs[Keys.CLOUD_SYNC_ENABLED] ?: true,
            lastLocalBackupTimestamp = prefs[Keys.LAST_LOCAL_BACKUP],
            lastCloudSyncTimestamp = prefs[Keys.LAST_CLOUD_SYNC],
            googleAccountEmail = prefs[Keys.GOOGLE_ACCOUNT_EMAIL],
            isGoogleSignedIn = prefs[Keys.IS_GOOGLE_SIGNED_IN] ?: false
        )
    }

    /**
     * Updates local backups enabled setting.
     */
    suspend fun updateLocalBackupsEnabled(enabled: Boolean) {
        context.backupDataStore.edit { prefs ->
            prefs[Keys.LOCAL_BACKUPS_ENABLED] = enabled
        }
    }

    /**
     * Updates cloud sync enabled setting.
     */
    suspend fun updateCloudSyncEnabled(enabled: Boolean) {
        context.backupDataStore.edit { prefs ->
            prefs[Keys.CLOUD_SYNC_ENABLED] = enabled
        }
    }

    /**
     * Records a successful local backup.
     */
    suspend fun recordLocalBackup(timestamp: Long) {
        context.backupDataStore.edit { prefs ->
            prefs[Keys.LAST_LOCAL_BACKUP] = timestamp
        }
    }

    /**
     * Records a successful cloud sync.
     */
    suspend fun recordCloudSync(timestamp: Long) {
        context.backupDataStore.edit { prefs ->
            prefs[Keys.LAST_CLOUD_SYNC] = timestamp
        }
    }

    /**
     * Updates Google Sign-In status.
     */
    suspend fun updateGoogleSignIn(email: String?, isSignedIn: Boolean) {
        context.backupDataStore.edit { prefs ->
            if (email != null) {
                prefs[Keys.GOOGLE_ACCOUNT_EMAIL] = email
            } else {
                prefs.remove(Keys.GOOGLE_ACCOUNT_EMAIL)
            }
            prefs[Keys.IS_GOOGLE_SIGNED_IN] = isSignedIn
        }
    }

    /**
     * Clears all backup preferences.
     */
    suspend fun clear() {
        context.backupDataStore.edit { it.clear() }
    }
}
