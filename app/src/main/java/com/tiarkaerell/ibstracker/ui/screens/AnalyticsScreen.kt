package com.tiarkaerell.ibstracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.model.InsightSummary
import com.tiarkaerell.ibstracker.data.model.TriggerAnalysis
import com.tiarkaerell.ibstracker.data.model.CategoryInsight
import com.tiarkaerell.ibstracker.ui.viewmodel.AnalyticsViewModel
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(analyticsViewModel: AnalyticsViewModel) {
    val insights by analyticsViewModel.insights.collectAsState()
    val isLoading by analyticsViewModel.isLoading.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Insights & Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { analyticsViewModel.refreshInsights() },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh insights"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (insights != null) {
            InsightsContent(insights = insights!!)
        } else {
            EmptyInsightsState()
        }
    }
}

@Composable
private fun InsightsContent(insights: InsightSummary) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall stats
        item {
            OverallStatsCard(insights)
        }
        
        // Potential triggers
        if (insights.topTriggers.isNotEmpty()) {
            item {
                TriggerAnalysisCard(triggers = insights.topTriggers)
            }
        }
        
        // Safe categories
        if (insights.safestCategories.isNotEmpty()) {
            item {
                SafeCategoriesCard(categories = insights.safestCategories)
            }
        }
        
        // Weekly patterns
        if (insights.weeklyPatterns.any { it.symptomCount > 0 }) {
            item {
                WeeklyPatternsCard(patterns = insights.weeklyPatterns)
            }
        }
        
        // Trend analysis
        item {
            TrendAnalysisCard(
                improvementTrend = insights.improvementTrend,
                daysSinceLastSymptom = insights.daysSinceLastSymptom
            )
        }
    }
}

@Composable
private fun OverallStatsCard(insights: InsightSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your Tracking Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = insights.totalFoodEntries.toString(),
                    label = "Foods Logged",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatItem(
                    value = insights.totalSymptoms.toString(),
                    label = "Symptoms",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatItem(
                    value = String.format("%.1f", insights.averageSymptomIntensity),
                    label = "Avg Intensity",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun TriggerAnalysisCard(triggers: List<TriggerAnalysis>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Potential Triggers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            triggers.forEach { trigger ->
                TriggerItem(trigger = trigger)
                if (trigger != triggers.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            if (triggers.isEmpty()) {
                Text(
                    text = "No clear triggers identified yet. Keep logging to see patterns!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TriggerItem(trigger: TriggerAnalysis) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(trigger.category.color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = trigger.category.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${trigger.symptomsTriggered}/${trigger.occurrences} times",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Text(
            text = "${(trigger.triggerScore * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun SafeCategoriesCard(categories: List<CategoryInsight>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Safest Food Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            categories.take(3).forEach { category ->
                SafeCategoryItem(category = category)
                if (category != categories.take(3).last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SafeCategoryItem(category: CategoryInsight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(category.category.color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = category.category.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${category.totalEntries} entries logged",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Text(
            text = "${(category.safetyScore * 100).toInt()}% safe",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
    }
}

@Composable
private fun WeeklyPatternsCard(patterns: List<com.tiarkaerell.ibstracker.data.model.WeeklyPattern>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Weekly Symptom Patterns",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val maxSymptoms = patterns.maxOfOrNull { it.symptomCount } ?: 1
            
            patterns.forEachIndexed { index, pattern ->
                val barWidth = if (maxSymptoms > 0) {
                    (pattern.symptomCount.toFloat() / maxSymptoms.toFloat()).coerceIn(0f, 1f)
                } else 0f
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dayNames[index],
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(40.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .weight(1f)
                            .background(
                                Color.Gray.copy(alpha = 0.2f),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(barWidth)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(10.dp)
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pattern.symptomCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(30.dp)
                    )
                }
                
                if (pattern != patterns.last()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun TrendAnalysisCard(improvementTrend: Float, daysSinceLastSymptom: Int) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Health Trends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Days since last symptom
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Days since last symptom: ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (daysSinceLastSymptom == Int.MAX_VALUE) "No symptoms yet" else "$daysSinceLastSymptom",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (daysSinceLastSymptom > 7) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Improvement trend
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val trendIcon = when {
                    improvementTrend > 0.1f -> Icons.Default.TrendingUp
                    improvementTrend < -0.1f -> Icons.Default.TrendingDown
                    else -> null
                }
                
                val trendText = when {
                    improvementTrend > 0.1f -> "Symptoms improving"
                    improvementTrend < -0.1f -> "Symptoms worsening"
                    else -> "Symptoms stable"
                }
                
                val trendColor = when {
                    improvementTrend > 0.1f -> Color(0xFF2E7D32)
                    improvementTrend < -0.1f -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                if (trendIcon != null) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = trendText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = trendColor
                )
            }
        }
    }
}

@Composable
private fun EmptyInsightsState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Not enough data yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Log more foods and symptoms to see insights and patterns",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}