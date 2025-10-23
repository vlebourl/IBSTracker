package com.tiarkaerell.ibstracker.data.migration

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.system.measureTimeMillis

/**
 * Migration_8_9_Test - Smart Food Categorization System (v1.9.0)
 *
 * Tests for database migration from v2 to v9.
 *
 * Success Criteria (SC-003):
 * - 10 entries: Migration completes in <5s with 0% data loss
 * - 100 entries: Migration completes in <10s with 0% data loss
 * - 1000 entries: Migration completes in <20s with 0% data loss
 * - 5000 entries: Migration completes in <30s with 0% data loss
 *
 * Verification:
 * - All old food_items migrated correctly
 * - New tables created (common_foods, food_usage_stats)
 * - Pre-populated ~100 common foods
 * - Usage stats initialized from existing data
 * - No data loss (all original rows preserved)
 */
@RunWith(AndroidJUnit4::class)
class Migration_8_9_Test {

    private val TEST_DB = "migration-test"

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
     * T018: Test migration with 10 entries (<5s, 0% data loss)
     */
    @Test
    fun migrate8To9_with10Entries_completesUnder5Seconds_withZeroDataLoss() {
        // Create v2 database with 10 food items
        helper.createDatabase(TEST_DB, 2).apply {
            // Insert 10 test food items in old schema
            for (i in 1..10) {
                execSQL(
                    """
                    INSERT INTO food_items (id, name, quantity, date, category)
                    VALUES ($i, 'Food $i', '100g', ${System.currentTimeMillis()}, 'OTHER')
                    """
                )
            }
            close()
        }

        // Measure migration time
        val migrationTime = measureTimeMillis {
            // Run migration
            helper.runMigrationsAndValidate(TEST_DB, 9, true, AppDatabase.MIGRATION_2_9)
        }

        // Verify performance: <5 seconds
        assertTrue("Migration took ${migrationTime}ms (expected <5000ms)", migrationTime < 5000)

        // Verify data integrity
        val db = getMigratedRoomDatabase()
        val foodItems = db.foodItemDao().getAllFoodItems().blockingFirst()

        // Verify 0% data loss: All 10 items should be present
        assertEquals("Expected 10 food items after migration", 10, foodItems.size)

        // Verify migration preserved data correctly
        assertTrue("Food items should have timestamps", foodItems.all { it.timestamp != null })
        assertTrue("Food items should have categories", foodItems.all { it.category != null })

        // Verify new tables were created
        val commonFoods = db.commonFoodDao().getAllCommonFoods().blockingFirst()
        assertTrue("Common foods should be pre-populated (expected ~100)", commonFoods.size >= 90)

        // Verify usage stats were initialized
        val usageStats = db.foodUsageStatsDao().getAllStats().blockingFirst()
        assertEquals("Usage stats should be initialized for 10 unique foods", 10, usageStats.size)

        db.close()
    }

    /**
     * T019: Test migration with 100 entries (<10s, 0% data loss)
     */
    @Test
    fun migrate8To9_with100Entries_completesUnder10Seconds_withZeroDataLoss() {
        // Create v2 database with 100 food items
        helper.createDatabase(TEST_DB, 2).apply {
            // Insert 100 test food items
            for (i in 1..100) {
                execSQL(
                    """
                    INSERT INTO food_items (id, name, quantity, date, category)
                    VALUES ($i, 'Food ${i % 20}', '${i * 10}g', ${System.currentTimeMillis() + i * 1000}, 'OTHER')
                    """
                )
            }
            close()
        }

        // Measure migration time
        val migrationTime = measureTimeMillis {
            helper.runMigrationsAndValidate(TEST_DB, 9, true, AppDatabase.MIGRATION_2_9)
        }

        // Verify performance: <10 seconds
        assertTrue("Migration took ${migrationTime}ms (expected <10000ms)", migrationTime < 10000)

        // Verify data integrity
        val db = getMigratedRoomDatabase()
        val foodItems = db.foodItemDao().getAllFoodItems().blockingFirst()

        // Verify 0% data loss
        assertEquals("Expected 100 food items after migration", 100, foodItems.size)

        // Verify usage stats aggregation
        val usageStats = db.foodUsageStatsDao().getAllStats().blockingFirst()
        assertTrue("Usage stats should aggregate duplicate food names", usageStats.size <= 20)

        // Verify highest usage count is correct (Food 0-19 should each appear 5 times)
        val topUsed = usageStats.sortedByDescending { it.usageCount }.first()
        assertEquals("Top used food should have count of 5", 5, topUsed.usageCount)

        db.close()
    }

