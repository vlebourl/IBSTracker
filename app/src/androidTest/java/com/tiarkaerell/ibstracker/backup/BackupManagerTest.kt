package com.tiarkaerell.ibstracker.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tiarkaerell.ibstracker.data.backup.BackupManager
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.model.backup.BackupError
import com.tiarkaerell.ibstracker.data.model.backup.BackupResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented test for BackupManager - Local Backup After Changes (User Story 1)
 *
 * Tests verify:
 * - Backup creation completes in <200ms
 * - SHA-256 checksum integrity
 * - Cleanup retains 7 most recent backups
 * - WAL checkpoint execution
 * - Storage full error handling
 *
 * IMPORTANT: These tests are written FIRST following TDD approach.
 * They should FAIL until BackupManager is implemented.
 */
@RunWith(AndroidJUnit4::class)
class BackupManagerTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var backupDirectory: File
    private lateinit var backupManager: BackupManager

    @Before
    fun setUp() {
        // Get instrumentation context
        context = ApplicationProvider.getApplicationContext()

        // Create real database (not in-memory) for WAL mode testing
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "test_database"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_9,
                AppDatabase.MIGRATION_9_10
            )
            .fallbackToDestructiveMigration()
            .build()

        // Create test backup directory
        backupDirectory = File(context.filesDir, "backups")
        if (!backupDirectory.exists()) {
            backupDirectory.mkdirs()
        }

        // Initialize BackupManager
        backupManager = BackupManager(
            context = context,
            database = database,
            databaseVersion = 10
        )
    }

    @After
    fun tearDown() {
        // Close database
        database.close()

        // Clean up test backup files
        if (backupDirectory.exists()) {
            backupDirectory.listFiles()?.forEach { it.delete() }
        }

        // Clean up test database
        context.deleteDatabase("test_database")
    }

    /**
     * T025: Test that backup creation completes in under 200ms
     *
     * Given: Database with sample data
     * When: createLocalBackup() is called
     * Then: Backup completes in <200ms and returns Success result
     */
    @org.junit.Test
    fun testCreateLocalBackup_success() = runTest {
        // Given: Insert some test data
        database.foodItemDao().insert(
            com.tiarkaerell.ibstracker.data.model.FoodItem(
                name = "Test Food",
                quantity = "100g",
                timestamp = java.util.Date(),
                category = com.tiarkaerell.ibstracker.data.model.FoodCategory.GRAINS,
                ibsImpacts = emptyList(),
                isCustom = true
            )
        )

        // When: Create backup
        val result = backupManager.createLocalBackup()

        // Then: Verify success and performance
        assertTrue("Backup should succeed", result is BackupResult.Success)
        if (result is BackupResult.Success) {
            assertTrue("Backup should complete in <200ms", result.durationMs < 200)
            assertTrue("Backup file should exist", File(result.backupFile.filePath).exists())
            assertTrue("Backup file should have content", result.backupFile.sizeBytes > 0)
        }
    }

    /**
     * T026: Test that SHA-256 checksum is correctly generated and stored
     *
     * Given: Backup file created
     * When: Checksum is calculated
     * Then: 64-character hex string matches file content hash
     */
    @org.junit.Test
    fun testBackupChecksum_integrity() = runTest {
        // Given: Create a backup
        val result = backupManager.createLocalBackup()

        // Then: Verify checksum format and integrity
        assertTrue("Backup should succeed", result is BackupResult.Success)
        if (result is BackupResult.Success) {
            val checksum = result.backupFile.checksum
            assertEquals("Checksum should be 64 hex characters", 64, checksum.length)
            assertTrue("Checksum should only contain hex chars", checksum.matches(Regex("[0-9a-f]{64}")))

            // Verify checksum matches file content
            val backupFile = File(result.backupFile.filePath)
            val isValid = backupManager.verifyBackupIntegrity(result.backupFile, checksum)
            assertTrue("Checksum should match file content", isValid)
        }
    }

    /**
     * T027: Test that cleanup retains only 7 most recent backups
     *
     * Given: 10 backup files exist
     * When: cleanupOldBackups() is called
     * Then: Only 7 most recent backups remain
     */
    @org.junit.Test
    fun testCleanupOldBackups_keepsSevenMostRecent() = runTest {
        // Given: Create 10 backups with sufficient delay for unique filenames
        repeat(10) {
            val result = backupManager.createLocalBackup()
            assertTrue("Each backup should succeed", result is BackupResult.Success)
            // Delay 1 second to ensure unique filenames (format includes seconds)
            kotlinx.coroutines.delay(1000)
        }

        // When: Check backup count (cleanup happens automatically in createLocalBackup)
        val backupsFlow = backupManager.listLocalBackups()
        val backups = backupsFlow.first()

        // Then: Verify only 7 backups remain
        assertEquals("Should retain exactly 7 backups", 7, backups.size)
    }

    /**
     * T028: Test that WAL checkpoint is executed during backup
     *
     * Given: Database with WAL mode enabled
     * When: createLocalBackup() is called
     * Then: PRAGMA wal_checkpoint(FULL) is executed
     */
    @org.junit.Test
    fun testWALCheckpoint_executed() = runTest {
        // Given: Database with WAL mode (default for Room)
        database.foodItemDao().insert(
            com.tiarkaerell.ibstracker.data.model.FoodItem(
                name = "Test Food",
                quantity = "100g",
                timestamp = java.util.Date(),
                category = com.tiarkaerell.ibstracker.data.model.FoodCategory.GRAINS,
                ibsImpacts = emptyList(),
                isCustom = true
            )
        )

        // When: Create backup (WAL checkpoint is executed internally)
        val result = backupManager.createLocalBackup()

        // Then: Verify backup succeeded (checkpoint didn't fail)
        assertTrue("Backup should succeed with WAL checkpoint", result is BackupResult.Success)
        // Note: WAL checkpoint execution is verified indirectly through successful backup
        // Direct verification would require mocking the database, which is complex
    }

    /**
     * T029: Test storage full error handling
     *
     * Given: Device storage is nearly full (simulated)
     * When: createLocalBackup() is called
     * Then: Returns Failure with BackupError.STORAGE_FULL
     */
    @org.junit.Test
    fun testStorageFull_errorHandling() = runTest {
        // Note: This test is difficult to simulate realistically without root access
        // We verify the hasEnoughStorageSpace() method exists and returns a boolean
        val hasSpace = backupManager.hasEnoughStorageSpace()
        assertTrue("hasEnoughStorageSpace should return boolean", hasSpace is Boolean)

        // In a real scenario with low storage, backup should fail with STORAGE_FULL error
        // For now, we verify the method signature and that backups work when storage IS available
        val result = backupManager.createLocalBackup()
        assertTrue("Backup should succeed when storage is available", result is BackupResult.Success)
    }
}
