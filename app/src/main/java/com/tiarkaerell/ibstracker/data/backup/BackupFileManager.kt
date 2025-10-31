package com.tiarkaerell.ibstracker.data.backup

import java.io.File
import java.security.MessageDigest

/**
 * Utility class for backup file operations including copy with checksum calculation.
 */
object BackupFileManager {

    /**
     * Copies a file to the target location while calculating SHA-256 checksum.
     *
     * This is a single-pass operation: the file is read once, written to the target,
     * and the checksum is calculated simultaneously for optimal performance.
     *
     * @param target The destination file
     * @return SHA-256 checksum as a 64-character hex string
     * @throws java.io.IOException if copy operation fails
     */
    fun File.copyToWithChecksum(target: File): String {
        val digest = MessageDigest.getInstance("SHA-256")

        this.inputStream().buffered(8192).use { input ->
            target.outputStream().buffered(8192).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead = input.read(buffer)

                while (bytesRead >= 0) {
                    output.write(buffer, 0, bytesRead)
                    digest.update(buffer, 0, bytesRead)
                    bytesRead = input.read(buffer)
                }
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies the checksum of a file against an expected value.
     *
     * @param expectedChecksum The expected SHA-256 checksum (64 hex characters)
     * @return true if checksum matches, false otherwise
     */
    fun File.verifyChecksum(expectedChecksum: String): Boolean {
        val digest = MessageDigest.getInstance("SHA-256")

        this.inputStream().buffered(8192).use { input ->
            val buffer = ByteArray(8192)
            var bytesRead = input.read(buffer)

            while (bytesRead >= 0) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
        }

        val actualChecksum = digest.digest().joinToString("") { "%02x".format(it) }
        return actualChecksum.equals(expectedChecksum, ignoreCase = true)
    }

    /**
     * Generates a backup filename with timestamp and database version.
     *
     * Format: ibstracker_v{version}_{yyyyMMdd}_{HHmmss}.db
     *
     * @param databaseVersion The Room database version
     * @param timestamp Unix epoch in milliseconds
     * @return Formatted filename
     */
    fun generateBackupFilename(databaseVersion: Int, timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
        val timestampStr = dateFormat.format(java.util.Date(timestamp))
        return "ibstracker_v${databaseVersion}_${timestampStr}.db"
    }

    /**
     * Parses a backup filename to extract database version and timestamp.
     *
     * @return Pair of (databaseVersion, timestamp) or null if parsing fails
     */
    fun parseBackupFilename(fileName: String): Pair<Int, Long>? {
        // Expected format: ibstracker_v10_20251027_140530.db
        val regex = Regex("""ibstracker_v(\d+)_(\d{8})_(\d{6})\.db""")
        val match = regex.matchEntire(fileName) ?: return null

        val (version, date, time) = match.destructured

        return try {
            val dateFormat = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
            val timestamp = dateFormat.parse("${date}_${time}")?.time ?: return null
            Pair(version.toInt(), timestamp)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculates SHA-256 checksum for a file.
     *
     * @param file The file to calculate checksum for
     * @return SHA-256 checksum as a 64-character hex string
     */
    fun calculateChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")

        file.inputStream().buffered(8192).use { input ->
            val buffer = ByteArray(8192)
            var bytesRead = input.read(buffer)

            while (bytesRead >= 0) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
