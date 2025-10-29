/**
 * API Contract: BackupManager
 *
 * Defines the interface for local backup creation and management operations.
 * Implementations must ensure data integrity through WAL checkpoint and checksum verification.
 *
 * @see research.md Section 1 (Room Database Backup) for implementation patterns
 * @see data-model.md BackupFile entity for data structure
 */
interface BackupManager {

    /**
     * Creates a local backup of the current database.
     *
     * This operation:
     * 1. Executes WAL checkpoint to merge all data into main database file
     * 2. Generates timestamped filename (ibstracker_v{version}_{timestamp}.db)
     * 3. Copies database file with SHA-256 checksum calculation
     * 4. Stores checksum in companion .sha256 file
     * 5. Cleans up old backups (keeps 7 most recent)
     *
     * Performance requirement: Must complete in <200ms
     *
     * @return BackupResult.Success with created BackupFile, or BackupResult.Failure with error
     * @throws SecurityException if app-specific storage is not accessible
     */
    suspend fun createLocalBackup(): BackupResult

    /**
     * Lists all available local backups sorted by timestamp (newest first).
     *
     * Scans app-specific filesDir for backup files matching pattern:
     * ibstracker_v{version}_{yyyyMMdd}_{HHmmss}.db
     *
     * Verifies checksum for each backup file and marks corrupted files with CORRUPTED status.
     *
     * @param includeCorrupted If true, includes backups with CORRUPTED status in the list
     * @return Flow of BackupFile list, sorted by timestamp descending
     */
    fun listLocalBackups(includeCorrupted: Boolean = false): Flow<List<BackupFile>>

    /**
     * Deletes a specific local backup file and its checksum companion.
     *
     * @param backupFile The backup to delete (must have location = LOCAL)
     * @return true if deleted successfully, false if file not found or deletion failed
     * @throws IllegalArgumentException if backupFile.location is not LOCAL
     */
    suspend fun deleteLocalBackup(backupFile: BackupFile): Boolean

    /**
     * Deletes all local backup files.
     *
     * WARNING: This operation is irreversible. Should only be called from
     * user-confirmed action (Settings > Clear all local backups).
     *
     * @return Number of backup files deleted
     */
    suspend fun deleteAllLocalBackups(): Int

    /**
     * Calculates total storage usage of local backups.
     *
     * @return Total size in bytes of all backup files in app-specific filesDir
     */
    suspend fun calculateLocalStorageUsage(): Long

    /**
     * Verifies integrity of a backup file by checking SHA-256 checksum.
     *
     * @param backupFile The backup file to verify
     * @return true if checksum matches, false if mismatch or checksum file missing
     */
    suspend fun verifyBackupIntegrity(backupFile: BackupFile): Boolean

    /**
     * Checks if device has sufficient storage for a new backup.
     *
     * Requires 2x the database size as buffer for safety.
     *
     * @param requiredBytes Estimated backup size (typically current database size)
     * @return true if sufficient storage available, false otherwise
     */
    suspend fun hasEnoughStorageSpace(requiredBytes: Long): Boolean
}
