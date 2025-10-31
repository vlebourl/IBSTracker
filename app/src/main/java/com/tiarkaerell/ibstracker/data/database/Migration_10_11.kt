package com.tiarkaerell.ibstracker.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log

/**
 * Database Migration from version 10 to version 11.
 *
 * Changes:
 * - Add index on symptoms.date column for improved query performance
 *
 * Performance Impact:
 * - Queries with ORDER BY date DESC will be significantly faster
 * - Index creation is fast (<100ms even with 1000+ entries)
 *
 * Rollback: Automatic via Room transaction (all-or-nothing)
 */
val MIGRATION_10_11 = object : Migration(10, 11) {
    private val TAG = "Migration_10_11"

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Log.d(TAG, "Starting migration from version 10 to 11")

            // Add index on symptoms.date column
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_symptoms_date` ON `symptoms` (`date`)")

            Log.d(TAG, "Migration from version 10 to 11 completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed: ${e.message}", e)
            throw e  // Re-throw to trigger Room's automatic rollback
        }
    }
}
