package com.tiarkaerell.ibstracker.data.repository

import com.tiarkaerell.ibstracker.data.backup.BackupManager
import com.tiarkaerell.ibstracker.data.backup.GoogleDriveService
import com.tiarkaerell.ibstracker.data.backup.RestoreManager
import com.tiarkaerell.ibstracker.data.model.backup.BackupFile
import com.tiarkaerell.ibstracker.data.model.backup.BackupResult
import com.tiarkaerell.ibstracker.data.model.backup.BackupSettings
import com.tiarkaerell.ibstracker.data.model.backup.RestoreResult
import com.tiarkaerell.ibstracker.data.preferences.BackupPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implementation of BackupRepository.
 *
 * Coordinates between:
 * - BackupManager (local backup operations)
 * - RestoreManager (restore operations)
 * - GoogleDriveService (cloud sync operations)
 * - BackupPreferences (DataStore for settings)
 *
 * @param backupManager Handles local backup creation and management
 * @param restoreManager Handles restore operations
 * @param googleDriveService Handles Google Drive cloud sync
 * @param backupPreferences DataStore for backup settings
 */
class BackupRepositoryImpl(
    private val backupManager: BackupManager,
    private val restoreManager: RestoreManager,
    private val googleDriveService: GoogleDriveService,
    private val backupPreferences: BackupPreferences
) : BackupRepository {

    // ==================== SETTINGS ====================

    override fun observeSettings(): Flow<BackupSettings> {
        // Combine preferences with real-time storage usage
        return backupPreferences.settingsFlow.map { prefs ->
            val localUsage = backupManager.calculateLocalStorageUsage()
            val localBackups = backupManager.listLocalBackups().first()

            prefs.copy(
                localStorageUsageBytes = localUsage,
                localBackupsCount = localBackups.size,
                totalBackupsCount = localBackups.size, // TODO: Add cloud count when implemented
                cloudStorageUsageBytes = 0L, // TODO: Calculate when cloud sync implemented
                cloudBackupsCount = 0 // TODO: Count when cloud sync implemented
            )
        }
    }

    override suspend fun updateSettings(settings: BackupSettings) {
        backupPreferences.updateLocalBackupsEnabled(settings.localBackupsEnabled)
        backupPreferences.updateCloudSyncEnabled(settings.cloudSyncEnabled)

        if (settings.googleAccountEmail != null) {
            backupPreferences.updateGoogleSignIn(
                email = settings.googleAccountEmail,
                isSignedIn = settings.isGoogleSignedIn
            )
        }
    }

    override suspend fun toggleLocalBackups(enabled: Boolean) {
        backupPreferences.updateLocalBackupsEnabled(enabled)
    }

    override suspend fun toggleCloudSync(enabled: Boolean) {
        backupPreferences.updateCloudSyncEnabled(enabled)
    }

    override suspend fun syncToCloud(): BackupResult {
        // TODO: Get actual access token from GoogleAuthManager
        // For now, this will fail with authentication error
        val accessToken: String? = null

        val result = googleDriveService.uploadBackupToDrive(accessToken)

        // Record timestamp if successful
        if (result is BackupResult.Success) {
            backupPreferences.recordCloudSync(System.currentTimeMillis())
        }

        return result
    }

    // ==================== LOCAL BACKUPS ====================

    override suspend fun createLocalBackup(): BackupResult {
        val result = backupManager.createLocalBackup()

        // Record timestamp if successful
        if (result is BackupResult.Success) {
            backupPreferences.recordLocalBackup(result.backupFile.timestamp)
        }

        return result
    }

    override fun observeLocalBackups(): Flow<List<BackupFile>> {
        return backupManager.listLocalBackups()
    }

    override suspend fun deleteLocalBackup(backupFile: BackupFile): Boolean {
        return backupManager.deleteLocalBackup(backupFile)
    }

    override suspend fun deleteAllLocalBackups(): Int {
        return backupManager.deleteAllLocalBackups()
    }

    // ==================== RESTORE ====================

    override suspend fun restoreFromBackup(backupFile: BackupFile): RestoreResult {
        // TODO: Get access token from GoogleAuthManager for cloud backups
        val accessToken: String? = null
        return restoreManager.restoreFromBackup(backupFile, accessToken)
    }

    override fun isBackupCompatible(backupFile: BackupFile): Boolean {
        return restoreManager.checkDatabaseVersionCompatibility(backupFile.databaseVersion)
    }

    // ==================== CLOUD BACKUPS ====================

    override fun observeCloudBackups(accessToken: String?): Flow<List<BackupFile>> {
        return googleDriveService.listCloudBackups(accessToken)
    }

    override suspend fun deleteCloudBackup(backupFile: BackupFile, accessToken: String?): Boolean {
        return googleDriveService.deleteCloudBackup(backupFile.filePath, accessToken)
    }
}
