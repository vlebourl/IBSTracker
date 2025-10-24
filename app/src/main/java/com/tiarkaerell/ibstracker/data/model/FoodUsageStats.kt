package com.tiarkaerell.ibstracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * FoodUsageStats Entity - Smart Food Categorization System (v1.9.0)
 *
 * Aggregated usage statistics for quick-add shortcuts.
 * Tracks the top most-used foods for fast logging.
 *
 * Update Strategy:
 * - Auto-updated via DAO after FoodItem insert
 * - Sorted by usage_count DESC, foodName ASC for UI display
 * - Top 6 results used for quick-add shortcuts on Food screen
 *
 * Data Source:
 * - Matches FoodItem.name or CommonFood.name
 * - Cached IBS impacts from CommonFood or last FoodItem entry
 */
@Entity(
    tableName = "food_usage_stats",
    indices = [
        Index(value = ["usage_count"]),
        Index(value = ["category"]),
        Index(value = ["last_used"])
    ]
)
data class FoodUsageStats(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "food_name")
    val foodName: String,  // Matches FoodItem.name or CommonFood.name

    @ColumnInfo(name = "category")
    val category: FoodCategory,

    @ColumnInfo(name = "usage_count")
    val usageCount: Int,  // Number of times logged (must be > 0)

    @ColumnInfo(name = "last_used")
    val lastUsed: Date,  // Most recent timestamp

    @ColumnInfo(name = "ibs_impacts")
    val ibsImpacts: List<IBSImpact> = emptyList(),  // Cached from CommonFood or last FoodItem

    @ColumnInfo(name = "is_from_common_foods", defaultValue = "0")
    val isFromCommonFoods: Boolean = false  // true if matches CommonFood, false if custom
)
