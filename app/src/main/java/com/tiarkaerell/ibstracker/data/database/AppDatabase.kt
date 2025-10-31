package com.tiarkaerell.ibstracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tiarkaerell.ibstracker.data.database.dao.CommonFoodDao
import com.tiarkaerell.ibstracker.data.database.dao.FoodItemDao
import com.tiarkaerell.ibstracker.data.database.dao.FoodUsageStatsDao
import com.tiarkaerell.ibstracker.data.database.dao.SymptomDao
import com.tiarkaerell.ibstracker.data.model.CommonFood
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.FoodUsageStats
import com.tiarkaerell.ibstracker.data.model.Symptom

/**
 * IBS Tracker Room Database.
 *
 * Version 11: Performance Optimization
 * - Added index on symptoms.date for improved query performance
 *
 * Version 10: Backfill FoodUsageStats for Quick-Add Feature
 * - Migrates existing food_items data into food_usage_stats
 * - Ensures quick-add section displays historical food usage
 *
 * Version 9: Smart Food Categorization System
 * - Added CommonFood entity (~100 pre-populated foods)
 * - Added FoodUsageStats entity (usage tracking for quick-add)
 * - Updated FoodItem with new fields (category, ibsImpacts, isCustom, commonFoodId)
 *
 * Migration v2→v9: Includes intermediate versions and MIGRATION_8_9 for major refactor
 */
@Database(
    entities = [
        FoodItem::class,
        Symptom::class,
        CommonFood::class,
        FoodUsageStats::class
    ],
    version = 11,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun symptomDao(): SymptomDao
    abstract fun commonFoodDao(): CommonFoodDao
    abstract fun foodUsageStatsDao(): FoodUsageStatsDao

    companion object {
        /**
         * Legacy migration from v1 to v2.
         * Added basic category column to food_items.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add category column to food_items table with default value 'OTHER'
                database.execSQL("ALTER TABLE food_items ADD COLUMN category TEXT NOT NULL DEFAULT 'OTHER'")
            }
        }

        /**
         * Migration from v2 to v9.
         * Major refactor for Smart Food Categorization System.
         * Handles migration from old production v2 (with 'date' column) to new v9 schema.
         */
        val MIGRATION_2_9 = object : Migration(2, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // v2 schema: id, name, quantity, date, category
                // v9 schema: id, name, quantity, timestamp, category, ibs_impacts, is_custom, common_food_id

                // Create new food_items table with v9 schema
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS food_items_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        quantity TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        ibs_impacts TEXT NOT NULL DEFAULT '[]',
                        is_custom INTEGER NOT NULL DEFAULT 1,
                        common_food_id INTEGER
                    )
                """)

                // Copy data from old table (rename 'date' to 'timestamp')
                database.execSQL("""
                    INSERT INTO food_items_new (id, name, quantity, timestamp, category, ibs_impacts, is_custom, common_food_id)
                    SELECT id, name, quantity, date, category, '[]', 1, NULL
                    FROM food_items
                """)

                // Drop old table
                database.execSQL("DROP TABLE food_items")

                // Rename new table to food_items
                database.execSQL("ALTER TABLE food_items_new RENAME TO food_items")

                // Create indices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_food_items_timestamp ON food_items (timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_food_items_category ON food_items (category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_food_items_common_food_id ON food_items (common_food_id)")

                // Now create the new tables and populate data (reuse MIGRATION_8_9 logic)
                // Create common_foods table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS common_foods (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        ibs_impacts TEXT NOT NULL,
                        search_terms TEXT NOT NULL,
                        usage_count INTEGER NOT NULL DEFAULT 0,
                        name_fr TEXT,
                        name_en TEXT,
                        is_verified INTEGER NOT NULL DEFAULT 1,
                        created_at INTEGER NOT NULL
                    )
                """)

                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_common_foods_name ON common_foods (name)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_common_foods_category ON common_foods (category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_common_foods_usage_count ON common_foods (usage_count)")

                // Create food_usage_stats table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS food_usage_stats (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        food_name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        usage_count INTEGER NOT NULL,
                        last_used INTEGER NOT NULL,
                        ibs_impacts TEXT NOT NULL,
                        is_from_common_foods INTEGER NOT NULL DEFAULT 0
                    )
                """)

                database.execSQL("CREATE INDEX IF NOT EXISTS index_food_usage_stats_usage_count ON food_usage_stats (usage_count)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_food_usage_stats_category ON food_usage_stats (category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_food_usage_stats_last_used ON food_usage_stats (last_used)")

                // Pre-populate common foods
                val insertStmt = database.compileStatement(
                    """
                    INSERT INTO common_foods (name, category, ibs_impacts, search_terms, usage_count, name_fr, name_en, is_verified, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """
                )

                val currentTime = System.currentTimeMillis()
                com.tiarkaerell.ibstracker.util.PrePopulatedFoods.foods.forEach { food ->
                    insertStmt.clearBindings()
                    insertStmt.bindString(1, food.name)
                    insertStmt.bindString(2, food.category.name)
                    insertStmt.bindString(3, food.ibsImpacts.joinToString(",") { it.name })
                    insertStmt.bindString(4, food.searchTerms.joinToString("|"))
                    insertStmt.bindLong(5, food.usageCount.toLong())
                    food.nameFr?.let { insertStmt.bindString(6, it) }
                    food.nameEn?.let { insertStmt.bindString(7, it) }
                    insertStmt.bindLong(8, if (food.isVerified) 1 else 0)
                    insertStmt.bindLong(9, currentTime)
                    insertStmt.executeInsert()
                }

                // Initialize usage stats from existing food_items
                database.execSQL("""
                    INSERT INTO food_usage_stats (food_name, category, usage_count, last_used, ibs_impacts, is_from_common_foods)
                    SELECT
                        name,
                        category,
                        COUNT(*) as usage_count,
                        MAX(timestamp) as last_used,
                        ibs_impacts,
                        0 as is_from_common_foods
                    FROM food_items
                    GROUP BY name, category
                    HAVING COUNT(*) > 0
                """)
            }
        }

        /**
         * Migration from v9 to v10.
         * Backfills food_usage_stats from existing food_items for users who already had v9.
         *
         * Background:
         * - MIGRATION_2_9 included backfill logic when upgrading from v2→v9
         * - But users already on v9 never had their historical data backfilled
         * - This migration ensures all users have usage stats populated from historical food entries
         */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Delete existing stats (in case there are partial/incorrect entries)
                database.execSQL("DELETE FROM food_usage_stats")

                // Backfill from existing food_items
                database.execSQL("""
                    INSERT INTO food_usage_stats (food_name, category, usage_count, last_used, ibs_impacts, is_from_common_foods)
                    SELECT
                        name as food_name,
                        category,
                        COUNT(*) as usage_count,
                        MAX(timestamp) as last_used,
                        COALESCE(ibs_impacts, '[]') as ibs_impacts,
                        CASE WHEN common_food_id IS NOT NULL THEN 1 ELSE 0 END as is_from_common_foods
                    FROM food_items
                    GROUP BY name, category
                    HAVING COUNT(*) > 0
                """)
            }
        }
    }
}