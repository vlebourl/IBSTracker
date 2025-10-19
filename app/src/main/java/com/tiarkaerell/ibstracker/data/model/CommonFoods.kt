package com.tiarkaerell.ibstracker.data.model

object CommonFoods {
    val foodsByCategory = mapOf(
        FoodCategory.DAIRY to listOf(
            "Milk",
            "Cheese",
            "Yogurt",
            "Ice Cream",
            "Butter",
            "Cream",
            "Cottage Cheese",
            "Sour Cream"
        ),
        FoodCategory.GLUTEN to listOf(
            "Bread",
            "Pasta",
            "Pizza",
            "Cereal",
            "Crackers",
            "Beer",
            "Cookies",
            "Cake"
        ),
        FoodCategory.HIGH_FODMAP to listOf(
            "Garlic",
            "Onions",
            "Apples",
            "Beans",
            "Wheat",
            "Honey",
            "Mushrooms",
            "Cauliflower"
        ),
        FoodCategory.SPICY to listOf(
            "Hot Sauce",
            "Chili",
            "Curry",
            "Jalapeños",
            "Salsa",
            "Wasabi",
            "Hot Wings",
            "Pepper Flakes"
        ),
        FoodCategory.PROCESSED_FATTY to listOf(
            "French Fries",
            "Fried Chicken", 
            "Chips",
            "Bacon",
            "Hot Dogs",
            "Chocolate",
            "Donuts",
            "Fast Food Burger"
        ),
        FoodCategory.BEVERAGES to listOf(
            "Coffee",
            "Tea", 
            "Beer",
            "Wine",
            "Soda",
            "Energy Drink",
            "Juice",
            "Sparkling Water"
        ),
        FoodCategory.FRUITS to listOf(
            "Apple",
            "Banana",
            "Orange",
            "Strawberries",
            "Grapes",
            "Watermelon",
            "Blueberries",
            "Pineapple"
        ),
        FoodCategory.VEGETABLES to listOf(
            "Salad",
            "Carrots",
            "Broccoli",
            "Spinach",
            "Tomatoes",
            "Cucumber",
            "Peppers",
            "Zucchini"
        ),
        FoodCategory.OTHER to listOf(
            "Rice",
            "Chicken",
            "Fish",
            "Eggs",
            "Nuts",
            "Oatmeal",
            "Soup",
            "Sandwich"
        )
    )
    
    fun getCommonFoods(category: FoodCategory): List<String> {
        return foodsByCategory[category] ?: emptyList()
    }
    
    data class FoodSearchResult(
        val foodName: String,
        val category: FoodCategory
    )
    
    fun searchFoods(query: String): List<FoodSearchResult> {
        if (query.isBlank()) return emptyList()
        
        val results = mutableListOf<Pair<FoodSearchResult, Int>>()
        val normalizedQuery = query.lowercase().trim()
        
        foodsByCategory.forEach { (category, foods) ->
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