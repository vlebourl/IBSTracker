package com.tiarkaerell.ibstracker.data.backup

import android.content.Context
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.model.backup.BackupFile
import com.tiarkaerell.ibstracker.data.model.backup.BackupLocation
import com.tiarkaerell.ibstracker.data.model.backup.BackupResult
import com.tiarkaerell.ibstracker.data.model.backup.BackupError
import com.tiarkaerell.ibstracker.data.repository.SettingsRepository
import com.tiarkaerell.ibstracker.data.sync.GoogleDriveBackup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * GoogleDriveService - Wrapper for Google Drive backup operations.
 *
 * Integrates existing GoogleDriveBackup class with new BackupManager system.
 * Provides a consistent interface for cloud backup operations that matches
 * the local backup API.
 *
 * Key Responsibilities:
 * - Upload local backups to Google Drive
 * - Download cloud backups to local storage
 * - List available cloud backups
 * - Delete old cloud backups
 * - Maintain maximum 30 cloud backups
 *
 * Architecture:
 * - Reuses existing GoogleDriveBackup implementation
 * - Converts between BackupManager format and GoogleDrive format
 * - Provides Flow-based reactive API
 *
 * @param context Application context
 * @param database Room database instance
 * @param settingsRepository Settings for Google auth tokens
 */
