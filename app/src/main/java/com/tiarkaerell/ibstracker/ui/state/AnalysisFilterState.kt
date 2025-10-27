package com.tiarkaerell.ibstracker.ui.state

import androidx.compose.runtime.*
import com.tiarkaerell.ibstracker.data.analysis.FilterStats
import com.tiarkaerell.ibstracker.data.model.AnalysisFilters
import com.tiarkaerell.ibstracker.data.model.AnalysisTimeWindow
import java.time.LocalDate

@Stable
class AnalysisFilterState {
    
    // Core filter state
    var filters by mutableStateOf(AnalysisFilters())
    
    var timeWindow by mutableStateOf(
        AnalysisTimeWindow(
            startDate = LocalDate.now().minusDays(30),
            endDate = LocalDate.now()
        )
    )
    
    // UI state
    var isFilterPanelExpanded by mutableStateOf(false)
        private set
    
    var isDateRangeDialogOpen by mutableStateOf(false)
        private set

    var isQuickFiltersDialogOpen by mutableStateOf(false)
        private set

    var lastAppliedFilters by mutableStateOf(AnalysisFilters())
        private set
    
    var filterStats by mutableStateOf<FilterStats?>(null)
        private set
    
    // Derived state
    val hasActiveFilters: Boolean
        get() = filters.hasActiveFilters()
    
    val hasUnappliedChanges: Boolean
        get() = filters != lastAppliedFilters
    
    val activeFilterCount: Int
        get() = filters.getActiveFilterCount()
    
    // Filter management
    fun updateSeverityThreshold(threshold: Int?) {
        filters = filters.copy(severityThreshold = threshold)
    }
    
    fun updateSymptomTypes(types: Set<String>) {
        filters = filters.copy(symptomTypes = types)
    }
    
    fun updateFoodCategories(categories: Set<String>) {
        filters = filters.copy(foodCategories = categories)
    }
    
    fun updateExcludeFoods(foods: Set<String>) {
        filters = filters.copy(excludeFoods = foods)
    }
    
    fun updateMinimumConfidence(confidence: Double) {
        filters = filters.copy(minimumConfidence = confidence)
    }
    
    fun updateShowLowOccurrenceCorrelations(show: Boolean) {
        filters = filters.copy(showLowOccurrenceCorrelations = show)
    }
    
    fun updateTimeWindow(newTimeWindow: AnalysisTimeWindow) {
        timeWindow = newTimeWindow
    }
    
    // Bulk operations
    fun applyFilters() {
        lastAppliedFilters = filters.copy()
    }
    
    fun resetFilters() {
        filters = AnalysisFilters()
    }
    
    fun resetToLastApplied() {
        filters = lastAppliedFilters.copy()
    }
    
    // UI state management
    fun toggleFilterPanel() {
        isFilterPanelExpanded = !isFilterPanelExpanded
    }
    
    fun expandFilterPanel() {
        isFilterPanelExpanded = true
    }
    
    fun collapseFilterPanel() {
        isFilterPanelExpanded = false
    }
    
    fun openDateRangeDialog() {
        isDateRangeDialogOpen = true
    }
    
    fun closeDateRangeDialog() {
        isDateRangeDialogOpen = false
    }

    fun showQuickFiltersDialog() {
        isQuickFiltersDialogOpen = true
    }

    fun closeQuickFiltersDialog() {
        isQuickFiltersDialogOpen = false
    }

    // Filter removal
    fun removeFilter(filterType: FilterType) {
        filters = when (filterType) {
            FilterType.SEVERITY -> filters.copy(severityThreshold = null)
            FilterType.SYMPTOM_TYPES -> filters.copy(symptomTypes = emptySet())
            FilterType.FOOD_CATEGORIES -> filters.copy(foodCategories = emptySet())
            FilterType.EXCLUDE_FOODS -> filters.copy(excludeFoods = emptySet())
            FilterType.MINIMUM_CONFIDENCE -> filters.copy(minimumConfidence = 0.0)
            FilterType.LOW_OCCURRENCE -> filters.copy(showLowOccurrenceCorrelations = true)
        }
    }
    
    // Statistics update
    fun updateFilterStats(stats: FilterStats) {
        filterStats = stats
    }
    
    fun clearFilterStats() {
        filterStats = null
    }
    
    // Preset configurations
    fun applyHighConfidencePreset() {
        filters = AnalysisFilters(
            severityThreshold = 5,
            minimumConfidence = 0.7,
            showLowOccurrenceCorrelations = false
        )
    }
    
    fun applyQuickInsightsPreset() {
        filters = AnalysisFilters(
            severityThreshold = 3,
            minimumConfidence = 0.4,
            showLowOccurrenceCorrelations = true
        )
    }
    
    fun applyCommonTriggersPreset() {
        filters = AnalysisFilters(
            foodCategories = setOf("High FODMAP", "Dairy", "Spicy Foods"),
            minimumConfidence = 0.5,
            showLowOccurrenceCorrelations = false
        )
    }
}

enum class FilterType {
    SEVERITY,
    SYMPTOM_TYPES,
    FOOD_CATEGORIES,
    EXCLUDE_FOODS,
    MINIMUM_CONFIDENCE,
    LOW_OCCURRENCE
}

@Composable
fun rememberAnalysisFilterState(): AnalysisFilterState {
    return remember { AnalysisFilterState() }
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