package com.tiarkaerell.ibstracker.data.backup

import android.content.Context
import androidx.room.RoomDatabase
import com.tiarkaerell.ibstracker.data.backup.BackupFileManager.verifyChecksum
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.model.backup.BackupFile
import com.tiarkaerell.ibstracker.data.model.backup.BackupLocation
import com.tiarkaerell.ibstracker.data.model.backup.RestoreError
import com.tiarkaerell.ibstracker.data.model.backup.RestoreResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * RestoreManager handles database restore operations from local and cloud backups.
 *
 * Key Responsibilities:
 * - Validates backup file integrity (checksum verification)
 * - Checks database version compatibility
 * - Creates pre-restore safety backup
 * - Downloads cloud backups to temporary files
 * - Performs database file replacement
 * - Counts restored items for user feedback
 *
 * Thread Safety: All public methods use Dispatchers.IO for file operations
 *
 * @param context Application context for accessing file storage
 * @param database Room database instance for closing/reopening
 * @param backupManager BackupManager instance for creating pre-restore backup
 * @param googleDriveService GoogleDriveService for downloading cloud backups
 * @param currentDatabaseVersion Current app database schema version
 */
class RestoreManager(
    private val context: Context,
    private val database: RoomDatabase,
    private val backupManager: BackupManager,
    private val googleDriveService: GoogleDriveService? = null,
    private val currentDatabaseVersion: Int
) {
    companion object {
        private const val DATABASE_NAME = "ibs-tracker-database"
        private const val VERSION_TOLERANCE = 0 // Only exact version matches allowed for now
    }

    /**
     * Restores the database from a backup file (local or cloud).
     *
     * Performance target: <3s for typical database size (excluding cloud download time)
     *
     * Steps:
     * 0. If CLOUD backup, download to temporary file first
     * 1. Verify backup file exists and checksum is valid
     * 2. Check database version compatibility
     * 3. Create pre-restore safety backup of current data
     * 4. Close database connection
     * 5. Replace database file with backup
     * 6. Reopen database (Room will handle WAL files automatically)
     * 7. Count restored items
     * 8. Clean up temporary file if cloud backup
     *
     * @param backupFile The backup file to restore from
     * @param accessToken OAuth access token for cloud backups (required if location == CLOUD)
     * @return RestoreResult.Success with item count and duration, or RestoreResult.Failure
     */
    suspend fun restoreFromBackup(
        backupFile: BackupFile,
        accessToken: String? = null
    ): RestoreResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var tempCloudFile: File? = null

        try {
            // Step 0: Download cloud backup if needed
            val actualBackupFile = if (backupFile.location == BackupLocation.CLOUD) {
                if (googleDriveService == null) {
                    return@withContext RestoreResult.Failure(
                        error = RestoreError.DOWNLOAD_FAILED,
                        message = "GoogleDriveService not available for cloud restore"
                    )
                }

                val downloadedFile = googleDriveService.downloadBackupFromDrive(
                    fileId = backupFile.filePath, // filePath contains Drive file ID for cloud backups
                    accessToken = accessToken
                )

                if (downloadedFile == null || !downloadedFile.exists()) {
                    return@withContext RestoreResult.Failure(
                        error = RestoreError.DOWNLOAD_FAILED,
                        message = "Failed to download cloud backup"
                    )
                }

                tempCloudFile = downloadedFile
                // Create new BackupFile pointing to downloaded temp file
                backupFile.copy(
                    filePath = downloadedFile.absolutePath,
                    location = BackupLocation.LOCAL // Now it's a local file
                )
            } else {
                backupFile
            }

            // Step 1: Verify backup file
            val verificationError = verifyBackupFile(actualBackupFile)
            if (verificationError != null) {
                return@withContext RestoreResult.Failure(
                    error = verificationError,
                    message = "Backup file verification failed"
                )
            }

            // Step 2: Check version compatibility
            if (!checkDatabaseVersionCompatibility(actualBackupFile.databaseVersion)) {
                return@withContext RestoreResult.Failure(
                    error = RestoreError.VERSION_MISMATCH,
                    message = "Backup version ${actualBackupFile.databaseVersion} incompatible with current version $currentDatabaseVersion"
                )
            }

            // Step 3: Create pre-restore safety backup
            val preRestoreBackup = createPreRestoreBackup()
            if (preRestoreBackup == null) {
                return@withContext RestoreResult.Failure(
                    error = RestoreError.RESTORE_INTERRUPTED,
                    message = "Failed to create pre-restore safety backup"
                )
            }

            // Step 4-6: Perform database restore
            val restoreSuccess = performDatabaseRestore(actualBackupFile)
            if (!restoreSuccess) {
                return@withContext RestoreResult.Failure(
                    error = RestoreError.RESTORE_INTERRUPTED,
                    message = "Database file replacement failed"
                )
            }

            // Step 7: Count restored items
            val itemCount = countRestoredItems()

            val durationMs = System.currentTimeMillis() - startTime
            RestoreResult.Success(
                itemsRestored = itemCount,
                backupFile = backupFile,
                durationMs = durationMs,
                requiresRestart = true // SQLite restore requires app restart
            )

        } catch (e: Exception) {
            RestoreResult.Failure(
                error = RestoreError.UNKNOWN,
                message = "Unexpected error during restore: ${e.message}",
                cause = e
            )
        } finally {
            // Step 8: Clean up temporary cloud backup file
            tempCloudFile?.let {
                try {
                    if (it.exists()) {
                        it.delete()
                    }
                } catch (e: Exception) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    /**
     * Verifies backup file integrity.
     *
     * Checks:
     * - File exists
     * - Checksum matches expected value
     *
     * @param backupFile The backup file to verify
     * @return RestoreError if verification fails, null if valid
     */
    fun verifyBackupFile(backupFile: BackupFile): RestoreError? {
        val file = File(backupFile.filePath)

        // Check file exists
        if (!file.exists()) {
            return RestoreError.FILE_NOT_FOUND
        }

        // Verify checksum
        val isValid = file.verifyChecksum(backupFile.checksum)
        if (!isValid) {
            return RestoreError.FILE_CORRUPTED
        }

        return null
    }

    /**
     * Checks if backup database version is compatible with current app version.
     *
     * Strategy: Exact match only for safety (future versions may support migrations)
     *
     * @param backupVersion Database version from backup
     * @return true if compatible, false otherwise
     */
    fun checkDatabaseVersionCompatibility(backupVersion: Int): Boolean {
        return backupVersion == currentDatabaseVersion
    }

    /**
     * Creates a safety backup before restore operation.
     *
     * This allows rollback if restore fails or user wants to undo.
     *
     * @return BackupFile if successful, null otherwise
     */
    suspend fun createPreRestoreBackup(): BackupFile? {
        val result = backupManager.createLocalBackup()
        return when (result) {
            is com.tiarkaerell.ibstracker.data.model.backup.BackupResult.Success -> result.backupFile
            is com.tiarkaerell.ibstracker.data.model.backup.BackupResult.Failure -> null
        }
    }

    /**
     * Performs the actual database file replacement.
     *
     * Steps:
     * 1. Close database connection
     * 2. Copy backup file to database location
     * 3. Delete WAL and SHM files (Room will recreate)
     *
     * @param backupFile The backup file to restore from
     * @return true if successful, false otherwise
     */
    suspend fun performDatabaseRestore(backupFile: BackupFile): Boolean = withContext(Dispatchers.IO) {
        try {
            // Step 1: Close database connection
            database.close()

            // Step 2: Replace database file
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            val backupSource = File(backupFile.filePath)

            if (!backupSource.exists()) {
                return@withContext false
            }

            // Copy backup to database location
            backupSource.copyTo(dbFile, overwrite = true)

            // Step 3: Clean up WAL and SHM files
            val walFile = File(dbFile.absolutePath + "-wal")
            val shmFile = File(dbFile.absolutePath + "-shm")

            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Counts items in the restored database.
     *
     * Counts:
     * - Food items
     * - Symptoms
     *
     * @return Total count of restored items
     */
    suspend fun countRestoredItems(): Int = withContext(Dispatchers.IO) {
        // Note: Database is closed during restore, so we can't query it directly
        // This method would need to be called after the database is reopened
        // For now, we return 0 as a placeholder
        // In production, this would require reopening the database and querying
        0
    }

    /**
     * Restores data from a JSON backup file by inserting into the database.
     *
     * This method:
     * - Parses JSON backup content
     * - Validates version compatibility
     * - Inserts food items and symptoms into database
     * - Does NOT require app restart (hot-reload)
     * - Does NOT overwrite existing data (uses database auto-increment IDs)
     *
     * @param jsonContent Raw JSON backup content
     * @return RestoreResult.Success with count, or RestoreResult.Failure
     */
    suspend fun restoreFromJson(jsonContent: String): RestoreResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            // Parse JSON backup
            val parsed = try {
                JsonBackupParser.parseBackup(jsonContent, currentDatabaseVersion)
            } catch (e: IllegalArgumentException) {
                return@withContext RestoreResult.Failure(
                    error = RestoreError.FILE_CORRUPTED,
                    message = "JSON parsing failed: ${e.message}",
                    cause = e
                )
            }

            // Get database instance (must be AppDatabase)
            val appDatabase = database as? AppDatabase
                ?: return@withContext RestoreResult.Failure(
                    error = RestoreError.UNKNOWN,
                    message = "Database instance is not AppDatabase"
                )

            // Insert food items (with ID = 0 to use auto-increment)
            val foodItemsToInsert = parsed.foodItems.map { it.copy(id = 0) }
            appDatabase.foodItemDao().insertAll(foodItemsToInsert)

            // Insert symptoms (with ID = 0 to use auto-increment)
            val symptomsToInsert = parsed.symptoms.map { it.copy(id = 0) }
            // SymptomDao doesn't have insertAll, so insert one by one
            symptomsToInsert.forEach { symptom ->
                appDatabase.symptomDao().insert(symptom)
            }

            val totalItems = foodItemsToInsert.size + symptomsToInsert.size
            val durationMs = System.currentTimeMillis() - startTime

            // Create a placeholder BackupFile for the result
            val placeholderBackupFile = BackupFile(
                id = "json-restore",
                fileName = "json_restore.json",
                filePath = "",
                location = BackupLocation.LOCAL,
                timestamp = System.currentTimeMillis(),
                sizeBytes = 0,
                databaseVersion = currentDatabaseVersion,
                checksum = "",
                status = com.tiarkaerell.ibstracker.data.model.backup.BackupStatus.AVAILABLE,
                createdAt = System.currentTimeMillis()
            )

            RestoreResult.Success(
                itemsRestored = totalItems,
                backupFile = placeholderBackupFile,
                durationMs = durationMs,
                requiresRestart = false // JSON restore works without restart (hot-reload)
            )
        } catch (e: Exception) {
            RestoreResult.Failure(
                error = RestoreError.UNKNOWN,
                message = "JSON restore failed: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets a preview of JSON backup content.
     *
     * @param jsonContent Raw JSON backup content
     * @return Preview string with counts and first few items
     */
    fun getJsonBackupPreview(jsonContent: String): String {
        return JsonBackupParser.getBackupPreview(jsonContent)
    }
}
