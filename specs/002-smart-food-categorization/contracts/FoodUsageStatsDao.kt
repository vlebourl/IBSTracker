package com.tiarkaerell.ibstracker.data.database.dao

import androidx.room.*
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import com.tiarkaerell.ibstracker.data.model.FoodUsageStats
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for FoodUsageStats entity.
 *
 * NEW in v9: Aggregated usage statistics for quick-add shortcuts
 *
 * Contract Version: 1.0.0
 * Database Version: 9
 */
@Dao
interface FoodUsageStatsDao {

    // ==================== CREATE ====================

    /**
     * Insert a single food usage stats entry.
     *
     * OnConflict: REPLACE (updates existing entry for same food name + category)
     *
     * @param stats The usage stats to insert
     * @return Row ID of inserted/replaced item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: FoodUsageStats): Long

    /**
     * Insert multiple usage stats in a transaction.
     *
     * Use Case: Batch update after importing data
     *
     * @param statsList List of usage stats to insert
     * @return List of row IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statsList: List<FoodUsageStats>): List<Long>

    // ==================== READ ====================

    /**
     * Get all food usage stats sorted by usage count DESC, then alphabetically ASC.
     *
     * Sort Order: usage_count DESC, food_name ASC
     * Performance: p95 < 50ms (typically <100 entries)
     *
     * @return Flow of all usage stats
     */
    @Query("""
        SELECT * FROM food_usage_stats
        ORDER BY usage_count DESC, food_name ASC
    """)
    fun getAllStats(): Flow<List<FoodUsageStats>>

    /**
     * Get top N most-used foods for quick-add shortcuts.
     *
     * Use Case: Dashboard quick-add section (N = 6)
     * Sort Order: usage_count DESC, food_name ASC (for ties)
     * Performance: p95 < 50ms (critical for UI responsiveness)
     *
     * @param limit Number of results to return (default 6)
     * @return Flow of top N usage stats
     */
    @Query("""
        SELECT * FROM food_usage_stats
        ORDER BY usage_count DESC, food_name ASC
        LIMIT :limit
    """)
    fun getTopUsedFoods(limit: Int = 6): Flow<List<FoodUsageStats>>

    /**
     * Get usage stats for a specific food by name and category.
     *
     * Use Case: Check if stats entry exists before incrementing
     *
     * @param foodName The food name
     * @param category The food category
     * @return Flow of the usage stats (or null if not found)
     */
    @Query("""
        SELECT * FROM food_usage_stats
        WHERE food_name = :foodName AND category = :category
    """)
    fun getStatsByFoodAndCategory(foodName: String, category: FoodCategory): Flow<FoodUsageStats?>

    /**
     * Get usage stats by category.
     *
     * @param category The food category
     * @return Flow of usage stats in the specified category
     */
    @Query("""
        SELECT * FROM food_usage_stats
        WHERE category = :category
        ORDER BY usage_count DESC, food_name ASC
    """)
    fun getStatsByCategory(category: FoodCategory): Flow<List<FoodUsageStats>>

    /**
     * Get usage stats for foods from common_foods table only.
     *
     * Use Case: Analytics on pre-populated foods vs custom foods
     *
     * @return Flow of stats for common foods
     */
    @Query("""
        SELECT * FROM food_usage_stats
        WHERE is_from_common_foods = 1
        ORDER BY usage_count DESC, food_name ASC
    """)
    fun getStatsForCommonFoods(): Flow<List<FoodUsageStats>>

    /**
     * Get usage stats for custom foods (not in common_foods table).
     *
     * @return Flow of stats for custom foods
     */
    @Query("""
        SELECT * FROM food_usage_stats
        WHERE is_from_common_foods = 0
        ORDER BY usage_count DESC, food_name ASC
    """)
    fun getStatsForCustomFoods(): Flow<List<FoodUsageStats>>

    /**
     * Search usage stats by food name (case-insensitive substring match).
     *
     * @param query Search query
     * @return Flow of matching usage stats
     */
    @Query("""
        SELECT * FROM food_usage_stats
        WHERE food_name LIKE '%' || :query || '%'
        ORDER BY usage_count DESC, food_name ASC
        LIMIT 50
    """)
    fun searchStats(query: String): Flow<List<FoodUsageStats>>

    // ==================== UPDATE ====================

    /**
     * Update an existing usage stats entry.
     *
     * Use Case: Modify cached IBS impacts after editing common food
     *
     * @param stats The usage stats to update
     * @return Number of rows updated (0 or 1)
     */
    @Update
    suspend fun update(stats: FoodUsageStats): Int

