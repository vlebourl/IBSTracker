/**
 * DataRepository API Contract
 * Feature: 004-fix-custom-food-persistence
 *
 * This contract defines the modified insertFoodItem() method signature and behavior.
 * No other repository methods are changed.
 */

package com.tiarkaerell.ibstracker.data.repository

import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.CommonFood
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import com.tiarkaerell.ibstracker.data.model.IBSImpact

/**
 * Insert a food item and update usage statistics.
 *
 * MODIFIED BEHAVIOR (Bug Fix):
 * 1. Check if CommonFood exists by exact name match
 * 2. If not found, create new CommonFood with:
 *    - isVerified = false (custom food marker)
 *    - ibsImpacts = [FODMAP_LOW] (safe default)
 *    - category = foodItem.category
 *    - usageCount = 0 (will increment to 1 after insert)
 * 3. Link FoodItem to CommonFood via commonFoodId
 * 4. Increment CommonFood.usage_count
 * 5. Upsert FoodUsageStats (existing behavior)
 *
 * PRE-CONDITIONS:
 * - foodItem.name must not be blank
 * - foodItem.category must be valid FoodCategory enum value
 * - Database schema v9 or higher (common_foods table exists)
 *
 * POST-CONDITIONS:
 * - FoodItem inserted with commonFoodId != null
 * - CommonFood exists in database (either found or created)
 * - CommonFood.usage_count incremented by 1
 * - FoodUsageStats updated (count incremented, last_used updated)
 * - UI displays custom food in category list (via Flow auto-update)
 * - Custom food appears in search results
 *
 * THREAD SAFETY:
 * - suspend function, executed on background thread
 * - Room handles transaction atomicity
 * - Safe to call from viewModelScope.launch
 *
 * ERROR HANDLING:
 * - Throws IllegalArgumentException if FODMAP validation fails
 * - Room throws SQLiteException on constraint violations
 * - Returns row ID on success
 *
 * BACKWARD COMPATIBILITY:
 * - Old FoodItems with commonFoodId = null continue working
 * - No migration required (nullable FK)
 *
 * @param foodItem The food item to insert
 * @return Row ID of inserted FoodItem
 * @throws IllegalArgumentException if CommonFood validation fails
 */
suspend fun insertFoodItem(foodItem: FoodItem): Long {
    // IMPLEMENTATION:
    //
    // Step 1: Check for existing CommonFood
    // val existingCommonFood = getCommonFoodByName(foodItem.name).first()
    //
    // Step 2: Create CommonFood if not found
    // val commonFood = existingCommonFood ?: run {
    //     val newCommonFood = CommonFood(
    //         name = foodItem.name,
    //         category = foodItem.category,
    //         ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
    //         isVerified = false,
    //         usageCount = 0
    //     )
    //     val commonFoodId = commonFoodDao.insert(newCommonFood)
    //     newCommonFood.copy(id = commonFoodId)
    // }
    //
    // Step 3: Insert FoodItem with commonFoodId
    // val updatedFoodItem = foodItem.copy(commonFoodId = commonFood.id)
    // val rowId = foodItemDao.insert(updatedFoodItem)
    //
    // Step 4: Update CommonFood usage count
    // commonFoodDao.incrementUsageCountById(commonFood.id)
    //
    // Step 5: Upsert FoodUsageStats (existing logic)
    // upsertUsageStats(...)
    //
    // return rowId
}

/**
 * Helper method: Get CommonFood by exact name match
 *
 * CONTRACT:
 * - Case-sensitive exact match on name field
 * - Returns first match (should be only one due to UI constraints)
 * - Returns null if no match found
 *
 * @param name Exact food name to search
 * @return Flow<CommonFood?> - CommonFood if found, null otherwise
 */
fun getCommonFoodByName(name: String): Flow<CommonFood?>

/**
 * Example Usage from FoodViewModel:
 *
 * fun saveFoodItem(
 *     name: String,
 *     category: FoodCategory = FoodCategory.OTHER,
 *     quantity: String = "",
 *     timestamp: Date = Date()
 * ) {
 *     viewModelScope.launch {
 *         dataRepository.insertFoodItem(
 *             FoodItem(
 *                 name = name,
 *                 quantity = quantity,
 *                 timestamp = timestamp,
 *                 category = category
 *                 // commonFoodId will be set by repository
 *             )
 *         )
 *     }
 * }
 */

/**
 * Test Cases:
 *
 * 1. Add new custom food "Soja":
 *    - Input: FoodItem(name="Soja", category=OTHER)
 *    - Expected: CommonFood created with isVerified=false
 *    - Expected: FoodItem.commonFoodId = CommonFood.id
 *    - Expected: CommonFood.usageCount = 1
 *
 * 2. Add duplicate custom food "Soja":
 *    - Input: FoodItem(name="Soja", category=OTHER)
 *    - Expected: No new CommonFood created
 *    - Expected: FoodItem.commonFoodId = existing CommonFood.id
 *    - Expected: CommonFood.usageCount incremented
 *
 * 3. Add pre-populated food "Riz blanc":
 *    - Input: FoodItem(name="Riz blanc", category=GRAINS_STARCHES)
 *    - Expected: No new CommonFood created
 *    - Expected: Uses existing verified CommonFood
 *    - Expected: CommonFood.usageCount incremented
 *
 * 4. Add custom food with special characters "Café au lait":
 *    - Input: FoodItem(name="Café au lait", category=DRINKS)
 *    - Expected: CommonFood created with exact name (UTF-8 support)
 *    - Expected: Search works with accented characters
 *
 * 5. Add 200 custom foods to same category:
 *    - Expected: All 200 foods searchable
 *    - Expected: Category list shows top 6 by usage
 *    - Expected: p95 < 500ms for category load
 */
