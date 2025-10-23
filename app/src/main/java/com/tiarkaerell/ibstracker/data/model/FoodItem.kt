package com.tiarkaerell.ibstracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * FoodItem Entity - Smart Food Categorization System (v1.9.0)
 *
 * User-logged food entries with timestamps and IBS attributes.
 *
 * Migration v8→v9:
 * - Replaced old `category` (old 9-category system) with new FoodCategory enum (12 categories)
 * - Added `ibsImpacts` for IBS impact attributes
 * - Added `isCustom` to distinguish user-added vs pre-populated foods
 * - Added `commonFoodId` for linking to CommonFood database
 */
@Entity(
    tableName = "food_items",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["category"]),
        Index(value = ["common_food_id"])
    ]
)
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "quantity")
    val quantity: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Date,

    @ColumnInfo(name = "category")
    val category: FoodCategory,

    @ColumnInfo(name = "ibs_impacts", defaultValue = "[]")
    val ibsImpacts: List<IBSImpact> = emptyList(),

    @ColumnInfo(name = "is_custom", defaultValue = "1")
    val isCustom: Boolean = true,

    @ColumnInfo(name = "common_food_id")
    val commonFoodId: Long? = null
)

/**
 * FrequentFoodItem - Lightweight DTO for analytics
 * Used for displaying frequently logged foods in UI
 */
data class FrequentFoodItem(
    val name: String,
    val category: FoodCategory,
    val count: Int
)
