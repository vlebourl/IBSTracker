package com.tiarkaerell.ibstracker.data.model.backup

/**
 * Error types that can occur during restore operations.
 */
enum class RestoreError {
    /**
     * Backup file doesn't exist at specified path.
     */
    FILE_NOT_FOUND,

    /**
     * Checksum verification failed, file is corrupted.
     */
    FILE_CORRUPTED,

    /**
     * Database version in backup is incompatible with current app version.
     */
    VERSION_MISMATCH,

    /**
     * Cloud download operation failed.
     */
    DOWNLOAD_FAILED,

    /**
     * App crash or force close during restore operation.
     */
    RESTORE_INTERRUPTED,

    /**
     * No network connection for cloud restore.
     */
    NETWORK_UNAVAILABLE,

    /**
     * Unexpected error occurred.
     */
    UNKNOWN
}
