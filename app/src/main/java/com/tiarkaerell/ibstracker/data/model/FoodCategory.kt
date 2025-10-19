package com.tiarkaerell.ibstracker.data.model

import androidx.compose.ui.graphics.Color

enum class FoodCategory(
    val displayName: String,
    val color: Color,
    val description: String
) {
    DAIRY(
        displayName = "Dairy",
        color = Color(0xFF6495ED), // Cornflower blue
        description = "Milk, cheese, yogurt, ice cream"
    ),
    GLUTEN(
        displayName = "Gluten",
        color = Color(0xFFDDA0DD), // Plum
        description = "Bread, pasta, wheat products"
    ),
    HIGH_FODMAP(
        displayName = "High FODMAP",
        color = Color(0xFFFF6347), // Tomato red
        description = "Onions, garlic, beans, certain fruits"
    ),
    SPICY(
        displayName = "Spicy",
        color = Color(0xFFFF4500), // Orange red
        description = "Hot peppers, spicy seasonings"
    ),
    PROCESSED_FATTY(
        displayName = "Processed/Fatty",
        color = Color(0xFFFFD700), // Gold
        description = "Fried foods, processed meals, high-fat items"
    ),
    BEVERAGES(
        displayName = "Beverages",
        color = Color(0xFF8B4513), // Saddle brown
        description = "Coffee, tea, alcohol, sodas, energy drinks"
    ),
    FRUITS(
        displayName = "Fruits",
        color = Color(0xFF32CD32), // Lime green
        description = "Fresh fruits, fruit juices"
    ),
    VEGETABLES(
        displayName = "Vegetables",
        color = Color(0xFF228B22), // Forest green
        description = "Fresh vegetables, salads"
    ),
    OTHER(
        displayName = "Other",
        color = Color(0xFF708090), // Slate gray
        description = "Uncategorized foods"
    );

    companion object {
        fun getByDisplayName(name: String): FoodCategory {
            return values().find { it.displayName == name } ?: OTHER
        }
        
        fun getAllCategories(): List<FoodCategory> {
            return values().toList()
        }
        
        fun getCommonTriggers(): List<FoodCategory> {
            return listOf(DAIRY, GLUTEN, HIGH_FODMAP, SPICY, PROCESSED_FATTY, BEVERAGES)
        }
    }
}