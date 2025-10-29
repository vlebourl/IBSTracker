package com.tiarkaerell.ibstracker.data.backup

import android.content.Context
import android.os.StatFs
import androidx.room.RoomDatabase
import com.tiarkaerell.ibstracker.data.backup.BackupFileManager.copyToWithChecksum
import com.tiarkaerell.ibstracker.data.backup.BackupFileManager.generateBackupFilename
import com.tiarkaerell.ibstracker.data.backup.BackupFileManager.parseBackupFilename
import com.tiarkaerell.ibstracker.data.backup.BackupFileManager.verifyChecksum
import com.tiarkaerell.ibstracker.data.model.backup.BackupError
import com.tiarkaerell.ibstracker.data.model.backup.BackupFile
import com.tiarkaerell.ibstracker.data.model.backup.BackupLocation
import com.tiarkaerell.ibstracker.data.model.backup.BackupResult
import com.tiarkaerell.ibstracker.data.model.backup.BackupStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * BackupManager handles all local backup operations for the IBS Tracker database.
 *
 * Key Responsibilities:
 * - Creates local backups after every data change (<200ms target)
 * - Maintains 7 most recent local backups
 * - Calculates SHA-256 checksums for integrity verification
 * - Executes WAL checkpoint before backup copy
 * - Manages local storage usage
 *
 * Thread Safety: All public methods use Dispatchers.IO for file operations
 *
 * @param context Application context for accessing file storage
 * @param database Room database instance for WAL checkpoint and path access
 * @param databaseVersion Current database schema version (for backup filename)
 */
