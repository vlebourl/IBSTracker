package com.tiarkaerell.ibstracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.model.FoodUsageStats

/**
 * QuickAddCard - Material Design 3 card for quick-add food shortcuts
 *
 * Displays:
 * - Food name (e.g., "Coffee")
 * - Category emoji icon (e.g., ☕)
 * - Usage count badge (e.g., "x12")
 *
 * Features:
 * - Compact design for horizontal scrolling
 * - Ripple effect on tap
 * - Usage badge positioned on icon
 * - Sorted by usage count DESC, then alphabetically ASC
 *
 * @param foodStats The food usage statistics to display
 * @param onClick Callback when card is tapped
 * @param modifier Optional modifier for the card
 */
@Composable
fun QuickAddCard(
    foodStats: FoodUsageStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(96.dp)
            .height(108.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Food name (top)
            Text(
                text = foodStats.foodName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // Category icon with usage badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = foodStats.category.icon,
                    contentDescription = foodStats.category.displayName,
                    tint = foodStats.category.colorLight,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Usage count badge (bottom)
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "x${foodStats.usageCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
