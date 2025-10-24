package com.tiarkaerell.ibstracker.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.tiarkaerell.ibstracker.ui.theme.*

/**
 * IBSImpact Enum - Smart Food Categorization System (v1.9.0)
 *
 * Hidden IBS impact attributes for analytical purposes (11 attributes).
 * These attributes describe HOW a food might affect IBS symptoms,
 * separate from the actual food category (what the food IS).
 *
 * Grouped by AttributeCategory for UI organization in Add/Edit Food dialogs.
 *
 * FODMAP Level: Exactly one FODMAP_* value should be present (default: FODMAP_LOW)
 * Other Attributes: Can be present or absent (e.g., a food may have GLUTEN + LACTOSE)
 */
enum class IBSImpact(
    val displayName: String,
    val displayNameFr: String,
    val category: AttributeCategory,
    val description: String,
    val icon: ImageVector,
    val colorLight: Color,
    val colorDark: Color
) {
    // ==================== FODMAP Level (mutually exclusive) ====================
    FODMAP_HIGH(
        displayName = "High FODMAP",
        displayNameFr = "FODMAP élevé",
        category = AttributeCategory.FODMAP,
        description = "High fermentable carbs (triggers gas/bloating)",
        icon = Icons.Default.Warning,
        colorLight = IBSErrorLight,
        colorDark = IBSErrorDark
    ),
    FODMAP_MODERATE(
        displayName = "Moderate FODMAP",
        displayNameFr = "FODMAP modéré",
        category = AttributeCategory.FODMAP,
        description = "Moderate fermentable carbs (portion-dependent)",
        icon = Icons.Default.Info,
        colorLight = IBSWarningLight,
        colorDark = IBSWarningDark
    ),
    FODMAP_LOW(
        displayName = "Low FODMAP",
        displayNameFr = "FODMAP faible",
        category = AttributeCategory.FODMAP,
        description = "Low fermentable carbs (generally safe)",
        icon = Icons.Default.CheckCircle,
        colorLight = IBSSuccessLight,
        colorDark = IBSSuccessDark
    ),

    // ==================== Grain-Based ====================
    GLUTEN(
        displayName = "Contains Gluten",
        displayNameFr = "Contient du gluten",
        category = AttributeCategory.GRAIN_BASED,
        description = "Wheat, barley, rye proteins",
        icon = Icons.Default.Grain,
        colorLight = IBSErrorLight,
        colorDark = IBSErrorDark
    ),

    // ==================== Dairy-Based ====================
    LACTOSE(
        displayName = "Contains Lactose",
        displayNameFr = "Contient du lactose",
        category = AttributeCategory.DAIRY_BASED,
        description = "Milk sugar (triggers lactose intolerance)",
        icon = Icons.Default.Icecream,
        colorLight = IBSErrorLight,
        colorDark = IBSErrorDark
    ),

    // ==================== Stimulants ====================
    CAFFEINE(
        displayName = "Contains Caffeine",
        displayNameFr = "Contient de la caféine",
        category = AttributeCategory.STIMULANTS,
        description = "Stimulant affecting gut motility",
        icon = Icons.Default.LocalCafe,
        colorLight = IBSWarningLight,
        colorDark = IBSWarningDark
    ),
    ALCOHOL(
        displayName = "Contains Alcohol",
        displayNameFr = "Contient de l'alcool",
        category = AttributeCategory.STIMULANTS,
        description = "Irritates gut lining, affects motility",
        icon = Icons.Default.LocalBar,
        colorLight = IBSErrorLight,
        colorDark = IBSErrorDark
    ),

    // ==================== Irritants ====================
    SPICY(
        displayName = "Spicy/Hot",
        displayNameFr = "Épicé/Piquant",
        category = AttributeCategory.IRRITANTS,
        description = "Capsaicin triggers pain receptors",
        icon = Icons.Default.LocalFireDepartment,
        colorLight = IBSWarningLight,
        colorDark = IBSWarningDark
    ),

    // ==================== Macronutrients ====================
    FATTY(
        displayName = "High Fat",
        displayNameFr = "Riche en graisses",
        category = AttributeCategory.MACRONUTRIENTS,
        description = "High fat content (slows digestion, triggers gallbladder)",
        icon = Icons.Default.WaterDrop,
        colorLight = IBSWarningLight,
        colorDark = IBSWarningDark
    ),

    // ==================== Chemical ====================
    ACIDIC(
        displayName = "Acidic",
        displayNameFr = "Acide",
        category = AttributeCategory.CHEMICAL,
        description = "Low pH (irritates gut lining)",
        icon = Icons.Default.Science,
        colorLight = IBSWarningLight,
        colorDark = IBSWarningDark
    ),

    // ==================== Additives ====================
    ARTIFICIAL_SWEETENERS(
        displayName = "Artificial Sweeteners",
        displayNameFr = "Édulcorants artificiels",
        category = AttributeCategory.ADDITIVES,
        description = "Sugar alcohols, aspartame (osmotic effect)",
        icon = Icons.Default.Cancel,
        colorLight = IBSErrorLight,
        colorDark = IBSErrorDark
    );

    companion object {
        /**
         * Get display name in current locale
         * @param isFrench true for French, false for English
         */
        fun getDisplayName(impact: IBSImpact, isFrench: Boolean = false): String {
            return if (isFrench) impact.displayNameFr else impact.displayName
        }

        /**
         * Get all FODMAP level options (mutually exclusive)
         */
        fun getFodmapLevels(): List<IBSImpact> {
            return listOf(FODMAP_HIGH, FODMAP_MODERATE, FODMAP_LOW)
        }

        /**
         * Get all non-FODMAP attributes (can be combined)
         */
        fun getNonFodmapAttributes(): List<IBSImpact> {
            return values().filter { it.category != AttributeCategory.FODMAP }
        }

        /**
         * Get attributes grouped by category (for UI organization)
         */
        fun getGroupedByCategory(): Map<AttributeCategory, List<IBSImpact>> {
            return values().groupBy { it.category }
        }

        /**
         * Get color for impact based on theme
         * @param isLightTheme true for light theme, false for dark theme
         */
        fun getColor(impact: IBSImpact, isLightTheme: Boolean): Color {
            return if (isLightTheme) impact.colorLight else impact.colorDark
        }

        /**
         * Extract FODMAP level from a list of impacts
         * @return FODMAP level if present, otherwise FODMAP_LOW (default)
         */
        fun extractFodmapLevel(impacts: List<IBSImpact>): IBSImpact {
            return impacts.firstOrNull { it in getFodmapLevels() } ?: FODMAP_LOW
        }

        /**
         * Validate impact list has exactly one FODMAP level
         * @return true if valid, false if 0 or 2+ FODMAP levels
         */
        fun validateFodmapLevel(impacts: List<IBSImpact>): Boolean {
            val fodmapCount = impacts.count { it in getFodmapLevels() }
            return fodmapCount == 1
        }
    }
}
