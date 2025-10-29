package com.tiarkaerell.ibstracker.data.model.backup

/**
 * Represents the current status of a backup file.
 */
enum class BackupStatus {
    /**
     * Backup is ready to be used for restore operations.
     */
    AVAILABLE,

    /**
     * Backup is currently being uploaded to cloud storage.
     */
    UPLOADING,

    /**
     * Backup is currently being downloaded from cloud storage.
     */
    DOWNLOADING,

    /**
     * Upload or download operation failed.
     */
    FAILED,

    /**
     * Backup file failed checksum verification and cannot be trusted.
     */
    CORRUPTED
}
