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
        FoodCategory.FRIED_FATTY to listOf(
            "French Fries",
            "Fried Chicken",
            "Chips",
            "Bacon",
            "Burger",
            "Donuts",
            "Onion Rings",
            "Pizza"
        ),
        FoodCategory.CAFFEINE to listOf(
            "Coffee",
            "Espresso",
            "Tea",
            "Energy Drink",
            "Soda",
            "Chocolate",
            "Green Tea",
            "Iced Coffee"
        ),
        FoodCategory.ALCOHOL to listOf(
            "Beer",
            "Wine",
            "Whiskey",
            "Vodka",
            "Cocktail",
            "Champagne",
            "Rum",
            "Gin"
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
        FoodCategory.PROCESSED to listOf(
            "Chips",
            "Cookies",
            "Candy",
            "Instant Noodles",
            "Frozen Pizza",
            "Hot Dogs",
            "Microwave Meal",
            "Crackers"
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
}