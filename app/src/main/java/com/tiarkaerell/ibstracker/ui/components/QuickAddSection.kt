package com.tiarkaerell.ibstracker.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.model.FoodUsageStats

/**
 * QuickAddSection - Full-width single row displaying top 4 most-used foods
 *
 * Features:
 * - 1x4 single row layout (full width, no scrolling)
 * - Displays top 4 foods sorted by usage count DESC, then alphabetically ASC
 * - Smooth animations when usage counts change
 * - Graceful empty state handling
 *
 * Layout:
 * ```
 * Quick Add (Top 4 by Usage)
 * ┌──────┬──────┬──────┬──────┐
 * │Coffee│Bread │Milk  │Apple │
 * │☕ x12│🌾 x8 │🥛 x6 │🍎 x5 │
 * └──────┴──────┴──────┴──────┘
 * ```
 *
 * @param topUsedFoods List of top 4 food usage statistics (sorted)
 * @param onFoodClick Callback when a food card is clicked
 * @param modifier Optional modifier for the section
 */
@Composable
fun QuickAddSection(
    topUsedFoods: List<FoodUsageStats>,
    onFoodClick: (FoodUsageStats) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show section if there are foods to display
    if (topUsedFoods.isEmpty()) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize() // Smooth animation when content changes
    ) {
        // Section header
        Text(
            text = "Quick Add",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Single row with 4 items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topUsedFoods.take(4).forEach { foodStats ->
                QuickAddCard(
                    foodStats = foodStats,
                    onClick = { onFoodClick(foodStats) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
