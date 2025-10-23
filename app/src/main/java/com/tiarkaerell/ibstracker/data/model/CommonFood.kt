package com.tiarkaerell.ibstracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * CommonFood Entity - Smart Food Categorization System (v1.9.0)
 *
 * Pre-populated database of ~150 common foods with verified IBS attributes.
 * This serves as a knowledge base for quick-add suggestions and guided categorization.
 *
 * Pre-population:
 * - ~150 foods loaded during Migration_8_9 from PrePopulatedFoods.kt
 * - All pre-populated foods have isVerified = true
 * - Usage counts start at 0, updated when user logs food
 */
@Entity(
    tableName = "common_foods",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["category"]),
        Index(value = ["usage_count"])
    ]
)
data class CommonFood(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,  // English name (primary key for search)

    @ColumnInfo(name = "category")
    val category: FoodCategory,

    @ColumnInfo(name = "ibs_impacts")
    val ibsImpacts: List<IBSImpact>,

    @ColumnInfo(name = "search_terms")
    val searchTerms: List<String> = emptyList(),  // Aliases for fuzzy search

    @ColumnInfo(name = "usage_count", defaultValue = "0")
    val usageCount: Int = 0,  // Incremented when user logs this food

    @ColumnInfo(name = "name_fr")
    val nameFr: String? = null,  // French translation (optional)

    @ColumnInfo(name = "name_en")
    val nameEn: String? = null,  // Explicit English name (optional)

    @ColumnInfo(name = "is_verified", defaultValue = "1")
    val isVerified: Boolean = true,  // true = pre-populated, false = user-added

    @ColumnInfo(name = "created_at")
    val createdAt: Date
)
