package com.tiarkaerell.ibstracker.data.model.backup

/**
 * Result of a restore operation.
 */
sealed class RestoreResult {
    /**
     * Restore operation completed successfully.
     *
     * @property itemsRestored Number of food/symptom entries restored
     * @property backupFile Source backup file that was restored from
     * @property durationMs Time taken to restore in milliseconds
     */
    data class Success(
        val itemsRestored: Int,
        val backupFile: BackupFile,
        val durationMs: Long
    ) : RestoreResult()

    /**
     * Restore operation failed.
     *
     * @property error The type of error that occurred
     * @property message Human-readable error description
     * @property cause Optional exception that caused the failure
     */
    data class Failure(
        val error: RestoreError,
        val message: String,
        val cause: Throwable? = null
    ) : RestoreResult()
}
