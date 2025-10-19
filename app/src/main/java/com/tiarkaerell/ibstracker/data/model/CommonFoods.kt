package com.tiarkaerell.ibstracker.data.model

import android.content.Context
import com.tiarkaerell.ibstracker.R

object CommonFoods {
    fun getCommonFoods(context: Context, category: FoodCategory): List<String> {
        val arrayResId = when (category) {
            FoodCategory.DAIRY -> R.array.foods_dairy
            FoodCategory.GLUTEN -> R.array.foods_gluten
            FoodCategory.HIGH_FODMAP -> R.array.foods_high_fodmap
            FoodCategory.SPICY -> R.array.foods_spicy
            FoodCategory.PROCESSED_FATTY -> R.array.foods_processed_fatty
            FoodCategory.BEVERAGES -> R.array.foods_beverages
            FoodCategory.FRUITS -> R.array.foods_fruits
            FoodCategory.VEGETABLES -> R.array.foods_vegetables
            FoodCategory.OTHER -> R.array.foods_other
        }
        
        return context.resources.getStringArray(arrayResId).toList()
    }
    
    data class FoodSearchResult(
        val foodName: String,
        val category: FoodCategory
    )
    
    fun searchFoods(context: Context, query: String): List<FoodSearchResult> {
        if (query.isBlank()) return emptyList()
        
        val results = mutableListOf<Pair<FoodSearchResult, Int>>()
        val normalizedQuery = query.lowercase().trim()
        
        FoodCategory.getAllCategories().forEach { category ->
            val foods = getCommonFoods(context, category)
            foods.forEach { food ->
                val normalizedFood = food.lowercase()
                val score = calculateFuzzyScore(normalizedFood, normalizedQuery)
                if (score > 0) {
                    results.add(FoodSearchResult(food, category) to score)
                }
            }
        }
        
        // Sort by score (higher = better match) then alphabetically
        return results
            .sortedWith(compareByDescending<Pair<FoodSearchResult, Int>> { it.second }
                .thenBy { it.first.foodName })
            .map { it.first }
            .take(10) // Limit to top 10 results
    }
    
    private fun calculateFuzzyScore(text: String, query: String): Int {
        // Exact match gets highest score
        if (text == query) return 1000
        
        // Starts with query gets high score
        if (text.startsWith(query)) return 800
        
        // Contains query gets medium score
        if (text.contains(query)) return 600
        
        // Check for partial matches and character similarity
        var score = 0
        
        // Check if all query characters exist in text (in order)
        var textIndex = 0
        var queryIndex = 0
        var consecutiveMatches = 0
        
        while (textIndex < text.length && queryIndex < query.length) {
            if (text[textIndex] == query[queryIndex]) {
                queryIndex++
                consecutiveMatches++
                score += 10 + consecutiveMatches * 5 // Bonus for consecutive matches
            } else {
                consecutiveMatches = 0
            }
            textIndex++
        }
        
        // If all query characters were found, add bonus
        if (queryIndex == query.length) {
            score += 200
        }
        
        // Check for word boundary matches (e.g., "fries" matches "French Fries")
        val words = text.split(" ")
        for (word in words) {
            if (word.startsWith(query)) {
                score += 400
                break
            }
            if (word.contains(query)) {
                score += 200
                break
            }
        }
        
        // Minimum threshold to appear in results
        return if (score >= 100) score else 0
    }
}