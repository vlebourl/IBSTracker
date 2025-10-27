package com.tiarkaerell.ibstracker.ui.components.analysis

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.model.SymptomAnalysis
import com.tiarkaerell.ibstracker.data.model.TriggerProbability

@Composable
fun InsightText(
    insights: List<String>,
    modifier: Modifier = Modifier,
    title: String = "Insights",
    maxLines: Int = 3,
    showExpandButton: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                if (showExpandButton && insights.size > maxLines) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Show less" else "Show more",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val displayedInsights = if (isExpanded || !showExpandButton) {
                insights
            } else {
                insights.take(maxLines)
            }
            
            displayedInsights.forEachIndexed { index, insight ->
                InsightItem(
                    text = insight,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            
            if (!isExpanded && insights.size > maxLines && showExpandButton) {
                Text(
                    text = "and ${insights.size - maxLines} more insights...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SymptomInsightText(
    symptomAnalysis: SymptomAnalysis,
    modifier: Modifier = Modifier
) {
    val insights = generateSymptomInsights(symptomAnalysis)
    
    InsightText(
        insights = insights,
        modifier = modifier,
        title = "${symptomAnalysis.symptomType} Insights"
    )
}

@Composable
fun TriggerInsightText(
    trigger: TriggerProbability,
    modifier: Modifier = Modifier
) {
    val insights = generateTriggerInsights(trigger)
    
    InsightText(
        insights = insights,
        modifier = modifier,
        title = "${trigger.foodName} Analysis",
        maxLines = 2
    )
}

@Composable
fun GeneralInsightText(
    totalSymptoms: Int,
    totalCorrelations: Int,
    averageConfidence: Double,
    modifier: Modifier = Modifier
) {
    val insights = generateGeneralInsights(totalSymptoms, totalCorrelations, averageConfidence)
    
    InsightText(
        insights = insights,
        modifier = modifier,
        title = "Analysis Summary"
    )
}

@Composable
private fun InsightItem(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f))
                .align(Alignment.CenterVertically)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SmartInsightBox(
    insight: String,
    type: InsightType = InsightType.INFO,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type) {
        InsightType.INFO -> MaterialTheme.colorScheme.primaryContainer
        InsightType.WARNING -> MaterialTheme.colorScheme.errorContainer
        InsightType.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer
        InsightType.TIP -> MaterialTheme.colorScheme.secondaryContainer
    }
    
    val contentColor = when (type) {
        InsightType.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
        InsightType.WARNING -> MaterialTheme.colorScheme.onErrorContainer
        InsightType.SUCCESS -> MaterialTheme.colorScheme.onTertiaryContainer
        InsightType.TIP -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    
    val icon = when (type) {
        InsightType.INFO -> Icons.Default.Info
        InsightType.WARNING -> Icons.Default.Info
        InsightType.SUCCESS -> Icons.Default.Lightbulb
        InsightType.TIP -> Icons.Default.Lightbulb
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onActionClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = contentColor
                        )
                    ) {
                        Text(text = actionLabel)
                    }
                }
            }
        }
    }
}

enum class InsightType {
    INFO, WARNING, SUCCESS, TIP
}

private fun generateSymptomInsights(symptom: SymptomAnalysis): List<String> {
    val insights = mutableListOf<String>()
    
    insights.add("You've experienced ${symptom.totalOccurrences} episodes with average intensity ${String.format("%.1f", symptom.averageIntensity)}/10")
    
    when (symptom.severityLevel) {
        com.tiarkaerell.ibstracker.data.model.SeverityLevel.HIGH -> {
            insights.add("This symptom shows high severity patterns - consider consulting your healthcare provider")
        }
        com.tiarkaerell.ibstracker.data.model.SeverityLevel.MEDIUM -> {
            insights.add("Moderate severity levels suggest manageable symptoms with lifestyle adjustments")
        }
        com.tiarkaerell.ibstracker.data.model.SeverityLevel.LOW -> {
            insights.add("Generally mild symptoms indicate good symptom management")
        }
    }
    
    if (symptom.triggerProbabilities.isNotEmpty()) {
        val topTrigger = symptom.triggerProbabilities.first()
        insights.add("${topTrigger.foodName} shows the strongest correlation (${topTrigger.probabilityPercentage}%)")
        
        val highProbabilityTriggers = symptom.triggerProbabilities.filter { it.probability >= 0.7 }
        if (highProbabilityTriggers.size > 1) {
            insights.add("${highProbabilityTriggers.size} foods show strong trigger patterns")
        }
    }
    
    when (symptom.recommendationLevel) {
        com.tiarkaerell.ibstracker.data.model.RecommendationLevel.HIGH -> {
            insights.add("Strong patterns detected - consider avoiding identified triggers")
        }
        com.tiarkaerell.ibstracker.data.model.RecommendationLevel.MEDIUM -> {
            insights.add("Moderate patterns suggest monitoring these foods more closely")
        }
        com.tiarkaerell.ibstracker.data.model.RecommendationLevel.LOW_CONFIDENCE -> {
            insights.add("Limited data available - continue tracking for better insights")
        }
        com.tiarkaerell.ibstracker.data.model.RecommendationLevel.HIDE -> {
            insights.add("Insufficient data for reliable recommendations")
        }
    }
    
    return insights
}

private fun generateTriggerInsights(trigger: TriggerProbability): List<String> {
    val insights = mutableListOf<String>()
    
    when {
        trigger.probability >= 0.8 -> insights.add("Very strong trigger relationship detected")
        trigger.probability >= 0.6 -> insights.add("Strong trigger correlation identified")
        trigger.probability >= 0.4 -> insights.add("Moderate trigger potential observed")
        else -> insights.add("Weak correlation with symptoms")
    }
    
    insights.add("Based on ${trigger.occurrenceCount} correlation events")
    
    val avgHours = trigger.averageTimeLag.toHours()
    when {
        avgHours <= 1 -> insights.add("Symptoms typically appear within 1 hour")
        avgHours <= 2 -> insights.add("Symptoms usually occur within 2 hours")
        avgHours <= 4 -> insights.add("Delayed reaction (2-4 hours) observed")
        else -> insights.add("Late reaction (4+ hours) pattern detected")
    }
    
    when {
        trigger.confidence >= 0.8 -> insights.add("High confidence based on consistent patterns")
        trigger.confidence >= 0.5 -> insights.add("Moderate confidence - patterns emerging")
        else -> insights.add("Low confidence - more data needed for reliable patterns")
    }
    
    return insights
}

private fun generateGeneralInsights(
    totalSymptoms: Int,
    totalCorrelations: Int,
    averageConfidence: Double
): List<String> {
    val insights = mutableListOf<String>()
    
    insights.add("Analysis based on $totalSymptoms symptom episodes")
    insights.add("Found $totalCorrelations potential food-symptom correlations")
    
    when {
        averageConfidence >= 0.7 -> insights.add("High confidence patterns detected - reliable insights available")
        averageConfidence >= 0.5 -> insights.add("Moderate confidence levels - patterns are emerging")
        else -> insights.add("Limited confidence - continue tracking for better patterns")
    }
    
    if (totalCorrelations == 0) {
        insights.add("No clear food triggers identified yet - keep tracking your symptoms and meals")
    } else if (totalCorrelations < 3) {
        insights.add("Few correlations found - consider tracking for a longer period")
    } else {
        insights.add("Multiple food correlations detected - review high-probability triggers")
    }
    
    return insights
}