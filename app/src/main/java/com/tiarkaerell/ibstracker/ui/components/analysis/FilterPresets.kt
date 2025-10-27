package com.tiarkaerell.ibstracker.ui.components.analysis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.model.AnalysisFilters
import com.tiarkaerell.ibstracker.ui.state.AnalysisFilterState

@Composable
fun FilterPresets(
    filterState: AnalysisFilterState,
    modifier: Modifier = Modifier
) {
    var showPresetsDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            TextButton(
                onClick = { showPresetsDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "View all presets",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("More")
            }
        }
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(getQuickFilterPresets()) { preset ->
                PresetFilterCard(
                    preset = preset,
                    isActive = isPresetActive(preset, filterState.filters),
                    onClick = {
                        applyPreset(preset, filterState)
                    }
                )
            }
        }
    }
    
    if (showPresetsDialog) {
        FilterPresetsDialog(
            filterState = filterState,
            onDismiss = { showPresetsDialog = false }
        )
    }
}

@Composable
private fun PresetFilterCard(
    preset: FilterPreset,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isActive) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = preset.icon,
                contentDescription = preset.name,
                tint = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
            )
        }
    }
}

@Composable
fun FilterPresetsDialog(
    filterState: AnalysisFilterState,
    onApplyFilter: () -> Unit = {},
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Presets",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose a preset to quickly apply common filter combinations:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                getAllFilterPresets().forEach { preset ->
                    PresetListItem(
                        preset = preset,
                        isActive = isPresetActive(preset, filterState.filters),
                        onClick = {
                            applyPreset(preset, filterState)
                            onApplyFilter()
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PresetListItem(
    preset: FilterPreset,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = preset.icon,
                contentDescription = preset.name,
                tint = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
                )
                
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                if (preset.details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset.details,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

private data class FilterPreset(
    val id: String,
    val name: String,
    val description: String,
    val details: String,
    val icon: ImageVector,
    val filters: AnalysisFilters
)

private fun getQuickFilterPresets(): List<FilterPreset> {
    return listOf(
        FilterPreset(
            id = "high_confidence",
            name = "High Confidence",
            description = "Strong correlations only",
            details = "Severity ≥5, Confidence ≥70%",
            icon = Icons.Default.HighQuality,
            filters = AnalysisFilters(
                severityThreshold = 5,
                minimumConfidence = 0.7,
                showLowOccurrenceCorrelations = false
            )
        ),
        FilterPreset(
            id = "quick_insights",
            name = "Quick Insights",
            description = "Broad view with patterns",
            details = "Severity ≥3, Confidence ≥40%",
            icon = Icons.Default.Speed,
            filters = AnalysisFilters(
                severityThreshold = 3,
                minimumConfidence = 0.4,
                showLowOccurrenceCorrelations = true
            )
        ),
        FilterPreset(
            id = "all_data",
            name = "All Data",
            description = "Complete overview",
            details = "No filters applied",
            icon = Icons.Default.Visibility,
            filters = AnalysisFilters()
        )
    )
}

private fun getAllFilterPresets(): List<FilterPreset> {
    return getQuickFilterPresets() + listOf(
        FilterPreset(
            id = "common_triggers",
            name = "Common Triggers",
            description = "Focus on known IBS triggers",
            details = "High FODMAP, Dairy, Spicy Foods",
            icon = Icons.Default.BookmarkBorder,
            filters = AnalysisFilters(
                foodCategories = setOf("High FODMAP", "Dairy", "Spicy Foods"),
                minimumConfidence = 0.5,
                showLowOccurrenceCorrelations = false
            )
        ),
        FilterPreset(
            id = "severe_only",
            name = "Severe Symptoms",
            description = "High intensity symptoms",
            details = "Severity ≥7, Confidence ≥60%",
            icon = Icons.Default.HighQuality,
            filters = AnalysisFilters(
                severityThreshold = 7,
                minimumConfidence = 0.6,
                showLowOccurrenceCorrelations = false
            )
        )
    )
}

private fun isPresetActive(preset: FilterPreset, currentFilters: AnalysisFilters): Boolean {
    return preset.filters == currentFilters
}

private fun applyPreset(preset: FilterPreset, filterState: AnalysisFilterState) {
    filterState.updateSeverityThreshold(preset.filters.severityThreshold)
    filterState.updateSymptomTypes(preset.filters.symptomTypes)
    filterState.updateFoodCategories(preset.filters.foodCategories)
    filterState.updateExcludeFoods(preset.filters.excludeFoods)
    filterState.updateMinimumConfidence(preset.filters.minimumConfidence)
    filterState.updateShowLowOccurrenceCorrelations(preset.filters.showLowOccurrenceCorrelations)
}