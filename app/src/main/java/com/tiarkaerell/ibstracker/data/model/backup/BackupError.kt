package com.tiarkaerell.ibstracker.data.model.backup

/**
 * Error types that can occur during backup operations.
 */
enum class BackupError {
    /**
     * Device storage is full, cannot create backup.
     */
    STORAGE_FULL,

    /**
     * Database is locked by another process.
     */
    DATABASE_LOCKED,

    /**
     * WAL checkpoint operation failed.
     */
    CHECKPOINT_FAILED,

    /**
     * File copy operation failed.
     */
    COPY_FAILED,

    /**
     * Checksum verification failed.
     */
    CHECKSUM_MISMATCH,

    /**
     * Google Drive upload failed.
     */
    UPLOAD_FAILED,

    /**
     * Google Sign-In failed or expired.
     */
    AUTHENTICATION_FAILED,

    /**
     * No network connection available.
     */
    NETWORK_UNAVAILABLE,

    /**
     * Unexpected error occurred.
     */
    UNKNOWN
}
