package com.tiarkaerell.ibstracker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tiarkaerell.ibstracker.data.model.FoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Insert
    suspend fun insert(foodItem: FoodItem)

    @Query("SELECT * FROM food_items ORDER BY date DESC")
    fun getAll(): Flow<List<FoodItem>>
}