package com.tiarkaerell.ibstracker.ui.components.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.model.AnalysisFilters
import com.tiarkaerell.ibstracker.data.model.IBSTriggerCategory

@Composable
fun FilterChips(
    filters: AnalysisFilters,
    onFiltersChange: (AnalysisFilters) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val hasActiveFilters = filters.hasActiveFilters()
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Filter header with expand/collapse
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters",
                    tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Filters${if (hasActiveFilters) " (${filters.getActiveFilterCount()})" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (hasActiveFilters) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row {
                if (hasActiveFilters) {
                    TextButton(
                        onClick = { onFiltersChange(AnalysisFilters()) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear filters",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
                
                IconButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse filters" else "Expand filters"
                    )
                }
            }
        }
        
        // Active filters summary (always visible)
        if (hasActiveFilters && !isExpanded) {
            ActiveFiltersSummary(
                filters = filters,
                onRemoveFilter = { filterType -> removeFilter(filters, filterType, onFiltersChange) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Expanded filter options
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Severity threshold filter
                SeverityThresholdSection(
                    severityThreshold = filters.severityThreshold,
                    onSeverityThresholdChange = { threshold ->
                        onFiltersChange(filters.copy(severityThreshold = threshold))
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Symptom types filter
                SymptomTypesSection(
                    selectedSymptomTypes = filters.symptomTypes,
                    onSymptomTypesChange = { types ->
                        onFiltersChange(filters.copy(symptomTypes = types))
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Food categories filter
                FoodCategoriesSection(
                    selectedFoodCategories = filters.foodCategories,
                    onFoodCategoriesChange = { categories ->
                        onFiltersChange(filters.copy(foodCategories = categories))
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Minimum confidence filter
                MinimumConfidenceSection(
                    minimumConfidence = filters.minimumConfidence,
                    onMinimumConfidenceChange = { confidence ->
                        onFiltersChange(filters.copy(minimumConfidence = confidence))
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Advanced options
                AdvancedOptionsSection(
                    showLowOccurrenceCorrelations = filters.showLowOccurrenceCorrelations,
                    onShowLowOccurrenceChange = { show ->
                        onFiltersChange(filters.copy(showLowOccurrenceCorrelations = show))
                    }
                )
            }
        }
    }
}

@Composable
private fun ActiveFiltersSummary(
    filters: AnalysisFilters,
    onRemoveFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (filters.severityThreshold != null) {
            item {
                FilterChip(
                    onClick = { onRemoveFilter("severity") },
                    label = { Text("Severity ≥ ${filters.severityThreshold}") },
                    selected = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Remove severity filter",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        if (filters.symptomTypes.isNotEmpty()) {
            item {
                FilterChip(
                    onClick = { onRemoveFilter("symptoms") },
                    label = { Text("Symptoms (${filters.symptomTypes.size})") },
                    selected = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Remove symptom filter",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        if (filters.foodCategories.isNotEmpty()) {
            item {
                FilterChip(
                    onClick = { onRemoveFilter("categories") },
                    label = { Text("Categories (${filters.foodCategories.size})") },
                    selected = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Remove category filter",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        if (filters.minimumConfidence > 0.0) {
            item {
                FilterChip(
                    onClick = { onRemoveFilter("confidence") },
                    label = { Text("Confidence ≥ ${(filters.minimumConfidence * 100).toInt()}%") },
                    selected = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Remove confidence filter",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SeverityThresholdSection(
    severityThreshold: Int?,
    onSeverityThresholdChange: (Int?) -> Unit
) {
    Column {
        Text(
            text = "Minimum Severity",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    onClick = { onSeverityThresholdChange(null) },
                    label = { Text("All") },
                    selected = severityThreshold == null
                )
            }
            
            items((1..10).toList()) { severity ->
                FilterChip(
                    onClick = { onSeverityThresholdChange(severity) },
                    label = { Text("≥ $severity") },
                    selected = severityThreshold == severity
                )
            }
        }
    }
}

@Composable
private fun SymptomTypesSection(
    selectedSymptomTypes: Set<String>,
    onSymptomTypesChange: (Set<String>) -> Unit
) {
    val commonSymptoms = listOf(
        "Diarrhea", "Constipation", "Bloating", "Nausea", 
        "Abdominal Pain", "Gas", "Cramping", "Indigestion"
    )
    
    Column {
        Text(
            text = "Symptom Types",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(commonSymptoms) { symptom ->
                FilterChip(
                    onClick = {
                        val newSelection = if (selectedSymptomTypes.contains(symptom)) {
                            selectedSymptomTypes - symptom
                        } else {
                            selectedSymptomTypes + symptom
                        }
                        onSymptomTypesChange(newSelection)
                    },
                    label = { Text(symptom) },
                    selected = selectedSymptomTypes.contains(symptom)
                )
            }
        }
    }
}

@Composable
private fun FoodCategoriesSection(
    selectedFoodCategories: Set<String>,
    onFoodCategoriesChange: (Set<String>) -> Unit
) {
    val categories = IBSTriggerCategory.values().map { it.displayName }
    
    Column {
        Text(
            text = "Food Categories",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    onClick = {
                        val newSelection = if (selectedFoodCategories.contains(category)) {
                            selectedFoodCategories - category
                        } else {
                            selectedFoodCategories + category
                        }
                        onFoodCategoriesChange(newSelection)
                    },
                    label = { Text(category) },
                    selected = selectedFoodCategories.contains(category)
                )
            }
        }
    }
}

@Composable
private fun MinimumConfidenceSection(
    minimumConfidence: Double,
    onMinimumConfidenceChange: (Double) -> Unit
) {
    Column {
        Text(
            text = "Minimum Confidence: ${(minimumConfidence * 100).toInt()}%",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = minimumConfidence.toFloat(),
            onValueChange = { onMinimumConfidenceChange(it.toDouble()) },
            valueRange = 0f..1f,
            steps = 19 // 5% increments
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "100%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AdvancedOptionsSection(
    showLowOccurrenceCorrelations: Boolean,
    onShowLowOccurrenceChange: (Boolean) -> Unit
) {
    Column {
        Text(
            text = "Advanced Options",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Show Low Occurrence Correlations",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Include correlations with fewer data points",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Switch(
                checked = showLowOccurrenceCorrelations,
                onCheckedChange = onShowLowOccurrenceChange
            )
        }
    }
}

private fun removeFilter(
    filters: AnalysisFilters,
    filterType: String,
    onFiltersChange: (AnalysisFilters) -> Unit
) {
    val newFilters = when (filterType) {
        "severity" -> filters.copy(severityThreshold = null)
        "symptoms" -> filters.copy(symptomTypes = emptySet())
        "categories" -> filters.copy(foodCategories = emptySet())
        "confidence" -> filters.copy(minimumConfidence = 0.0)
        else -> filters
    }
    onFiltersChange(newFilters)
}

// Extension functions for AnalysisFilters
private fun AnalysisFilters.hasActiveFilters(): Boolean {
    return severityThreshold != null ||
           symptomTypes.isNotEmpty() ||
           foodCategories.isNotEmpty() ||
           excludeFoods.isNotEmpty() ||
           minimumConfidence > 0.0 ||
           !showLowOccurrenceCorrelations
}

private fun AnalysisFilters.getActiveFilterCount(): Int {
    var count = 0
    if (severityThreshold != null) count++
    if (symptomTypes.isNotEmpty()) count++
    if (foodCategories.isNotEmpty()) count++
    if (excludeFoods.isNotEmpty()) count++
    if (minimumConfidence > 0.0) count++
    if (!showLowOccurrenceCorrelations) count++
    return count
}