    /**
     * T020: Test migration with 1000 entries (<20s, 0% data loss)
     */
    @Test
    fun migrate8To9_with1000Entries_completesUnder20Seconds_withZeroDataLoss() {
        // Create v2 database with 1000 food items
        helper.createDatabase(TEST_DB, 2).apply {
            // Use batch insert for performance
            beginTransaction()
            try {
                for (i in 1..1000) {
                    execSQL(
                        """
                        INSERT INTO food_items (id, name, quantity, date, category)
                        VALUES ($i, 'Food ${i % 50}', '${i % 500}g', ${System.currentTimeMillis() + i * 100}, 'OTHER')
                        """
                    )
                }
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
            close()
        }

        // Measure migration time
        val migrationTime = measureTimeMillis {
            helper.runMigrationsAndValidate(TEST_DB, 9, true, AppDatabase.MIGRATION_2_9)
        }

        // Verify performance: <20 seconds
        assertTrue("Migration took ${migrationTime}ms (expected <20000ms)", migrationTime < 20000)

        // Verify data integrity
        val db = getMigratedRoomDatabase()
        val foodItems = db.foodItemDao().getAllFoodItems().blockingFirst()

        // Verify 0% data loss
        assertEquals("Expected 1000 food items after migration", 1000, foodItems.size)

        // Verify usage stats aggregation (50 unique food names)
        val usageStats = db.foodUsageStatsDao().getAllStats().blockingFirst()
        assertEquals("Usage stats should have 50 unique foods", 50, usageStats.size)

        // Verify highest usage count (each food appears 20 times)
        val topUsed = usageStats.sortedByDescending { it.usageCount }.first()
        assertEquals("Top used food should have count of 20", 20, topUsed.usageCount)

        // Verify sorting works (usage_count DESC, name ASC)
        val sortedStats = usageStats.sortedWith(
            compareByDescending<com.tiarkaerell.ibstracker.data.model.FoodUsageStats> { it.usageCount }
                .thenBy { it.foodName }
        )
        assertEquals("Stats should be sorted correctly", sortedStats, usageStats)

        db.close()
    }

    /**
     * T021: Test migration with 5000 entries (<30s, 0% data loss)
     */
    @Test
    fun migrate8To9_with5000Entries_completesUnder30Seconds_withZeroDataLoss() {
        // Create v2 database with 5000 food items
        helper.createDatabase(TEST_DB, 2).apply {
            // Use batch insert for performance
            beginTransaction()
            try {
                for (i in 1..5000) {
                    execSQL(
                        """
                        INSERT INTO food_items (id, name, quantity, date, category)
                        VALUES ($i, 'Food ${i % 100}', '${i % 1000}g', ${System.currentTimeMillis() + i * 50}, 'OTHER')
                        """
                    )
                }
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
            close()
        }

        // Measure migration time
        val migrationTime = measureTimeMillis {
            helper.runMigrationsAndValidate(TEST_DB, 9, true, AppDatabase.MIGRATION_2_9)
        }

        // Verify performance: <30 seconds
        assertTrue("Migration took ${migrationTime}ms (expected <30000ms)", migrationTime < 30000)

        // Verify data integrity
        val db = getMigratedRoomDatabase()
        val foodItems = db.foodItemDao().getAllFoodItems().blockingFirst()

        // Verify 0% data loss
        assertEquals("Expected 5000 food items after migration", 5000, foodItems.size)

        // Verify usage stats aggregation (100 unique food names)
        val usageStats = db.foodUsageStatsDao().getAllStats().blockingFirst()
        assertEquals("Usage stats should have 100 unique foods", 100, usageStats.size)

        // Verify highest usage count (each food appears 50 times)
        val topUsed = usageStats.sortedByDescending { it.usageCount }.first()
        assertEquals("Top used food should have count of 50", 50, topUsed.usageCount)

        // Verify common foods table is intact
        val commonFoods = db.commonFoodDao().getAllCommonFoods().blockingFirst()
        assertTrue("Common foods should still be pre-populated", commonFoods.size >= 90)

        // Verify database is responsive after large migration
        val recentFoods = db.foodItemDao().getAllFoodItems().blockingFirst().take(10)
        assertEquals("Should retrieve 10 recent items", 10, recentFoods.size)

        db.close()
    }

    /**
     * Helper: Get migrated Room database
     */
    private fun getMigratedRoomDatabase(): AppDatabase {
        val database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
            TEST_DB
        )
            .addMigrations(AppDatabase.MIGRATION_2_9)
            .build()

        // Trigger Room to validate the schema
        helper.closeWhenFinished(database)
        return database
    }

    /**
     * Helper extension: Convert Flow to blocking first value
     * (For test convenience - NOT for production code)
     */
    private fun <T> kotlinx.coroutines.flow.Flow<T>.blockingFirst(): T {
        return kotlinx.coroutines.runBlocking {
            this@blockingFirst.first()
        }
    }
}
