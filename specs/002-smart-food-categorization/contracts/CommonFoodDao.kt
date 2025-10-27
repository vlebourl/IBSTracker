package com.tiarkaerell.ibstracker.data.database.dao

import androidx.room.*
import com.tiarkaerell.ibstracker.data.model.CommonFood
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for CommonFood entity.
 *
 * NEW in v9: Manages pre-populated common foods database (~150 foods)
 *
 * Contract Version: 1.0.0
 * Database Version: 9
 */
@Dao
interface CommonFoodDao {

    // ==================== CREATE ====================

    /**
     * Insert a single common food.
     *
     * OnConflict: REPLACE (updates existing food if name matches)
     *
     * Validation: Caller must ensure exactly one FODMAP_* value in ibsImpacts
     *
     * @param commonFood The common food to insert
     * @return Row ID of inserted/replaced item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(commonFood: CommonFood): Long

    /**
     * Insert multiple common foods in a transaction.
     *
     * Use Case: Pre-population during migration (150 foods)
     *
     * Performance: <5s for 150 foods (compiled statement)
     *
     * @param commonFoods List of common foods to insert
     * @return List of row IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(commonFoods: List<CommonFood>): List<Long>

    // ==================== READ ====================

    /**
     * Get all common foods sorted by usage count (most used first), then alphabetically.
     *
     * Sort Order: usage_count DESC, name ASC
     * Performance: p95 < 200ms for 500+ foods
     *
     * @return Flow of all common foods
     */
    @Query("""
        SELECT * FROM common_foods
        ORDER BY usage_count DESC, name ASC
    """)
    fun getAllCommonFoods(): Flow<List<CommonFood>>

    /**
     * Get common foods by category, sorted by usage count DESC, then alphabetically ASC.
     *
     * Use Case: Category detail screen showing all foods in a category
     *
     * @param category The food category to filter by
     * @return Flow of common foods in the specified category
     */
    @Query("""
        SELECT * FROM common_foods
        WHERE category = :category
        ORDER BY usage_count DESC, name ASC
    """)
    fun getCommonFoodsByCategory(category: FoodCategory): Flow<List<CommonFood>>

    /**
     * Get top N most-used common foods across all categories.
     *
     * Use Case: Quick-add shortcuts (N = 6)
     * Sort Order: usage_count DESC, name ASC (for ties)
     *
     * @param limit Number of foods to return (default 6)
     * @return Flow of top N common foods
     */
    @Query("""
        SELECT * FROM common_foods
        WHERE usage_count > 0
        ORDER BY usage_count DESC, name ASC
        LIMIT :limit
    """)
    fun getTopUsedCommonFoods(limit: Int = 6): Flow<List<CommonFood>>

    /**
     * Get a single common food by ID.
     *
     * @param id The common food ID
     * @return Flow of the common food (or null if not found)
     */
    @Query("SELECT * FROM common_foods WHERE id = :id")
    fun getCommonFoodById(id: Long): Flow<CommonFood?>

    /**
     * Get a common food by exact name (case-sensitive).
     *
     * Use Case: Check if user-entered food matches existing common food
     *
     * @param name The food name (e.g., "Banana")
     * @return Flow of the common food (or null if not found)
     */
    @Query("SELECT * FROM common_foods WHERE name = :name")
    fun getCommonFoodByName(name: String): Flow<CommonFood?>

    /**
     * Search common foods by name or search terms (case-insensitive substring match).
     *
     * Performance: p95 < 1s for 500+ foods
     * Limit: 50 results
     *
     * @param query Search query (e.g., "yogu" matches "yogurt", "yoghurt")
     * @return Flow of matching common foods
     */
    @Query("""
        SELECT * FROM common_foods
        WHERE name LIKE '%' || :query || '%'
           OR search_terms LIKE '%' || :query || '%'
        ORDER BY usage_count DESC, name ASC
        LIMIT 50
    """)
    fun searchCommonFoods(query: String): Flow<List<CommonFood>>

    /**
     * Get only verified common foods (pre-populated, not user-added).
     *
     * Use Case: Show only curated foods, exclude user additions
     *
     * @return Flow of verified common foods
     */
    @Query("""
        SELECT * FROM common_foods
        WHERE is_verified = 1
        ORDER BY usage_count DESC, name ASC
    """)
    fun getVerifiedCommonFoods(): Flow<List<CommonFood>>

