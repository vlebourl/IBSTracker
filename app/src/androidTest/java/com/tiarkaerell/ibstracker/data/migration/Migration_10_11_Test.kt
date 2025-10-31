package com.tiarkaerell.ibstracker.data.migration

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.database.MIGRATION_10_11
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.system.measureTimeMillis

/**
 * Migration_10_11_Test - Performance Optimization (v1.14.0)
 *
 * Tests for database migration from v10 to v11.
 *
 * Changes:
 * - Add index on symptoms.date column for improved query performance
 *
 * Success Criteria:
 * - Migration completes in <1s even with 1000+ symptoms
 * - All symptom data is preserved (0% data loss)
 * - Index is created successfully
 * - Queries with ORDER BY date DESC are faster
 */
@RunWith(AndroidJUnit4::class)
class Migration_10_11_Test {

    private val TEST_DB = "migration-test-10-11"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @After
    @Throws(IOException::class)
    fun closeDb() {
        // Clean up test database
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(TEST_DB)
    }

    /**
     * Test migration with empty database (baseline verification).
     */
    @Test
    fun migrate10To11_emptyDatabase_completesSuccessfully() {
        // Create v10 database (empty)
        helper.createDatabase(TEST_DB, 10).apply {
            close()
        }

        // Run migration
        val migrationTime = measureTimeMillis {
            helper.runMigrationsAndValidate(TEST_DB, 11, true, MIGRATION_10_11)
        }

        // Verify performance: Should be very fast (<100ms)
        assertTrue("Migration took ${migrationTime}ms (expected <100ms)", migrationTime < 100)

        // Verify database is accessible
        val db = getMigratedRoomDatabase()
        assertNotNull("Database should be accessible after migration", db)
        db.close()
    }

    /**
     * Test migration with 10 symptoms (typical user scenario).
     */
    @Test
    fun migrate10To11_with10Symptoms_preservesAllData() {
        // Create v10 database with 10 symptoms
        helper.createDatabase(TEST_DB, 10).apply {
            for (i in 1..10) {
                execSQL(
                    """
                    INSERT INTO symptoms (id, name, intensity, date)
                    VALUES ($i, 'Symptom $i', ${i % 10 + 1}, ${System.currentTimeMillis() + i * 60000})
                    """
                )
            }
            close()
        }

        // Run migration
        val migrationTime = measureTimeMillis {
            helper.runMigrationsAndValidate(TEST_DB, 11, true, MIGRATION_10_11)
        }

        // Verify performance: Should be fast (<500ms)
        assertTrue("Migration took ${migrationTime}ms (expected <500ms)", migrationTime < 500)

        // Verify data integrity
        val db = getMigratedRoomDatabase()
        val symptoms = db.symptomDao().getAllSymptoms().blockingFirst()

        // Verify 0% data loss
        assertEquals("Expected 10 symptoms after migration", 10, symptoms.size)

        // Verify data values are preserved
        assertTrue("Symptom names should be preserved", symptoms.all { it.name.startsWith("Symptom") })
        assertTrue("Symptom intensities should be in range 1-10", symptoms.all { it.intensity in 1..10 })
        assertTrue("Symptom dates should be preserved", symptoms.all { it.date != null })

        db.close()
    }

    /**
     * Test migration with 100 symptoms (power user scenario).
     */
    @Test
    fun migrate10To11_with100Symptoms_completesUnder1Second() {
        // Create v10 database with 100 symptoms
        helper.createDatabase(TEST_DB, 10).apply {
            for (i in 1..100) {
                execSQL(
                    """
                    INSERT INTO symptoms (id, name, intensity, date)
                    VALUES ($i, 'Symptom ${i % 20}', ${i % 10 + 1}, ${System.currentTimeMillis() + i * 60000})
                    """
                )
            }
            close()
        }

        // Run migration
        val migrationTime = measureTimeMillis {
            helper.runMigrationsAndValidate(TEST_DB, 11, true, MIGRATION_10_11)
        }

        // Verify performance: Should complete quickly (<1 second)
        assertTrue("Migration took ${migrationTime}ms (expected <1000ms)", migrationTime < 1000)

        // Verify data integrity
        val db = getMigratedRoomDatabase()
        val symptoms = db.symptomDao().getAllSymptoms().blockingFirst()

        // Verify 0% data loss
        assertEquals("Expected 100 symptoms after migration", 100, symptoms.size)

        db.close()
    }

    /**
     * Test migration with 1000 symptoms (stress test).
     */
    @Test
    fun migrate10To11_with1000Symptoms_completesUnder5Seconds() {
        // Create v10 database with 1000 symptoms
        helper.createDatabase(TEST_DB, 10).apply {
            for (i in 1..1000) {
                execSQL(
                    """
                    INSERT INTO symptoms (id, name, intensity, date)
                    VALUES ($i, 'Symptom ${i % 50}', ${i % 10 + 1}, ${System.currentTimeMillis() + i * 60000})
                    """
                )
            }
            close()
        }

        // Run migration
        val migrationTime = measureTimeMillis {
            helper.runMigrationsAndValidate(TEST_DB, 11, true, MIGRATION_10_11)
        }

        // Verify performance: Should complete reasonably fast (<5 seconds)
        assertTrue("Migration took ${migrationTime}ms (expected <5000ms)", migrationTime < 5000)

        // Verify data integrity
        val db = getMigratedRoomDatabase()
        val symptoms = db.symptomDao().getAllSymptoms().blockingFirst()

        // Verify 0% data loss
        assertEquals("Expected 1000 symptoms after migration", 1000, symptoms.size)

        db.close()
    }

    /**
     * Helper function to get the migrated database for validation.
     */
    private fun getMigratedRoomDatabase(): AppDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            TEST_DB
        )
        .addMigrations(MIGRATION_10_11)
        .build()
        .apply {
            openHelper.writableDatabase  // Force database open
        }
    }

    /**
     * Extension function to block and get first value from Flow (for testing).
     */
    private fun <T> kotlinx.coroutines.flow.Flow<T>.blockingFirst(): T {
        return kotlinx.coroutines.runBlocking {
            this@blockingFirst.kotlinx.coroutines.flow.first()
        }
    }
}
