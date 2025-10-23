package com.tiarkaerell.ibstracker.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.model.FoodUsageStats

/**
 * QuickAddSection - Horizontal scrollable section displaying top 6 most-used foods
 *
 * Features:
 * - Title with scroll indicator
 * - Horizontal scrollable row of QuickAddCard
 * - Displays top 6 foods sorted by usage count DESC, then alphabetically ASC
 * - Smooth animations when usage counts change
 * - Graceful empty state handling
 *
 * Layout:
 * ```
 * Quick Add (Top 6 by Usage) →→→
 * ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
 * │Coffee│ │Bread │ │Milk  │ │Apple │
 * │☕ x12│ │🌾 x8 │ │🥛 x6 │ │🍎 x5 │
 * └──────┘ └──────┘ └──────┘ └──────┘
 * ```
 *
 * @param topUsedFoods List of top 6 food usage statistics (sorted)
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
        // Section header with scroll indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Add (Top 6 by Usage)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Scroll indicator
            if (topUsedFoods.size > 3) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Scroll for more",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Horizontal scrollable row of quick-add cards
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = topUsedFoods.take(6), // Limit to top 6
                key = { it.foodName } // Use foodName as key for stable animations
            ) { foodStats ->
                QuickAddCard(
                    foodStats = foodStats,
                    onClick = { onFoodClick(foodStats) }
                )
            }
        }

        // Divider after section
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
