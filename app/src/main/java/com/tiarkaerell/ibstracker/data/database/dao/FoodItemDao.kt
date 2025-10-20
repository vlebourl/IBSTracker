package com.tiarkaerell.ibstracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.FrequentFoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Insert
    suspend fun insert(foodItem: FoodItem)

    @Update
    suspend fun update(foodItem: FoodItem)

    @Delete
    suspend fun delete(foodItem: FoodItem)

    @Query("SELECT * FROM food_items ORDER BY date DESC")
    fun getAll(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items ORDER BY date DESC")
    suspend fun getAllFoodItems(): List<FoodItem>

    @Query("DELETE FROM food_items")
    suspend fun deleteAllFoodItems()

    @Insert
    suspend fun insertFoodItem(foodItem: FoodItem): Long

    @Query("""
        SELECT name, category, COUNT(*) as count
        FROM food_items
        GROUP BY name, category
        HAVING COUNT(*) > 1
        ORDER BY count DESC
        LIMIT 4
    """)
    fun getFrequentFoodItems(): Flow<List<FrequentFoodItem>>
}