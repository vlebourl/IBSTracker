/**
 * API Contract: RestoreManager
 *
 * Defines the interface for restoring database from local or cloud backups.
 * Implementations must handle database version compatibility and create safety backups.
 *
 * @see research.md Section 5 (Error Handling) for error scenarios
 * @see data-model.md RestoreResult for result types
 */
interface RestoreManager {

    /**
     * Restores database from a backup file (local or cloud).
     *
     * This operation:
     * 1. Downloads backup if location is CLOUD
     * 2. Verifies SHA-256 checksum integrity
     * 3. Checks database version compatibility
     * 4. Creates pre-restore safety backup of current database
     * 5. Closes Room database
     * 6. Replaces database file with backup
     * 7. Reopens Room database (triggers migrations if needed)
     * 8. Counts restored items (food + symptom entries)
     * 9. Emits refresh signal for UI to reload data
     *
     * Performance requirement: Must complete in <3 seconds for local restore
     *
     * IMPORTANT: App should be restarted after restore to reinitialize all ViewModels
     * with fresh data from the restored database.
     *
     * @param backupFile The backup to restore from (LOCAL or CLOUD)
     * @return RestoreResult.Success with count of restored items, or RestoreResult.Failure with error
     * @throws SecurityException if database file cannot be accessed
     */
    suspend fun restoreFromBackup(backupFile: BackupFile): RestoreResult

    /**
     * Checks if a backup file is compatible with the current app version.
     *
     * Compatibility rules:
     * - Same database version: Direct restore (no migration)
     * - Older database version: Restore + run migrations
     * - Newer database version: INCOMPATIBLE (cannot downgrade)
     *
     * @param backupFile The backup file to check
     * @return true if backup can be restored safely, false if version incompatible
     */
    suspend fun isBackupCompatible(backupFile: BackupFile): Boolean

    /**
     * Downloads a cloud backup file to local storage for restore.
     *
     * This is an internal step of restoreFromBackup() but exposed for progress tracking.
     *
     * @param backupFile Cloud backup file (must have location = CLOUD)
     * @param onProgress Callback for download progress (0-100)
     * @return Downloaded file path, or null if download failed
     * @throws IllegalArgumentException if backupFile.location is not CLOUD
     * @throws IOException if network error or download fails
     */
    suspend fun downloadCloudBackup(
        backupFile: BackupFile,
        onProgress: (Int) -> Unit = {}
    ): String?

    /**
     * Creates a safety backup of the current database before restore.
     *
     * Safety backup is stored as: ibstracker_pre_restore_{timestamp}.db
     * Allows rollback if restore fails or user wants to undo.
     *
     * @return BackupFile representing the safety backup, or null if creation failed
     */
    suspend fun createPreRestoreBackup(): BackupFile?

    /**
     * Rolls back to pre-restore backup if restore failed or was interrupted.
     *
     * Called automatically if:
     * - App crashes during restore
     * - Restore operation throws exception
     * - User force-closes app during restore
     *
     * @param preRestoreBackup The safety backup created before restore
     * @return true if rollback successful, false otherwise
     */
    suspend fun rollbackRestore(preRestoreBackup: BackupFile): Boolean

    /**
     * Counts total items in the current database (food + symptom entries).
     *
     * Used to display "Restored 150 items" success message.
     *
     * @return Total count of FoodItem + Symptom entities
     */
    suspend fun countDatabaseItems(): Int
}
