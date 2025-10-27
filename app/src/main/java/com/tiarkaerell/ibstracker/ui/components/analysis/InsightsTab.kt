package com.tiarkaerell.ibstracker.ui.components.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.analysis.InsightEngine
import com.tiarkaerell.ibstracker.data.model.*

@Composable
fun InsightsTab(
    analysisResult: AnalysisResult,
    modifier: Modifier = Modifier
) {
    val insightEngine = remember { InsightEngine() }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analysis Summary Section
        item {
            InsightSection(
                title = "Analysis Summary",
                icon = Icons.Default.Assessment,
                insights = insightEngine.generateAnalysisSummary(analysisResult)
            )
        }
        
        // Recommendations Section
        if (analysisResult.symptomAnalyses.isNotEmpty()) {
            item {
                InsightSection(
                    title = "Recommendations",
                    icon = Icons.Default.Lightbulb,
                    insights = insightEngine.generateRecommendations(analysisResult.symptomAnalyses),
                    cardColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
        
        // Pattern Insights Section
        if (analysisResult.symptomAnalyses.isNotEmpty()) {
            item {
                InsightSection(
                    title = "Pattern Insights",
                    icon = Icons.Default.Timeline,
                    insights = insightEngine.generatePatternInsights(analysisResult.symptomAnalyses)
                )
            }
        }
        
        // Personalized Insights Section
        if (analysisResult.symptomAnalyses.isNotEmpty()) {
            item {
                InsightSection(
                    title = "Personalized Insights",
                    icon = Icons.Default.Person,
                    insights = insightEngine.generatePersonalizedInsights(
                        analysisResult.symptomAnalyses,
                        analysisResult.analysisTimeWindow
                    )
                )
            }
        }
        
        // Empty State
        if (analysisResult.symptomAnalyses.isEmpty()) {
            item {
                EmptyInsightsState()
            }
        }
    }
}

@Composable
private fun InsightSection(
    title: String,
    icon: ImageVector,
    insights: List<String>,
    modifier: Modifier = Modifier,
    cardColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section Header
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
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Insights Content
            insights.forEach { insight ->
                InsightItem(
                    text = insight,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            if (insights.isEmpty()) {
                Text(
                    text = "No insights available for this section",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun InsightItem(
    text: String,
    modifier: Modifier = Modifier
) {
    val isListItem = text.startsWith("•")
    val isSectionHeader = text.endsWith(":")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (!isListItem && !isSectionHeader) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
        
        Text(
            text = text,
            style = when {
                isSectionHeader -> MaterialTheme.typography.titleSmall
                isListItem -> MaterialTheme.typography.bodyMedium
                else -> MaterialTheme.typography.bodyMedium
            },
            fontWeight = when {
                isSectionHeader -> FontWeight.SemiBold
                text.contains("Strong recommendation") || text.contains("Important") -> FontWeight.SemiBold
                else -> FontWeight.Normal
            },
            color = when {
                text.contains("Strong recommendation") -> MaterialTheme.colorScheme.error
                text.contains("Important") -> MaterialTheme.colorScheme.error
                text.contains("High confidence") -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EmptyInsightsState() {
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
                imageVector = Icons.Default.Insights,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Insights Available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Track more food and symptoms to generate meaningful insights about your IBS patterns.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun InsightsTabWithActions(
    analysisResult: AnalysisResult,
    onShareInsights: (List<String>) -> Unit,
    onExportInsights: () -> Unit,
    modifier: Modifier = Modifier
) {
    val insightEngine = remember { InsightEngine() }
    val allInsights = remember(analysisResult) {
        insightEngine.generateAnalysisSummary(analysisResult) +
        insightEngine.generateRecommendations(analysisResult.symptomAnalyses) +
        insightEngine.generatePatternInsights(analysisResult.symptomAnalyses) +
        insightEngine.generatePersonalizedInsights(
            analysisResult.symptomAnalyses,
            analysisResult.analysisTimeWindow
        )
    }
    
    Column(modifier = modifier) {
        // Action Bar
        if (analysisResult.symptomAnalyses.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { onShareInsights(allInsights) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }
                
                OutlinedButton(onClick = onExportInsights) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export")
                }
            }
        }
        
        // Insights Content
        InsightsTab(
            analysisResult = analysisResult,
            modifier = Modifier.weight(1f)
        )
    }
}