class GoogleDriveService(
    private val context: Context,
    private val database: AppDatabase,
    private val settingsRepository: SettingsRepository
) {

    private val googleDriveBackup = GoogleDriveBackup(context, database, settingsRepository)

    companion object {
        private const val MAX_CLOUD_BACKUPS = 30
    }

    /**
     * Uploads a local backup file to Google Drive.
     *
     * Uses existing GoogleDriveBackup.createBackup() which:
     * - Creates JSON backup from current database state
     * - Optionally encrypts with user password
     * - Uploads to Google Drive appDataFolder
     *
     * @param accessToken Google OAuth access token
     * @param isAutoBackup If true, uses fixed filename and overwrites previous auto-backup; if false, uses timestamped filename
     * @return BackupResult.Success with Drive file ID, or BackupResult.Failure
     */
    suspend fun uploadBackupToDrive(accessToken: String?, isAutoBackup: Boolean = true): BackupResult {
        return try {
            android.util.Log.d("GoogleDriveService", "uploadBackupToDrive() called with isAutoBackup=$isAutoBackup")
            val result = googleDriveBackup.createBackup(accessToken, isAutoBackup)

            if (result.isSuccess) {
                // Create a placeholder BackupFile for cloud uploads
                val placeholderFile = BackupFile(
                    id = "cloud-${System.currentTimeMillis()}",
                    fileName = "cloud_backup",
                    filePath = "", // No local path for cloud-only backups
                    location = BackupLocation.CLOUD,
                    timestamp = System.currentTimeMillis(),
                    sizeBytes = 0L,
                    databaseVersion = 10,
                    checksum = ""
                )

                BackupResult.Success(
                    backupFile = placeholderFile,
                    durationMs = 0L // Not tracked by existing implementation
                )
            } else {
                val error = result.exceptionOrNull()
                BackupResult.Failure(
                    error = when {
                        error?.message?.contains("Not authorized") == true ->
                            BackupError.AUTHENTICATION_FAILED
                        error?.message?.contains("network") == true ->
                            BackupError.NETWORK_UNAVAILABLE
                        else -> BackupError.UNKNOWN
                    },
                    message = error?.message ?: "Failed to upload backup"
                )
            }
        } catch (e: Exception) {
            BackupResult.Failure(
                error = BackupError.UNKNOWN,
                message = e.message ?: "Failed to upload backup"
            )
        }
    }

    /**
     * Lists all available cloud backups from Google Drive.
     *
     * Returns backups sorted by creation time (newest first).
     * Maps GoogleDriveBackup.DriveFile to our BackupFile format.
     *
     * @param accessToken Google OAuth access token
     * @return Flow emitting list of cloud backup files
     */
    fun listCloudBackups(accessToken: String?): Flow<List<BackupFile>> = flow {
        try {
            val result = googleDriveBackup.listBackups(accessToken)

            if (result.isSuccess) {
                val driveFiles = result.getOrNull() ?: emptyList()
                val backupFiles = driveFiles.map { driveFile ->
                    BackupFile(
                        id = driveFile.id,
                        fileName = driveFile.name,
                        filePath = driveFile.id, // Use Drive file ID as path for cloud files
                        location = BackupLocation.CLOUD,
                        timestamp = driveFile.createdTime,
                        sizeBytes = driveFile.size,
                        databaseVersion = extractVersionFromFilename(driveFile.name),
                        checksum = "" // Not available for cloud files
                    )
                }
                emit(backupFiles)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Downloads a cloud backup file to a temporary local file.
     *
     * This method creates a temporary file in the app's cache directory,
     * downloads the backup from Google Drive, and returns the File reference.
     * The caller is responsible for deleting the temp file after use.
     *
     * Note: Existing GoogleDriveBackup only supports direct database restore.
     * This implementation will use the Drive API directly to download the file.
     *
     * @param fileId Google Drive file ID
     * @param accessToken Google OAuth access token
     * @return File pointing to downloaded backup, or null on failure
     */
    suspend fun downloadBackupFromDrive(
        fileId: String,
        accessToken: String?
    ): File? {
        return try {
            if (accessToken.isNullOrEmpty()) {
                return null
            }

            // Create temporary file in cache directory
            val tempFile = File(context.cacheDir, "temp_cloud_backup_${System.currentTimeMillis()}.db")

            // Use existing GoogleDriveBackup to download the file
            // Note: The current GoogleDriveBackup.restoreBackup() restores directly to DB
            // We need to modify this to download to file instead
            // For now, we'll use a workaround that downloads the content

            val result = googleDriveBackup.downloadBackupToFile(fileId, tempFile, accessToken)

            if (result.isSuccess) {
                tempFile
            } else {
                tempFile.delete()
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Deletes a cloud backup from Google Drive.
     *
     * Uses GoogleDriveBackup.deleteBackup() to perform the actual deletion.
     * Includes comprehensive logging for debugging.
     *
     * @param fileId Google Drive file ID
     * @param accessToken Google OAuth access token
     * @return true if deleted successfully, false otherwise
     */
    suspend fun deleteCloudBackup(
        fileId: String,
        accessToken: String?
    ): Boolean {
        return try {
            android.util.Log.d("GoogleDriveService", "deleteCloudBackup() called with fileId=$fileId")

            if (accessToken == null) {
                android.util.Log.e("GoogleDriveService", "deleteCloudBackup() failed: Access token is null")
                return false
            }

            android.util.Log.d("GoogleDriveService", "Access token present, calling GoogleDriveBackup.deleteBackup()")
            val result = googleDriveBackup.deleteBackup(fileId, accessToken)

            if (result.isSuccess) {
                android.util.Log.i("GoogleDriveService", "Successfully deleted cloud backup with fileId=$fileId")
                true
            } else {
                val error = result.exceptionOrNull()
                android.util.Log.e("GoogleDriveService", "deleteCloudBackup() failed: ${error?.message}", error)
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("GoogleDriveService", "deleteCloudBackup() failed with exception", e)
            false
        }
    }

    /**
     * Cleans up old cloud backups, keeping only MAX_CLOUD_BACKUPS most recent.
     *
     * Lists all backups, sorts by creation time, deletes oldest ones
     * that exceed the maximum limit.
     *
     * NOTE: Auto-backups (auto_cloud_backup_*) are excluded from cleanup
     * since they are managed by the overwrite mechanism.
     *
     * @param accessToken Google OAuth access token
     * @return Number of backups deleted
     */
    suspend fun cleanupOldCloudBackups(accessToken: String?): Int {
        try {
            val result = googleDriveBackup.listBackups(accessToken)

            if (result.isSuccess) {
                val backups = result.getOrNull() ?: emptyList()

                // Filter out auto-backups from cleanup (they manage themselves via overwrite)
                val manualBackups = backups.filter { !it.name.startsWith("auto_cloud_backup") }

                if (manualBackups.size > MAX_CLOUD_BACKUPS) {
                    val toDelete = manualBackups
                        .sortedByDescending { it.createdTime }
                        .drop(MAX_CLOUD_BACKUPS)

                    var deletedCount = 0
                    toDelete.forEach { backup ->
                        if (deleteCloudBackup(backup.id, accessToken)) {
                            deletedCount++
                        }
                    }
                    return deletedCount
                }
            }
        } catch (e: Exception) {
            // Log error but don't throw
        }

        return 0
    }

    /**
     * Checks if user is authenticated with Google Drive.
     *
     * @param accessToken Google OAuth access token
     * @return true if valid access token provided
     */
    fun isAuthenticated(accessToken: String?): Boolean {
        return !accessToken.isNullOrEmpty()
    }

    /**
     * Extracts database version from backup filename.
     *
     * Filename format: ibs_tracker_backup_YYYY-MM-DD_HH-mm-ss.json
     * or ibs_tracker_backup_YYYY-MM-DD_HH-mm-ss.enc
     *
     * Default to current version (10) if can't parse.
     */
    private fun extractVersionFromFilename(filename: String): Int {
        // Existing GoogleDriveBackup doesn't include version in filename
        // Default to current database version
        return 10
    }
}
