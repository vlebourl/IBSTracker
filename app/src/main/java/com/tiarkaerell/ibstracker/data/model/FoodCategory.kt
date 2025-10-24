package com.tiarkaerell.ibstracker.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.tiarkaerell.ibstracker.ui.icons.CustomIcons
import com.tiarkaerell.ibstracker.ui.theme.*

/**
 * FoodCategory Enum - Smart Food Categorization System (v1.9.0)
 *
 * Represents 12 actual food type categories (not IBS-trigger based).
 * Each category has Material Design 3 colors, icons, and bilingual names.
 * Used for organizing foods by what they ARE, not by IBS impact.
 *
 * Migration: v8→v9 remaps old 9-category system to new 12-category system.
 */
enum class FoodCategory(
    val displayName: String,
    val displayNameFr: String,
    val colorLight: Color,
    val colorDark: Color,
    val icon: ImageVector,
    val sortOrder: Int,
    val description: String = ""
) {
    GRAINS(
        displayName = "Grains",
        displayNameFr = "Céréales",
        colorLight = md_theme_light_tertiary,
        colorDark = md_theme_dark_tertiary,
        icon = CustomIcons.Barley,  // MDI Barley icon for grains/wheat
        sortOrder = 1
    ),
    PROTEINS(
        displayName = "Proteins",
        displayNameFr = "Protéines",
        colorLight = md_theme_light_primary,
        colorDark = md_theme_dark_primary,
        icon = CustomIcons.FoodDrumstick,  // MDI Drumstick icon for proteins
        sortOrder = 2
    ),
    DAIRY(
        displayName = "Dairy",
        displayNameFr = "Produits laitiers",
        colorLight = md_theme_light_secondary,
        colorDark = md_theme_dark_secondary,
        icon = CustomIcons.Cow,  // MDI Cow icon for dairy
        sortOrder = 3
    ),
    VEGETABLES(
        displayName = "Vegetables",
        displayNameFr = "Légumes",
        colorLight = CategoryGreenLight,
        colorDark = CategoryGreenDark,
        icon = Icons.Filled.Grass,  // Grass/plant icon for vegetables
        sortOrder = 4
    ),
    FRUITS(
        displayName = "Fruits",
        displayNameFr = "Fruits",
        colorLight = CategoryOrangeLight,
        colorDark = CategoryOrangeDark,
        icon = CustomIcons.FoodApple,  // MDI Apple icon for fruits
        sortOrder = 5
    ),
    LEGUMES(
        displayName = "Legumes",
        displayNameFr = "Légumineuses",
        colorLight = CategoryBrownLight,
        colorDark = CategoryBrownDark,
        icon = CustomIcons.Sprout,  // MDI Sprout icon for legumes
        sortOrder = 6
    ),
    NUTS_SEEDS(
        displayName = "Nuts & Seeds",
        displayNameFr = "Noix et graines",
        colorLight = CategoryAmberLight,
        colorDark = CategoryAmberDark,
        icon = CustomIcons.Peanut,  // MDI Peanut icon for nuts & seeds
        sortOrder = 7
    ),
    BEVERAGES(
        displayName = "Beverages",
        displayNameFr = "Boissons",
        colorLight = CategoryBlueLight,
        colorDark = CategoryBlueDark,
        icon = CustomIcons.Coffee,  // MDI Coffee icon for beverages
        sortOrder = 8
    ),
    FATS_OILS(
        displayName = "Fats & Oils",
        displayNameFr = "Matières grasses",
        colorLight = CategoryYellowLight,
        colorDark = CategoryYellowDark,
        icon = CustomIcons.Water,  // MDI Water droplet icon
        sortOrder = 9
    ),
    SWEETS(
        displayName = "Sweets",
        displayNameFr = "Sucreries",
        colorLight = CategoryPinkLight,
        colorDark = CategoryPinkDark,
        icon = CustomIcons.IcePop,  // MDI Ice-Pop icon for sweets
        sortOrder = 10
    ),
    PROCESSED(
        displayName = "Processed Foods",
        displayNameFr = "Aliments transformés",
        colorLight = CategoryRedLight,
        colorDark = CategoryRedDark,
        icon = CustomIcons.Hamburger,  // MDI Hamburger icon for processed
        sortOrder = 11
    ),
    OTHER(
        displayName = "Other",
        displayNameFr = "Autre",
        colorLight = CategoryNeutralLight,
        colorDark = CategoryNeutralDark,
        icon = Icons.Filled.MoreHoriz,  // Dots-horizontal (MoreHoriz is same)
        sortOrder = 12
    );

    companion object {
        /**
         * Get display name in current locale
         * @param isFrench true for French, false for English
         */
        fun getDisplayName(category: FoodCategory, isFrench: Boolean = false): String {
            return if (isFrench) category.displayNameFr else category.displayName
        }

        /**
         * Get all categories in sort order
         */
        fun getAllCategories(): List<FoodCategory> {
            return values().sortedBy { it.sortOrder }
        }

        /**
         * Find category by display name (supports both EN and FR)
         */
        fun findByDisplayName(name: String): FoodCategory? {
            return values().find {
                it.displayName.equals(name, ignoreCase = true) ||
                it.displayNameFr.equals(name, ignoreCase = true)
            }
        }

        /**
         * Get color for category based on theme
         * @param isLightTheme true for light theme, false for dark theme
         */
        fun getColor(category: FoodCategory, isLightTheme: Boolean): Color {
            return if (isLightTheme) category.colorLight else category.colorDark
        }
    }
}
