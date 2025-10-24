package com.tiarkaerell.ibstracker.data.repository

import com.tiarkaerell.ibstracker.data.database.dao.CommonFoodDao
import com.tiarkaerell.ibstracker.data.database.dao.FoodItemDao
import com.tiarkaerell.ibstracker.data.database.dao.FoodUsageStatsDao
import com.tiarkaerell.ibstracker.data.database.dao.SymptomDao
import com.tiarkaerell.ibstracker.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

/**
 * Repository layer for Smart Food Categorization feature.
 *
 * MODIFIED from v8: Added CommonFood and FoodUsageStats methods
 *
 * Architecture: Wraps DAOs and provides business logic for:
 * - Automatic usage tracking when logging food items
 * - Syncing CommonFood usage counts with FoodUsageStats
 * - Coordinating multi-table operations in transactions
 */
class DataRepository(
    private val foodItemDao: FoodItemDao,
    private val commonFoodDao: CommonFoodDao,
    private val foodUsageStatsDao: FoodUsageStatsDao,
    private val symptomDao: SymptomDao
) {

    // ==================== FOOD ITEMS ====================

    fun getAllFoodItems(): Flow<List<FoodItem>> = foodItemDao.getAllFoodItems()

    fun getFoodItemsByCategory(category: FoodCategory): Flow<List<FoodItem>> =
        foodItemDao.getFoodItemsByCategory(category)

    fun getFoodItemsByDateRange(startDate: Date, endDate: Date): Flow<List<FoodItem>> =
        foodItemDao.getFoodItemsByDateRange(startDate, endDate)

    fun getFoodItemById(id: Long): Flow<FoodItem?> = foodItemDao.getFoodItemById(id)

    fun searchFoodItems(query: String): Flow<List<FoodItem>> = foodItemDao.searchFoodItems(query)

    /**
     * Insert a food item and update usage statistics.
     *
     * Business Logic:
     * 1. Insert food item to food_items table
     * 2. If commonFoodId is not null, increment CommonFood.usage_count
     * 3. Upsert FoodUsageStats (increment count or create new entry)
     */
    suspend fun insertFoodItem(foodItem: FoodItem): Long {
        // Step 1: Insert food item
        val rowId = foodItemDao.insert(foodItem)

        // Step 2: Update CommonFood usage count (if linked)
        foodItem.commonFoodId?.let { commonFoodId ->
            commonFoodDao.incrementUsageCountById(commonFoodId)
        }

        // Step 3: Upsert FoodUsageStats
        upsertUsageStats(
            foodItem.name,
            foodItem.category,
            foodItem.timestamp,
            foodItem.ibsImpacts,
            foodItem.commonFoodId != null
        )

        return rowId
    }

    suspend fun updateFoodItem(foodItem: FoodItem): Int {
        return foodItemDao.update(foodItem)
    }

    /**
     * Delete a food item and decrement usage statistics.
     *
     * Business Logic:
     * 1. Delete food item from food_items table
     * 2. Decrement FoodUsageStats.usage_count
     * 3. If usage_count reaches 0, delete the stats entry
     */
    suspend fun deleteFoodItem(foodItem: FoodItem): Int {
        // Step 1: Delete food item
        val deletedCount = foodItemDao.delete(foodItem)

        if (deletedCount > 0) {
            // Step 2: Decrement usage stats
            val decremented = foodUsageStatsDao.decrementUsageCount(foodItem.name, foodItem.category)

            // Step 3: Cleanup zero-usage stats
            if (decremented > 0) {
                foodUsageStatsDao.deleteZeroUsageStats()
            }
        }

        return deletedCount
    }

    suspend fun deleteAllFoodItems(): Int = foodItemDao.deleteAll()

    fun getFoodItemCount(): Flow<Int> = foodItemDao.getFoodItemCount()

    // ==================== COMMON FOODS ====================

    fun getAllCommonFoods(): Flow<List<CommonFood>> = commonFoodDao.getAllCommonFoods()

    fun getCommonFoodsByCategory(category: FoodCategory): Flow<List<CommonFood>> =
        commonFoodDao.getCommonFoodsByCategory(category)

    fun getTopUsedCommonFoods(limit: Int = 6): Flow<List<CommonFood>> =
        commonFoodDao.getTopUsedCommonFoods(limit)

    fun getCommonFoodById(id: Long): Flow<CommonFood?> = commonFoodDao.getCommonFoodById(id)

    fun getCommonFoodByName(name: String): Flow<CommonFood?> = commonFoodDao.getCommonFoodByName(name)

    fun searchCommonFoods(query: String): Flow<List<CommonFood>> = commonFoodDao.searchCommonFoods(query)

    suspend fun insertCommonFood(commonFood: CommonFood): Long {
        // Validate exactly one FODMAP level
        val fodmapCount = commonFood.ibsImpacts.count { it.name.startsWith("FODMAP_") }
        require(fodmapCount == 1) {
            "CommonFood must have exactly one FODMAP level (found $fodmapCount)"
        }
        return commonFoodDao.insert(commonFood)
    }

    suspend fun updateCommonFood(commonFood: CommonFood): Int {
        // Validate exactly one FODMAP level
        val fodmapCount = commonFood.ibsImpacts.count { it.name.startsWith("FODMAP_") }
        require(fodmapCount == 1) {
            "CommonFood must have exactly one FODMAP level (found $fodmapCount)"
        }
        return commonFoodDao.update(commonFood)
    }

    suspend fun deleteCommonFood(commonFood: CommonFood): Int = commonFoodDao.delete(commonFood)

    fun getCommonFoodCount(): Flow<Int> = commonFoodDao.getCommonFoodCount()

    // ==================== FOOD USAGE STATS ====================

    fun getAllStats(): Flow<List<FoodUsageStats>> = foodUsageStatsDao.getAllStats()

    fun getTopUsedFoods(limit: Int = 4): Flow<List<FoodUsageStats>> =
        foodUsageStatsDao.getTopUsedFoods(limit)

    fun getStatsByFoodAndCategory(foodName: String, category: FoodCategory): Flow<FoodUsageStats?> =
        foodUsageStatsDao.getStatsByFoodAndCategory(foodName, category)

    fun getStatsByCategory(category: FoodCategory): Flow<List<FoodUsageStats>> =
        foodUsageStatsDao.getStatsByCategory(category)

    fun searchStats(query: String): Flow<List<FoodUsageStats>> = foodUsageStatsDao.searchStats(query)

    suspend fun insertOrUpdateStats(stats: FoodUsageStats): Long = foodUsageStatsDao.insert(stats)

    suspend fun deleteAllStats(): Int = foodUsageStatsDao.deleteAll()

    fun getStatsCount(): Flow<Int> = foodUsageStatsDao.getStatsCount()

    fun getTotalUsageCount(): Flow<Int?> = foodUsageStatsDao.getTotalUsageCount()

    /**
     * Private helper to upsert FoodUsageStats.
     *
     * Logic:
     * - If stats exist: increment usage_count, update last_used
     * - If stats don't exist: create new entry with count = 1
     */
    private suspend fun upsertUsageStats(
        foodName: String,
        category: FoodCategory,
        lastUsed: Date,
        ibsImpacts: List<IBSImpact>,
        isFromCommonFoods: Boolean
    ) {
        val existingStats = getStatsByFoodAndCategory(foodName, category).first()

        if (existingStats != null) {
            // Increment existing stats
            foodUsageStatsDao.incrementUsageCount(foodName, category, lastUsed)
        } else {
            // Create new stats entry
            val newStats = FoodUsageStats(
                foodName = foodName,
                category = category,
                usageCount = 1,
                lastUsed = lastUsed,
                ibsImpacts = ibsImpacts,
                isFromCommonFoods = isFromCommonFoods
            )
            foodUsageStatsDao.insert(newStats)
        }
    }

    // ==================== SYMPTOMS ====================

    fun getAllSymptoms(): Flow<List<Symptom>> = symptomDao.getAll()

    suspend fun insertSymptom(symptom: Symptom) = symptomDao.insert(symptom)

    suspend fun updateSymptom(symptom: Symptom) = symptomDao.update(symptom)

    suspend fun deleteSymptom(symptom: Symptom) = symptomDao.delete(symptom)
}
