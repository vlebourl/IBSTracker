package com.tiarkaerell.ibstracker.utils

import com.tiarkaerell.ibstracker.data.model.IBSTriggerCategory

object FoodGroupMapper {
    
    private val categoryKeywords = mapOf(
        IBSTriggerCategory.DAIRY to listOf(
            "milk", "cheese", "yogurt", "yoghurt", "dairy", "cream", "butter", 
            "ice cream", "mozzarella", "cheddar", "parmesan", "cottage cheese", 
            "sour cream", "whey", "lactose", "kefir"
        ),
        IBSTriggerCategory.GLUTEN to listOf(
            "wheat", "bread", "pasta", "gluten", "flour", "cereal", "crackers",
            "bagel", "muffin", "cookie", "cake", "pizza", "sandwich", "toast",
            "noodles", "barley", "rye", "oats", "biscuit"
        ),
        IBSTriggerCategory.FODMAP_HIGH to listOf(
            "onion", "garlic", "apple", "pear", "watermelon", "mango", "beans",
            "lentils", "chickpeas", "kidney beans", "black beans", "cashews",
            "pistachios", "artichoke", "asparagus", "brussels sprouts", "cabbage",
            "cauliflower", "mushrooms", "honey", "agave"
        ),
        IBSTriggerCategory.CAFFEINE to listOf(
            "coffee", "tea", "espresso", "latte", "cappuccino", "caffeine",
            "energy drink", "cola", "soda", "chocolate", "cocoa", "green tea",
            "black tea", "matcha", "guarana"
        ),
        IBSTriggerCategory.ALCOHOL to listOf(
            "beer", "wine", "alcohol", "vodka", "whiskey", "rum", "gin",
            "cocktail", "liquor", "champagne", "prosecco", "sake", "brandy",
            "tequila", "bourbon", "scotch"
        ),
        IBSTriggerCategory.SPICY to listOf(
            "spicy", "hot", "pepper", "chili", "jalapeno", "habanero", "cayenne",
            "paprika", "curry", "salsa", "hot sauce", "wasabi", "horseradish",
            "ginger", "mustard", "tabasco"
        ),
        IBSTriggerCategory.FATTY to listOf(
            "fried", "greasy", "fatty", "oil", "butter", "margarine", "lard",
            "bacon", "sausage", "fried chicken", "french fries", "chips",
            "nuts", "avocado", "fatty fish", "salmon", "tuna"
        ),
        IBSTriggerCategory.ARTIFICIAL_SWEETENERS to listOf(
            "diet", "sugar-free", "aspartame", "sucralose", "stevia", "xylitol",
            "sorbitol", "mannitol", "artificial sweetener", "splenda", "equal",
            "sweet'n low", "diet soda", "sugar substitute"
        ),
        IBSTriggerCategory.CITRUS to listOf(
            "orange", "lemon", "lime", "grapefruit", "citrus", "tangerine",
            "mandarin", "pomelo", "citric acid", "orange juice", "lemonade",
            "lime juice", "zest"
        ),
        IBSTriggerCategory.BEANS_LEGUMES to listOf(
            "beans", "lentils", "chickpeas", "peas", "legumes", "kidney beans",
            "black beans", "pinto beans", "navy beans", "lima beans", "soybeans",
            "tofu", "tempeh", "hummus", "split peas", "black-eyed peas"
        )
    )
    
    fun categorizeFood(foodName: String): IBSTriggerCategory {
        val lowercaseName = foodName.lowercase().trim()
        
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { keyword -> lowercaseName.contains(keyword) }) {
                return category
            }
        }
        
        return IBSTriggerCategory.OTHER
    }
    
    fun findSimilarFoods(foodName: String, allFoodNames: List<String>): List<String> {
        val normalized = normalizeFood(foodName)
        
        return allFoodNames.filter { otherFood ->
            otherFood != foodName && calculateSimilarity(normalized, normalizeFood(otherFood)) > 0.7
        }.take(5)
    }
    
    fun normalizeFood(foodName: String): String {
        return foodName.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    private fun calculateSimilarity(food1: String, food2: String): Double {
        val words1 = food1.split(" ").toSet()
        val words2 = food2.split(" ").toSet()
        
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }
    
    fun groupSimilarFoods(foodNames: List<String>): Map<String, List<String>> {
        val groups = mutableMapOf<String, MutableList<String>>()
        val processed = mutableSetOf<String>()
        
        for (foodName in foodNames) {
            if (foodName in processed) continue
            
            val normalizedName = normalizeFood(foodName)
            val similarFoods = findSimilarFoods(foodName, foodNames)
            
            val groupKey = if (similarFoods.isNotEmpty()) {
                val allInGroup = listOf(foodName) + similarFoods
                allInGroup.minByOrNull { it.length } ?: foodName
            } else {
                foodName
            }
            
            groups.getOrPut(groupKey) { mutableListOf() }.add(foodName)
            processed.add(foodName)
            processed.addAll(similarFoods)
        }
        
        return groups
    }
    
    fun getCategoryDisplayName(category: IBSTriggerCategory): String {
        return category.displayName
    }
    
    fun getCategoryBaselineProbability(category: IBSTriggerCategory): Double {
        return category.baselineProbability
    }
}