    /**
     * Get user-added common foods (not pre-populated).
     *
     * Use Case: Show custom foods added by user
     *
     * @return Flow of user-added common foods
     */
    @Query("""
        SELECT * FROM common_foods
        WHERE is_verified = 0
        ORDER BY usage_count DESC, name ASC
    """)
    fun getUserAddedCommonFoods(): Flow<List<CommonFood>>

    // ==================== UPDATE ====================

    /**
     * Update an existing common food.
     *
     * Use Case: Edit category, IBS impacts, or translations
     *
     * Validation: Caller must ensure exactly one FODMAP_* value in ibsImpacts
     *
     * @param commonFood The common food to update
     * @return Number of rows updated (0 or 1)
     */
    @Update
    suspend fun update(commonFood: CommonFood): Int

    /**
     * Increment usage count for a common food by name.
     *
     * Use Case: Triggered when user logs a food item matching this common food
     *
     * Side Effect: Updates usage_count +1
     *
     * @param name The food name
     * @return Number of rows updated (0 or 1)
     */
    @Query("""
        UPDATE common_foods
        SET usage_count = usage_count + 1
        WHERE name = :name
    """)
    suspend fun incrementUsageCount(name: String): Int

    /**
     * Increment usage count for a common food by ID.
     *
     * @param id The common food ID
     * @return Number of rows updated (0 or 1)
     */
    @Query("""
        UPDATE common_foods
        SET usage_count = usage_count + 1
        WHERE id = :id
    """)
    suspend fun incrementUsageCountById(id: Long): Int

    /**
     * Reset usage count for a common food (testing/debugging).
     *
     * @param id The common food ID
     * @return Number of rows updated (0 or 1)
     */
    @Query("""
        UPDATE common_foods
        SET usage_count = 0
        WHERE id = :id
    """)
    suspend fun resetUsageCount(id: Long): Int

    /**
     * Reset all usage counts (testing/debugging).
     *
     * Use Case: Reset app to initial state
     *
     * @return Number of rows updated
     */
    @Query("""
        UPDATE common_foods
        SET usage_count = 0
    """)
    suspend fun resetAllUsageCounts(): Int

    // ==================== DELETE ====================

    /**
     * Delete a single common food.
     *
     * Warning: May orphan FoodItem entries with commonFoodId pointing to deleted food
     *
     * @param commonFood The common food to delete
     * @return Number of rows deleted (0 or 1)
     */
    @Delete
    suspend fun delete(commonFood: CommonFood): Int

    /**
     * Delete a common food by ID.
     *
     * @param id The common food ID
     * @return Number of rows deleted (0 or 1)
     */
    @Query("DELETE FROM common_foods WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    /**
     * Delete user-added common foods (keep only verified pre-populated foods).
     *
     * Use Case: Reset to initial pre-populated state
     *
     * @return Number of rows deleted
     */
    @Query("DELETE FROM common_foods WHERE is_verified = 0")
    suspend fun deleteUserAddedFoods(): Int

    /**
     * Delete all common foods (DANGER: irreversible).
     *
     * Use Case: Testing, full reset
     * Warning: Will break FoodItem foreign key references
     *
     * @return Number of rows deleted
     */
    @Query("DELETE FROM common_foods")
    suspend fun deleteAll(): Int

    // ==================== ANALYTICS ====================

    /**
     * Count total common foods in database.
     *
     * Expected: ~150+ (pre-populated + user-added)
     *
     * @return Flow of total count
     */
    @Query("SELECT COUNT(*) FROM common_foods")
    fun getCommonFoodCount(): Flow<Int>

    /**
     * Count common foods by category.
     *
     * @param category The category to count
     * @return Flow of count for specified category
     */
    @Query("SELECT COUNT(*) FROM common_foods WHERE category = :category")
    fun getCommonFoodCountByCategory(category: FoodCategory): Flow<Int>

    /**
     * Count verified common foods (pre-populated only).
     *
     * Expected: ~150 (initial pre-population)
     *
     * @return Flow of verified food count
     */
    @Query("SELECT COUNT(*) FROM common_foods WHERE is_verified = 1")
    fun getVerifiedFoodCount(): Flow<Int>

    /**
     * Get common food with highest usage count.
     *
     * Use Case: Display most-logged food in analytics
     *
     * @return Flow of the most-used common food (or null if database empty)
     */
    @Query("""
        SELECT * FROM common_foods
        WHERE usage_count > 0
        ORDER BY usage_count DESC, name ASC
        LIMIT 1
    """)
    fun getMostUsedCommonFood(): Flow<CommonFood?>
}
