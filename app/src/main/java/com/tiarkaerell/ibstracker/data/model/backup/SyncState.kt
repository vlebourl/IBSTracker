package com.tiarkaerell.ibstracker.data.model.backup

/**
 * Represents the current state of cloud synchronization.
 */
enum class SyncState {
    /**
     * Last sync completed successfully.
     */
    SYNCED,

    /**
     * Currently syncing (uploading or downloading).
     */
    SYNCING,

    /**
     * Last sync attempt failed.
     */
    FAILED,

    /**
     * No sync has ever been performed.
     */
    NEVER
}
