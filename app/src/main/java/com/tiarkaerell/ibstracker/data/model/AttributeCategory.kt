package com.tiarkaerell.ibstracker.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * AttributeCategory Enum - Smart Food Categorization System (v1.9.0)
 *
 * Grouping for IBS impact attributes (UI organization).
 * Used to organize the 11 IBS attributes into logical sections
 * in Add/Edit Food dialogs.
 */
enum class AttributeCategory(
    val displayName: String,
    val displayNameFr: String,
    val icon: ImageVector,
    val description: String
) {
    FODMAP(
        displayName = "FODMAP Level",
        displayNameFr = "Niveau FODMAP",
        icon = Icons.Default.Restaurant,
        description = "Fermentable carbohydrate content"
    ),
    GRAIN_BASED(
        displayName = "Grain-Based",
        displayNameFr = "Céréales",
        icon = Icons.Default.Grain,
        description = "Grain-related triggers"
    ),
    DAIRY_BASED(
        displayName = "Dairy-Based",
        displayNameFr = "Produits laitiers",
        icon = Icons.Default.Icecream,
        description = "Dairy-related triggers"
    ),
    STIMULANTS(
        displayName = "Stimulants",
        displayNameFr = "Stimulants",
        icon = Icons.Default.LocalCafe,
        description = "Caffeine and alcohol"
    ),
    IRRITANTS(
        displayName = "Irritants",
        displayNameFr = "Irritants",
        icon = Icons.Default.LocalFireDepartment,
        description = "Spicy and hot foods"
    ),
    MACRONUTRIENTS(
        displayName = "Macronutrients",
        displayNameFr = "Macronutriments",
        icon = Icons.Default.FitnessCenter,
        description = "Fat, fiber, protein content"
    ),
    CHEMICAL(
        displayName = "Chemical Properties",
        displayNameFr = "Propriétés chimiques",
        icon = Icons.Default.Science,
        description = "Acidity and pH"
    ),
    ADDITIVES(
        displayName = "Additives",
        displayNameFr = "Additifs",
        icon = Icons.Default.Cancel,
        description = "Artificial ingredients"
    );

    companion object {
        /**
         * Get display name in current locale
         * @param isFrench true for French, false for English
         */
        fun getDisplayName(category: AttributeCategory, isFrench: Boolean = false): String {
            return if (isFrench) category.displayNameFr else category.displayName
        }

        /**
         * Get all categories in declaration order
         */
        fun getAllCategories(): List<AttributeCategory> {
            return values().toList()
        }
    }
}
