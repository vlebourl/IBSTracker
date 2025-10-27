package com.tiarkaerell.ibstracker.ui.components.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.analysis.PatternDetectionEngine
import com.tiarkaerell.ibstracker.data.analysis.ProbabilityEngine
import com.tiarkaerell.ibstracker.data.analysis.SymptomOccurrence
import com.tiarkaerell.ibstracker.data.analysis.FoodOccurrence
import com.tiarkaerell.ibstracker.data.model.*

@Composable
fun PatternsTab(
    analysisResult: AnalysisResult,
    symptoms: List<SymptomOccurrence>,
    foods: List<FoodOccurrence>,
    modifier: Modifier = Modifier
) {
    val patternEngine = remember { PatternDetectionEngine() }
    // Remove ProbabilityEngine for now since it requires correlationCalculator
    
    val detectedPatterns = remember(analysisResult, symptoms, foods) {
        val allPatterns = mutableListOf<SymptomPattern>()
        
        // Detect various pattern types
        allPatterns.addAll(patternEngine.detectTemporalPatterns(symptoms, foods))
        allPatterns.addAll(patternEngine.detectCyclicalPatterns(symptoms))
        allPatterns.addAll(patternEngine.detectTriggerCombinations(analysisResult.symptomAnalyses, foods))
        allPatterns.addAll(patternEngine.detectSeverityPatterns(symptoms))
        
        // Sort by confidence and filter meaningful patterns
        allPatterns.filter { it.confidence >= 0.3 }.sortedByDescending { it.confidence }
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pattern Summary
        item {
            PatternSummaryCard(detectedPatterns)
        }
        
        // High Confidence Patterns
        val highConfidencePatterns = detectedPatterns.filter { it.isHighConfidence }
        if (highConfidencePatterns.isNotEmpty()) {
            item {
                PatternGroupCard(
                    title = "High Confidence Patterns",
                    subtitle = "Strong patterns detected in your data",
                    patterns = highConfidencePatterns,
                    icon = Icons.Default.Verified,
                    cardColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
        
        // Moderate Confidence Patterns
        val moderateConfidencePatterns = detectedPatterns.filter { it.isModerateConfidence }
        if (moderateConfidencePatterns.isNotEmpty()) {
            item {
                PatternGroupCard(
                    title = "Emerging Patterns",
                    subtitle = "Patterns that are developing",
                    patterns = moderateConfidencePatterns,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    cardColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
        
        // Pattern Type Analysis
        if (detectedPatterns.isNotEmpty()) {
            item {
                PatternTypeAnalysisCard(detectedPatterns)
            }
        }
        
        // Empty State
        if (detectedPatterns.isEmpty()) {
            item {
                EmptyPatternsState()
            }
        }
    }
}

@Composable
private fun PatternSummaryCard(patterns: List<SymptomPattern>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Pattern Detection Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val highConfidence = patterns.count { it.isHighConfidence }
            val moderate = patterns.count { it.isModerateConfidence }
            val total = patterns.size
            
            Text(
                text = when {
                    total == 0 -> "No significant patterns detected in your data."
                    highConfidence > 0 -> "Found $total patterns including $highConfidence with high confidence."
                    moderate > 0 -> "Detected $total emerging patterns that may strengthen with more data."
                    else -> "Found $total weak patterns that need more data to confirm."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            if (total > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PatternStatChip("High", highConfidence, MaterialTheme.colorScheme.primary)
                    PatternStatChip("Moderate", moderate, MaterialTheme.colorScheme.secondary)
                    PatternStatChip("Total", total, MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun PatternStatChip(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = "$label: $count",
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color
        )
    )
}

@Composable
private fun PatternGroupCard(
    title: String,
    subtitle: String,
    patterns: List<SymptomPattern>,
    icon: ImageVector,
    cardColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Patterns
            patterns.forEach { pattern ->
                PatternCard(pattern = pattern)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PatternCard(
    pattern: SymptomPattern,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = getPatternTypeIcon(pattern.patternType),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = pattern.patternType.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "${pattern.confidencePercentage}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when {
                            pattern.isHighConfidence -> MaterialTheme.colorScheme.primary
                            pattern.isModerateConfidence -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = pattern.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (pattern.symptomType != "General") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Related to: ${pattern.symptomType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Occurrences: ${pattern.occurrenceCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PatternTypeAnalysisCard(patterns: List<SymptomPattern>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Pattern Type Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val patternsByType = patterns.groupBy { it.patternType }
            val mostCommonType = patternsByType.maxByOrNull { it.value.size }
            
            if (mostCommonType != null) {
                Text(
                    text = "Most detected: ${mostCommonType.key.displayName} (${mostCommonType.value.size} patterns)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = mostCommonType.key.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun EmptyPatternsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Patterns Detected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Continue tracking consistently to reveal patterns in your symptoms and triggers. Patterns typically emerge after 2-3 weeks of regular tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

private fun getPatternTypeIcon(patternType: PatternType): ImageVector {
    return when (patternType) {
        PatternType.FREQUENCY -> Icons.Default.Schedule
        PatternType.TEMPORAL -> Icons.Default.AccessTime
        PatternType.TRIGGER_CONSISTENCY -> Icons.Default.Repeat
        PatternType.SEVERITY_TREND -> Icons.AutoMirrored.Filled.TrendingUp
        PatternType.COMBINATION -> Icons.Default.GroupWork
        PatternType.MEAL_RELATED -> Icons.Default.Restaurant
        PatternType.CATEGORY_PREFERENCE -> Icons.Default.Category
        PatternType.SEASONAL -> Icons.Default.CalendarMonth
    }
}