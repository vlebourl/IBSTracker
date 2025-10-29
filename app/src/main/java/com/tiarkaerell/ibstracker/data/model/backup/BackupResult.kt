package com.tiarkaerell.ibstracker.data.model.backup

/**
 * Result of a backup operation (local or cloud).
 */
sealed class BackupResult {
    /**
     * Backup operation completed successfully.
     *
     * @property backupFile The created backup file
     * @property durationMs Time taken to create backup in milliseconds
     */
    data class Success(
        val backupFile: BackupFile,
        val durationMs: Long
    ) : BackupResult()

    /**
     * Backup operation failed.
     *
     * @property error The type of error that occurred
     * @property message Human-readable error description
     * @property cause Optional exception that caused the failure
     */
    data class Failure(
        val error: BackupError,
        val message: String,
        val cause: Throwable? = null
    ) : BackupResult()
}
