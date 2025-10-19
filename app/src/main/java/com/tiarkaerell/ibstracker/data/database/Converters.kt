package com.tiarkaerell.ibstracker.data.database

import androidx.room.TypeConverter
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
    
    @TypeConverter
    fun fromFoodCategory(category: FoodCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toFoodCategory(categoryName: String): FoodCategory {
        return try {
            FoodCategory.valueOf(categoryName)
        } catch (e: IllegalArgumentException) {
            FoodCategory.OTHER
        }
    }
}