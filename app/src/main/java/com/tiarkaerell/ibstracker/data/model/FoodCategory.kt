package com.tiarkaerell.ibstracker.data.model

import androidx.compose.ui.graphics.Color

enum class FoodCategory(
    val color: Color
) {
    DAIRY(Color(0xFF6495ED)), // Cornflower blue
    GLUTEN(Color(0xFFDDA0DD)), // Plum
    HIGH_FODMAP(Color(0xFFFF6347)), // Tomato red
    SPICY(Color(0xFFFF4500)), // Orange red
    PROCESSED_FATTY(Color(0xFFFFD700)), // Gold
    BEVERAGES(Color(0xFF8B4513)), // Saddle brown
    FRUITS(Color(0xFF32CD32)), // Lime green
    VEGETABLES(Color(0xFF228B22)), // Forest green
    OTHER(Color(0xFF708090)); // Slate gray

    companion object {
        fun getByDisplayName(context: android.content.Context, name: String): FoodCategory {
            return values().find { 
                FoodCategoryHelper.getDisplayName(context, it) == name 
            } ?: OTHER
        }
        
        fun getAllCategories(): List<FoodCategory> {
            return values().toList()
        }
        
        fun getCommonTriggers(): List<FoodCategory> {
            return listOf(DAIRY, GLUTEN, HIGH_FODMAP, SPICY, PROCESSED_FATTY, BEVERAGES)
        }
    }
}