package com.tiarkaerell.ibstracker.data.model

import android.content.Context
import com.tiarkaerell.ibstracker.R

object FoodCategoryHelper {
    fun getDisplayName(context: Context, category: FoodCategory): String {
        return when (category) {
            FoodCategory.DAIRY -> context.getString(R.string.food_category_dairy)
            FoodCategory.GLUTEN -> context.getString(R.string.food_category_gluten)
            FoodCategory.HIGH_FODMAP -> context.getString(R.string.food_category_high_fodmap)
            FoodCategory.SPICY -> context.getString(R.string.food_category_spicy)
            FoodCategory.PROCESSED_FATTY -> context.getString(R.string.food_category_processed_fatty)
            FoodCategory.BEVERAGES -> context.getString(R.string.food_category_beverages)
            FoodCategory.FRUITS -> context.getString(R.string.food_category_fruits)
            FoodCategory.VEGETABLES -> context.getString(R.string.food_category_vegetables)
            FoodCategory.OTHER -> context.getString(R.string.food_category_other)
        }
    }
    
    fun getDescription(context: Context, category: FoodCategory): String {
        return when (category) {
            FoodCategory.DAIRY -> context.getString(R.string.food_category_dairy_desc)
            FoodCategory.GLUTEN -> context.getString(R.string.food_category_gluten_desc)
            FoodCategory.HIGH_FODMAP -> context.getString(R.string.food_category_high_fodmap_desc)
            FoodCategory.SPICY -> context.getString(R.string.food_category_spicy_desc)
            FoodCategory.PROCESSED_FATTY -> context.getString(R.string.food_category_processed_fatty_desc)
            FoodCategory.BEVERAGES -> context.getString(R.string.food_category_beverages_desc)
            FoodCategory.FRUITS -> context.getString(R.string.food_category_fruits_desc)
            FoodCategory.VEGETABLES -> context.getString(R.string.food_category_vegetables_desc)
            FoodCategory.OTHER -> context.getString(R.string.food_category_other_desc)
        }
    }
}