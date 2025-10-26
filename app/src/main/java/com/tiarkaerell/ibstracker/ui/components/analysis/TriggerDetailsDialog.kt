package com.tiarkaerell.ibstracker.ui.components.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tiarkaerell.ibstracker.data.model.TriggerProbability
import java.time.format.DateTimeFormatter

@Composable
fun TriggerDetailsDialog(
    trigger: TriggerProbability,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${trigger.foodName} Analysis",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Probability overview
                    ProbabilityOverviewCard(trigger)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Detailed metrics
                    DetailedMetricsCard(trigger)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Correlation evidence
                    CorrelationEvidenceCard(trigger)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Insights and recommendations
                    InsightsCard(trigger)
                }
            }
        }
    }
}

@Composable
private fun ProbabilityOverviewCard(trigger: TriggerProbability) {
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
                text = "Trigger Probability",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${trigger.probabilityPercentage}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "Confidence: ${(trigger.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DetailedMetricsCard(trigger: TriggerProbability) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Detailed Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MetricRow("Occurrences", "${trigger.occurrenceCount}")
            MetricRow("Correlation Score", "${(trigger.correlationScore * 100).toInt()}%")
            MetricRow("Temporal Score", "${(trigger.temporalScore * 100).toInt()}%")
            MetricRow("Baseline Score", "${(trigger.baselineScore * 100).toInt()}%")
            MetricRow("Frequency Score", "${(trigger.frequencyScore * 100).toInt()}%")
            MetricRow("Average Time Lag", formatDuration(trigger.averageTimeLag))
            MetricRow("Intensity Multiplier", "%.1fx".format(trigger.intensityMultiplier))
            MetricRow("Category", trigger.ibsTriggerCategory.displayName)
        }
    }
}

@Composable
private fun CorrelationEvidenceCard(trigger: TriggerProbability) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Evidence Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Based on ${trigger.supportingEvidence.size} correlation events",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (trigger.supportingEvidence.isNotEmpty()) {
                val lastEvent = trigger.supportingEvidence.maxByOrNull { it.symptomTimestamp }
                lastEvent?.let { evidence ->
                    Text(
                        text = "Last correlation: ${formatTimestamp(evidence.symptomTimestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Text(
                        text = "Symptom intensity: ${evidence.symptomIntensity}/10",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightsCard(trigger: TriggerProbability) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val insights = generateInsights(trigger)
            insights.forEach { insight ->
                Text(
                    text = "• $insight",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDuration(duration: java.time.Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}

private fun formatTimestamp(instant: java.time.Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
    return instant.atZone(java.time.ZoneId.systemDefault()).format(formatter)
}

private fun generateInsights(trigger: TriggerProbability): List<String> {
    val insights = mutableListOf<String>()
    
    when {
        trigger.probability >= 0.8 -> insights.add("Very high correlation suggests strong trigger relationship")
        trigger.probability >= 0.6 -> insights.add("High correlation indicates likely trigger")
        trigger.probability >= 0.4 -> insights.add("Moderate correlation suggests possible trigger")
        else -> insights.add("Low correlation indicates weak trigger relationship")
    }
    
    when {
        trigger.confidence >= 0.8 -> insights.add("High confidence based on sufficient data")
        trigger.confidence >= 0.5 -> insights.add("Moderate confidence - more data would help")
        else -> insights.add("Low confidence - limited data available")
    }
    
    if (trigger.averageTimeLag.toHours() <= 2) {
        insights.add("Symptoms typically occur within 2 hours")
    } else if (trigger.averageTimeLag.toHours() <= 4) {
        insights.add("Symptoms typically occur within 2-4 hours")
    } else {
        insights.add("Symptoms occur with delayed timing (4+ hours)")
    }
    
    if (trigger.intensityMultiplier > 1.5) {
        insights.add("Associated with higher intensity symptoms")
    } else if (trigger.intensityMultiplier < 0.8) {
        insights.add("Associated with milder symptoms")
    }
    
    return insights
}