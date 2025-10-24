package com.tiarkaerell.ibstracker.data.model

import android.content.Context
import com.tiarkaerell.ibstracker.R

/**
 * DEPRECATED: This helper is obsolete in Smart Food Categorization v1.9.0.
 * Use FoodCategory enum properties directly (displayName, displayNameFr, description).
 */
object FoodCategoryHelper {
    @Deprecated(
        message = "Use category.displayName or category.displayNameFr instead",
        replaceWith = ReplaceWith("category.displayName")
    )
    fun getDisplayName(context: Context, category: FoodCategory): String {
        // Return built-in display name from enum (English by default)
        return category.displayName
    }

    @Deprecated(
        message = "Use category.description instead",
        replaceWith = ReplaceWith("category.description")
    )
    fun getDescription(context: Context, category: FoodCategory): String {
        // Return built-in description from enum
        return category.description
    }
}