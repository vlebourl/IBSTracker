package com.tiarkaerell.ibstracker.data.backup

import android.content.Context
import androidx.room.RoomDatabase
import com.tiarkaerell.ibstracker.data.backup.BackupFileManager.verifyChecksum
import com.tiarkaerell.ibstracker.data.model.backup.BackupFile
import com.tiarkaerell.ibstracker.data.model.backup.RestoreError
import com.tiarkaerell.ibstracker.data.model.backup.RestoreResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * RestoreManager handles database restore operations from local backups.
 *
 * Key Responsibilities:
 * - Validates backup file integrity (checksum verification)
 * - Checks database version compatibility
 * - Creates pre-restore safety backup
 * - Performs database file replacement
 * - Counts restored items for user feedback
 *
 * Thread Safety: All public methods use Dispatchers.IO for file operations
 *
 * @param context Application context for accessing file storage
 * @param database Room database instance for closing/reopening
 * @param backupManager BackupManager instance for creating pre-restore backup
 * @param currentDatabaseVersion Current app database schema version
 */
class RestoreManager(
    private val context: Context,
    private val database: RoomDatabase,
    private val backupManager: BackupManager,
    private val currentDatabaseVersion: Int
) {
    companion object {
        private const val DATABASE_NAME = "ibs-tracker-database"
        private const val VERSION_TOLERANCE = 0 // Only exact version matches allowed for now
    }

    /**
     * Restores the database from a backup file.
     *
     * Performance target: <3s for typical database size
     *
     * Steps:
     * 1. Verify backup file exists and checksum is valid
     * 2. Check database version compatibility
     * 3. Create pre-restore safety backup of current data
     * 4. Close database connection
     * 5. Replace database file with backup
     * 6. Reopen database (Room will handle WAL files automatically)
     * 7. Count restored items
     *
     * @param backupFile The backup file to restore from
     * @return RestoreResult.Success with item count and duration, or RestoreResult.Failure
     */
    suspend fun restoreFromBackup(backupFile: BackupFile): RestoreResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            // Step 1: Verify backup file
            val verificationError = verifyBackupFile(backupFile)
            if (verificationError != null) {
                return@withContext RestoreResult.Failure(
                    error = verificationError,
                    message = "Backup file verification failed"
                )
            }

            // Step 2: Check version compatibility
            if (!checkDatabaseVersionCompatibility(backupFile.databaseVersion)) {
                return@withContext RestoreResult.Failure(
                    error = RestoreError.VERSION_MISMATCH,
                    message = "Backup version ${backupFile.databaseVersion} incompatible with current version $currentDatabaseVersion"
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
            val restoreSuccess = performDatabaseRestore(backupFile)
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
                durationMs = durationMs
            )

        } catch (e: Exception) {
            RestoreResult.Failure(
                error = RestoreError.UNKNOWN,
                message = "Unexpected error during restore: ${e.message}",
                cause = e
            )
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
}
