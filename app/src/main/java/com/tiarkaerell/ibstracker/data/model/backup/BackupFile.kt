package com.tiarkaerell.ibstracker.data.model.backup

import java.util.UUID

/**
 * Represents a physical backup file on disk or in cloud storage.
 *
 * @property id Unique identifier for this backup
 * @property fileName Backup filename (e.g., "ibstracker_v10_20251027_140530.db")
 * @property filePath Absolute path (local) or Drive file ID (cloud)
 * @property location Storage location (LOCAL or CLOUD)
 * @property timestamp Unix epoch in milliseconds when backup was created
 * @property sizeBytes File size in bytes
 * @property databaseVersion Room database version at backup time
 * @property checksum SHA-256 checksum (64 hex characters)
 * @property status Current status of this backup
 * @property createdAt Unix epoch when BackupFile object was created
 */
data class BackupFile(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String,
    val location: BackupLocation,
    val timestamp: Long,
    val sizeBytes: Long,
    val databaseVersion: Int,
    val checksum: String,
    val status: BackupStatus = BackupStatus.AVAILABLE,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Validates that the backup file has required properties.
     */
    fun isValid(): Boolean {
        return fileName.isNotBlank()
                && filePath.isNotBlank()
                && checksum.length == 64 // SHA-256 = 64 hex chars
                && sizeBytes > 0
                && timestamp > 0
    }
}
