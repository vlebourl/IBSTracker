package com.tiarkaerell.ibstracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tiarkaerell.ibstracker.data.model.FoodItem
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
}