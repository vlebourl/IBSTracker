package com.tiarkaerell.ibstracker.data.database.dao

import androidx.room.*
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import com.tiarkaerell.ibstracker.data.model.FoodItem
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for FoodItem entity.
 *
 * MODIFIED from v8: Updated queries to use new schema fields (category, ibsImpacts, isCustom, commonFoodId)
 *
 * Contract Version: 1.0.0
 * Database Version: 9 (migrated from v8)
 */
@Dao
interface FoodItemDao {

    // ==================== CREATE ====================

    /**
     * Insert a single food item.
     *
     * Side Effect: Triggers FoodUsageStats update (increments usage_count, updates last_used)
     *
     * @param foodItem The food item to insert
     * @return Row ID of inserted item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItem): Long

    /**
     * Insert multiple food items in a transaction.
     *
     * Side Effect: Triggers FoodUsageStats update for each item
     *
     * @param foodItems List of food items to insert
     * @return List of row IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foodItems: List<FoodItem>): List<Long>

    // ==================== READ ====================

    /**
     * Get all food items ordered by timestamp descending (most recent first).
     *
     * Performance: p95 < 200ms for 5000+ entries
     * Returns: Flow for reactive updates
     */
    @Query("""
        SELECT * FROM food_items
        ORDER BY timestamp DESC
    """)
    fun getAllFoodItems(): Flow<List<FoodItem>>

    /**
     * Get food items by category.
     *
     * @param category The food category to filter by
     * @return Flow of food items in the specified category
     */
    @Query("""
        SELECT * FROM food_items
        WHERE category = :category
        ORDER BY timestamp DESC
    """)
    fun getFoodItemsByCategory(category: FoodCategory): Flow<List<FoodItem>>

    /**
     * Get food items within a date range.
     *
     * @param startDate Start of range (inclusive)
     * @param endDate End of range (inclusive)
     * @return Flow of food items within date range
     */
    @Query("""
        SELECT * FROM food_items
        WHERE timestamp BETWEEN :startDate AND :endDate
        ORDER BY timestamp DESC
    """)
    fun getFoodItemsByDateRange(startDate: Date, endDate: Date): Flow<List<FoodItem>>

    /**
     * Get a single food item by ID.
     *
     * @param id The food item ID
     * @return Flow of the food item (or null if not found)
     */
    @Query("SELECT * FROM food_items WHERE id = :id")
    fun getFoodItemById(id: Long): Flow<FoodItem?>

    /**
     * Get food items linked to a common food.
     *
     * @param commonFoodId The common food ID
     * @return Flow of food items from this common food
     */
    @Query("""
        SELECT * FROM food_items
        WHERE common_food_id = :commonFoodId
        ORDER BY timestamp DESC
    """)
    fun getFoodItemsByCommonFood(commonFoodId: Long): Flow<List<FoodItem>>

    /**
     * Get custom food items (user-added, not from common_foods).
     *
     * @return Flow of custom food items
     */
    @Query("""
        SELECT * FROM food_items
        WHERE is_custom = 1
        ORDER BY timestamp DESC
    """)
    fun getCustomFoodItems(): Flow<List<FoodItem>>

    /**
     * Search food items by name (case-insensitive substring match).
     *
     * @param query Search query (e.g., "bread")
     * @return Flow of matching food items
     */
    @Query("""
        SELECT * FROM food_items
        WHERE name LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
        LIMIT 50
    """)
    fun searchFoodItems(query: String): Flow<List<FoodItem>>

    // ==================== UPDATE ====================

    /**
     * Update an existing food item.
     *
     * Side Effect: May trigger FoodUsageStats update if name or category changed
     *
     * @param foodItem The food item to update
     * @return Number of rows updated (0 or 1)
     */
    @Update
    suspend fun update(foodItem: FoodItem): Int

    /**
     * Update multiple food items in a transaction.
     *
     * @param foodItems List of food items to update
     * @return Number of rows updated
     */
    @Update
    suspend fun updateAll(foodItems: List<FoodItem>): Int

    // ==================== DELETE ====================

    /**
     * Delete a single food item.
     *
     * Side Effect: Decrements FoodUsageStats.usage_count, may delete stats entry if count reaches 0
     *
     * @param foodItem The food item to delete
     * @return Number of rows deleted (0 or 1)
     */
    @Delete
    suspend fun delete(foodItem: FoodItem): Int

    /**
     * Delete a food item by ID.
     *
     * Side Effect: Decrements FoodUsageStats.usage_count
     *
     * @param id The food item ID
     * @return Number of rows deleted (0 or 1)
     */
    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    /**
     * Delete all food items (DANGER: irreversible).
     *
     * Side Effect: Clears all FoodUsageStats entries
     * Use Case: Testing, reset app data
     *
     * @return Number of rows deleted
     */
    @Query("DELETE FROM food_items")
    suspend fun deleteAll(): Int

    /**
     * Delete food items older than a specific date.
     *
     * Side Effect: Updates FoodUsageStats (decrements counts)
     *
     * @param beforeDate Delete items before this date
     * @return Number of rows deleted
     */
    @Query("DELETE FROM food_items WHERE timestamp < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: Date): Int

    // ==================== ANALYTICS ====================

    /**
     * Count total food items in database.
     *
     * @return Flow of total count
     */
    @Query("SELECT COUNT(*) FROM food_items")
    fun getFoodItemCount(): Flow<Int>

    /**
     * Count food items by category.
     *
     * @param category The category to count
     * @return Flow of count for specified category
     */
    @Query("SELECT COUNT(*) FROM food_items WHERE category = :category")
    fun getFoodItemCountByCategory(category: FoodCategory): Flow<Int>

    /**
     * Get the most recently logged food item.
     *
     * @return Flow of the most recent food item (or null if database empty)
     */
    @Query("""
        SELECT * FROM food_items
        ORDER BY timestamp DESC
        LIMIT 1
    """)
    fun getMostRecentFoodItem(): Flow<FoodItem?>
}