    /**
     * Increment usage count for a food (by name + category).
     *
     * Use Case: Triggered when user logs a food item
     * Side Effect: Also updates last_used timestamp
     *
     * @param foodName The food name
     * @param category The food category
     * @param newLastUsed The timestamp of the new log entry
     * @return Number of rows updated (0 or 1)
     */
    @Query("""
        UPDATE food_usage_stats
        SET usage_count = usage_count + 1,
            last_used = :newLastUsed
        WHERE food_name = :foodName AND category = :category
    """)
    suspend fun incrementUsageCount(foodName: String, category: FoodCategory, newLastUsed: Date): Int

    /**
     * Decrement usage count for a food (by name + category).
     *
     * Use Case: Triggered when user deletes a food item
     * Side Effect: Deletes entry if usage_count reaches 0
     *
     * @param foodName The food name
     * @param category The food category
     * @return Number of rows updated (0 or 1)
     */
    @Query("""
        UPDATE food_usage_stats
        SET usage_count = usage_count - 1
        WHERE food_name = :foodName AND category = :category
    """)
    suspend fun decrementUsageCount(foodName: String, category: FoodCategory): Int

    /**
     * Update last_used timestamp for a food.
     *
     * Use Case: Update timestamp without incrementing count
     *
     * @param foodName The food name
     * @param category The food category
     * @param lastUsed The new last_used timestamp
     * @return Number of rows updated (0 or 1)
     */
    @Query("""
        UPDATE food_usage_stats
        SET last_used = :lastUsed
        WHERE food_name = :foodName AND category = :category
    """)
    suspend fun updateLastUsed(foodName: String, category: FoodCategory, lastUsed: Date): Int

    // ==================== DELETE ====================

    /**
     * Delete a single usage stats entry.
     *
     * Use Case: Cleanup entries with 0 usage_count
     *
     * @param stats The usage stats to delete
     * @return Number of rows deleted (0 or 1)
     */
    @Delete
    suspend fun delete(stats: FoodUsageStats): Int

    /**
     * Delete usage stats by food name and category.
     *
     * @param foodName The food name
     * @param category The food category
     * @return Number of rows deleted (0 or 1)
     */
    @Query("""
        DELETE FROM food_usage_stats
        WHERE food_name = :foodName AND category = :category
    """)
    suspend fun deleteByFoodAndCategory(foodName: String, category: FoodCategory): Int

    /**
     * Delete usage stats with 0 usage count (cleanup).
     *
     * Use Case: Maintenance task after batch deletions
     *
     * @return Number of rows deleted
     */
    @Query("DELETE FROM food_usage_stats WHERE usage_count <= 0")
    suspend fun deleteZeroUsageStats(): Int

    /**
     * Delete all usage stats (DANGER: irreversible).
     *
     * Use Case: Testing, full reset
     * Side Effect: Clears quick-add shortcuts
     *
     * @return Number of rows deleted
     */
    @Query("DELETE FROM food_usage_stats")
    suspend fun deleteAll(): Int

    /**
     * Delete usage stats older than a specific date.
     *
     * Use Case: Archive old data
     *
     * @param beforeDate Delete stats with last_used before this date
     * @return Number of rows deleted
     */
    @Query("DELETE FROM food_usage_stats WHERE last_used < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: Date): Int

    // ==================== ANALYTICS ====================

    /**
     * Count total usage stats entries.
     *
     * Expected: <1000 (one entry per unique food name + category pair)
     *
     * @return Flow of total count
     */
    @Query("SELECT COUNT(*) FROM food_usage_stats")
    fun getStatsCount(): Flow<Int>

    /**
     * Sum total usage count across all foods.
     *
     * Use Case: Display total logged foods count
     *
     * @return Flow of total usage count
     */
    @Query("SELECT SUM(usage_count) FROM food_usage_stats")
    fun getTotalUsageCount(): Flow<Int?>

    /**
     * Get the food with highest usage count.
     *
     * Use Case: Display most-logged food in analytics
     *
     * @return Flow of the most-used food stats (or null if database empty)
     */
    @Query("""
        SELECT * FROM food_usage_stats
        ORDER BY usage_count DESC, food_name ASC
        LIMIT 1
    """)
    fun getMostUsedFood(): Flow<FoodUsageStats?>

    /**
     * Get the most recently logged food.
     *
     * @return Flow of the most recently used food stats (or null if database empty)
     */
    @Query("""
        SELECT * FROM food_usage_stats
        ORDER BY last_used DESC
        LIMIT 1
    """)
    fun getMostRecentlyUsedFood(): Flow<FoodUsageStats?>

    /**
     * Get average usage count across all foods.
     *
     * Use Case: Analytics dashboard
     *
     * @return Flow of average usage count
     */
    @Query("SELECT AVG(usage_count) FROM food_usage_stats")
    fun getAverageUsageCount(): Flow<Double?>
}
