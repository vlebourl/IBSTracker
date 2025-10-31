package com.tiarkaerell.ibstracker.data.repository

import com.tiarkaerell.ibstracker.data.model.backup.BackupFile
import com.tiarkaerell.ibstracker.data.model.backup.BackupResult
import com.tiarkaerell.ibstracker.data.model.backup.BackupSettings
import com.tiarkaerell.ibstracker.data.model.backup.RestoreResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for backup and restore operations.
 *
 * Provides:
 * - Backup settings management (local/cloud toggles, timestamps)
 * - Local backup operations (create, list, delete)
 * - Restore operations from local backups
 * - Backup compatibility checking
 *
 * This abstraction allows for:
 * - Easy testing with mock implementations
 * - Future extension for cloud backup providers
 * - Clean separation between business logic and data layer
 */
interface BackupRepository {

    // ==================== SETTINGS ====================

    /**
     * Observes backup settings as a reactive Flow.
     *
     * Emits updates when:
     * - User toggles local/cloud backups
     * - Backup or sync timestamps update
     * - Google account sign-in status changes
     */
    fun observeSettings(): Flow<BackupSettings>

    /**
     * Updates backup settings.
     *
     * @param settings New settings to apply
     */
    suspend fun updateSettings(settings: BackupSettings)

    /**
     * Toggles local backups on/off.
     *
     * @param enabled true to enable, false to disable
     */
    suspend fun toggleLocalBackups(enabled: Boolean)

    /**
     * Toggles cloud sync on/off.
     *
     * @param enabled true to enable, false to disable
     */
    suspend fun toggleCloudSync(enabled: Boolean)

    /**
     * Manually triggers a cloud sync now.
     *
     * @param accessToken OAuth access token for Google Drive
     * @param isAutoBackup If true, uses fixed filename and overwrites previous auto-backup; if false, uses timestamped filename
     * @return BackupResult indicating success or failure
     */
    suspend fun syncToCloud(accessToken: String?, isAutoBackup: Boolean = true): BackupResult

    // ==================== LOCAL BACKUPS ====================

    /**
     * Creates a new local backup.
     *
     * @param isAutoBackup If true, overwrites previous auto-backup; if false, creates timestamped backup
     * @return BackupResult.Success or BackupResult.Failure
     */
    suspend fun createLocalBackup(isAutoBackup: Boolean = true): BackupResult

    /**
     * Observes local backup files as a reactive Flow.
     *
     * Emits sorted list (most recent first) when:
     * - New backup is created
     * - Backup is deleted
     * - Cleanup removes old backups
     */
    fun observeLocalBackups(): Flow<List<BackupFile>>

    /**
     * Deletes a specific local backup.
     *
     * @param backupFile The backup to delete
     * @return true if deleted successfully
     */
    suspend fun deleteLocalBackup(backupFile: BackupFile): Boolean

    /**
     * Deletes all local backups.
     *
     * @return Number of backups deleted
     */
    suspend fun deleteAllLocalBackups(): Int

    // ==================== RESTORE ====================

    /**
     * Restores database from a local backup.
     *
     * IMPORTANT: This operation:
     * - Creates a pre-restore safety backup
     * - Closes the database
     * - Replaces database file
     * - App should restart after restore
     *
     * @param backupFile The backup to restore from
     * @return RestoreResult.Success or RestoreResult.Failure
     */
    suspend fun restoreFromBackup(backupFile: BackupFile): RestoreResult

    /**
     * Checks if a backup is compatible with current database version.
     *
     * @param backupFile The backup to check
     * @return true if compatible, false otherwise
     */
    fun isBackupCompatible(backupFile: BackupFile): Boolean

    /**
     * Imports a custom JSON backup file and adds it to the backups list.
     *
     * This validates the JSON structure, copies it to the backups directory,
     * and makes it available in the local backups list.
     *
     * @param context Android context for content resolver
     * @param uri Uri of the JSON file to import
     * @return BackupResult.Success with the imported backup file, or BackupResult.Failure
     */
    suspend fun importCustomBackup(context: android.content.Context, uri: android.net.Uri): BackupResult

    // ==================== CLOUD BACKUPS ====================

    /**
     * Observes cloud backup files as a reactive Flow.
     *
     * Emits sorted list (most recent first) when:
     * - User signs into Google account
     * - Cloud sync completes
     * - Cloud backup is deleted
     *
     * @param accessToken OAuth access token for Google Drive
     */
    fun observeCloudBackups(accessToken: String?): Flow<List<BackupFile>>

    /**
     * Deletes a specific cloud backup from Google Drive.
     *
     * @param backupFile The cloud backup to delete
     * @param accessToken OAuth access token
     * @return true if deleted successfully
     */
    suspend fun deleteCloudBackup(backupFile: BackupFile, accessToken: String?): Boolean
}
