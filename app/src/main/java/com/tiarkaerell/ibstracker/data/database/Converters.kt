package com.tiarkaerell.ibstracker.data.database

import androidx.room.TypeConverter
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import com.tiarkaerell.ibstracker.data.model.IBSImpact
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

    @TypeConverter
    fun fromIBSImpactList(value: List<IBSImpact>): String {
        return value.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toIBSImpactList(value: String): List<IBSImpact> {
        if (value.isEmpty()) return emptyList()
        return try {
            value.split(",").map { IBSImpact.valueOf(it.trim()) }
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString("|")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split("|")
    }
}