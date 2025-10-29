package com.tiarkaerell.ibstracker.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tiarkaerell.ibstracker.data.backup.BackupManager
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.Symptom
import com.tiarkaerell.ibstracker.data.model.backup.BackupResult
import com.tiarkaerell.ibstracker.data.model.backup.RestoreResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import java.io.File
import java.util.Date

/**
 * Integration test for full Backup → Restore flow (User Story 2)
 *
 * Tests verify:
 * - Complete end-to-end backup and restore cycle
 * - Data integrity after restore
 * - Counts match original data
 *
 * IMPORTANT: This test verifies the complete user journey.
 * It should FAIL until both BackupManager and RestoreManager are implemented.
 */
@RunWith(AndroidJUnit4::class)
class BackupIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var backupManager: BackupManager
    private lateinit var backupDirectory: File

    @Before
    fun setUp() {
        // Get instrumentation context
        context = ApplicationProvider.getApplicationContext()

        // Create real database for testing
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "test_integration_database"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_9,
                AppDatabase.MIGRATION_9_10
            )
            .fallbackToDestructiveMigration()
            .build()

        // Create backup directory
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
        context.deleteDatabase("test_integration_database")
    }

    /**
     * T056: Test full backup → restore flow
     *
     * Given: Database with sample food items and symptoms
     * When: Create backup, delete all data, restore from backup
     * Then: All original data is recovered correctly
     */
    @org.junit.Test
    fun testFullBackupRestoreFlow() = runTest {
        // This test will FAIL until RestoreManager is implemented
        TODO("T056: Implement test after RestoreManager is created")
    }
}