class BackupManager(
    private val context: Context,
    private val database: RoomDatabase,
    private val databaseVersion: Int
) {
    private val backupDirectory: File = File(context.filesDir, "backups")

    companion object {
        private const val MAX_LOCAL_BACKUPS = 7
        private const val MIN_FREE_STORAGE_MB = 50L // Minimum 50MB required
        private const val DATABASE_NAME = "ibs-tracker-database"
    }

    init {
        // Ensure backup directory exists
        if (!backupDirectory.exists()) {
            backupDirectory.mkdirs()
        }
    }

    /**
     * Creates a local backup of the database.
     *
     * Performance target: <200ms
     *
     * Steps:
     * 1. Check storage space
     * 2. Execute WAL checkpoint (PRAGMA wal_checkpoint(FULL))
     * 3. Copy database file with checksum calculation (single pass)
     * 4. Cleanup old backups (keep 7 most recent)
     *
     * @return BackupResult.Success with BackupFile and duration, or BackupResult.Failure
     */
    suspend fun createLocalBackup(): BackupResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            // Step 1: Check storage space
            if (!hasEnoughStorageSpace()) {
                return@withContext BackupResult.Failure(
                    error = BackupError.STORAGE_FULL,
                    message = "Insufficient storage space. At least ${MIN_FREE_STORAGE_MB}MB required."
                )
            }

            // Step 2: Execute WAL checkpoint to ensure all data is written to main database file
            val checkpointResult = database.openHelper.writableDatabase.query(
                "PRAGMA wal_checkpoint(FULL)"
            )
            checkpointResult.use {
                if (!it.moveToFirst()) {
                    return@withContext BackupResult.Failure(
                        error = BackupError.CHECKPOINT_FAILED,
                        message = "WAL checkpoint failed to execute"
                    )
                }
            }

            // Step 3: Copy database file with checksum calculation
            val timestamp = System.currentTimeMillis()
            val backupFileName = generateBackupFilename(databaseVersion, timestamp)
            val backupFile = File(backupDirectory, backupFileName)

            // Get source database file
            val sourceDbFile = context.getDatabasePath(DATABASE_NAME)

            if (!sourceDbFile.exists()) {
                return@withContext BackupResult.Failure(
                    error = BackupError.COPY_FAILED,
                    message = "Source database file not found: ${sourceDbFile.absolutePath}"
                )
            }

            // Copy file and calculate checksum in single pass
            val checksum = try {
                sourceDbFile.copyToWithChecksum(backupFile)
            } catch (e: Exception) {
                return@withContext BackupResult.Failure(
                    error = BackupError.COPY_FAILED,
                    message = "Failed to copy database file: ${e.message}",
                    cause = e
                )
            }

            // Create BackupFile metadata
            val backup = BackupFile(
                id = UUID.randomUUID().toString(),
                fileName = backupFileName,
                filePath = backupFile.absolutePath,
                location = BackupLocation.LOCAL,
                timestamp = timestamp,
                sizeBytes = backupFile.length(),
                databaseVersion = databaseVersion,
                checksum = checksum,
                status = BackupStatus.AVAILABLE,
                createdAt = timestamp
            )

            // Step 4: Cleanup old backups
            cleanupOldBackups()

            val durationMs = System.currentTimeMillis() - startTime
            BackupResult.Success(backupFile = backup, durationMs = durationMs)

        } catch (e: Exception) {
            BackupResult.Failure(
                error = BackupError.UNKNOWN,
                message = "Unexpected error during backup: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Checks if there is enough storage space for a backup.
     *
     * @return true if at least MIN_FREE_STORAGE_MB is available
     */
    fun hasEnoughStorageSpace(): Boolean {
        val stat = StatFs(context.filesDir.absolutePath)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        val availableMB = availableBytes / (1024 * 1024)
        return availableMB >= MIN_FREE_STORAGE_MB
    }

    /**
     * Lists all local backup files as a Flow.
     *
     * Backups are sorted by timestamp descending (most recent first).
     *
     * @return Flow of list of BackupFile objects
     */
    fun listLocalBackups(): Flow<List<BackupFile>> = flow {
        val backupFiles = backupDirectory.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".db") }
            ?.mapNotNull { file ->
                val parsed = parseBackupFilename(file.name) ?: return@mapNotNull null
                val (version, timestamp) = parsed

                BackupFile(
                    id = UUID.randomUUID().toString(),
                    fileName = file.name,
                    filePath = file.absolutePath,
                    location = BackupLocation.LOCAL,
                    timestamp = timestamp,
                    sizeBytes = file.length(),
                    databaseVersion = version,
                    checksum = "", // Checksum not stored separately, would need recalculation
                    status = BackupStatus.AVAILABLE,
                    createdAt = timestamp
                )
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()

        emit(backupFiles)
    }.flowOn(Dispatchers.IO)

    /**
     * Deletes a specific local backup file.
     *
     * @param backupFile The backup file to delete
     * @return true if deletion successful, false otherwise
     */
    suspend fun deleteLocalBackup(backupFile: BackupFile): Boolean = withContext(Dispatchers.IO) {
        val file = File(backupFile.filePath)
        file.exists() && file.delete()
    }

    /**
     * Deletes all local backup files.
     *
     * @return Number of files deleted
     */
    suspend fun deleteAllLocalBackups(): Int = withContext(Dispatchers.IO) {
        val files = backupDirectory.listFiles()?.filter { it.isFile && it.name.endsWith(".db") } ?: emptyList()
        var deletedCount = 0
        files.forEach { file ->
            if (file.delete()) {
                deletedCount++
            }
        }
        deletedCount
    }

    /**
     * Calculates total storage used by local backups.
     *
     * @return Total size in bytes
     */
    suspend fun calculateLocalStorageUsage(): Long = withContext(Dispatchers.IO) {
        backupDirectory.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".db") }
            ?.sumOf { it.length() }
            ?: 0L
    }

    /**
     * Verifies the integrity of a backup file using SHA-256 checksum.
     *
     * @param backupFile The backup file to verify
     * @param expectedChecksum The expected SHA-256 checksum (64 hex chars)
     * @return true if checksum matches, false otherwise
     */
    suspend fun verifyBackupIntegrity(backupFile: BackupFile, expectedChecksum: String): Boolean =
        withContext(Dispatchers.IO) {
            val file = File(backupFile.filePath)
            if (!file.exists()) return@withContext false
            file.verifyChecksum(expectedChecksum)
        }

    /**
     * Cleans up old backups, keeping only the MAX_LOCAL_BACKUPS most recent files.
     *
     * Private method called automatically after each backup creation.
     */
    private fun cleanupOldBackups() {
        val backupFiles = backupDirectory.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".db") }
            ?.sortedByDescending { it.lastModified() }
            ?: return

        // Delete all backups beyond MAX_LOCAL_BACKUPS
        backupFiles.drop(MAX_LOCAL_BACKUPS).forEach { file ->
            file.delete()
        }
    }
}
