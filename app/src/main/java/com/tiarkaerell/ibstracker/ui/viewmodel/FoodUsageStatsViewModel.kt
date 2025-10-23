package com.tiarkaerell.ibstracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.model.FoodUsageStats
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * FoodUsageStatsViewModel - Smart Food Categorization System (v1.9.0)
 *
 * Manages state for quick-add shortcuts based on food usage statistics.
 *
 * Purpose: User Story 3 - Quick Add from Usage-Sorted Shortcuts
 * - Provides StateFlow for top 6 most-used foods
 * - Auto-updates when usage counts change
 * - Sorted by usage_count DESC, then name ASC
 *
 * Performance Target (SC-001):
 * - Quick-add update latency: <200ms from write to UI re-render
 */
class FoodUsageStatsViewModel(private val dataRepository: DataRepository) : ViewModel() {

    /**
     * Top 6 most-used foods for quick-add shortcuts.
     *
     * Sorted by:
     * 1. usage_count DESC (most used first)
     * 2. name ASC (alphabetically for equal usage)
     *
     * Updates automatically when:
     * - New food is logged (increments usage_count)
     * - Food is deleted (decrements usage_count)
     * - Usage stats are manually updated
     */
    val topUsedFoods: StateFlow<List<FoodUsageStats>> = dataRepository.getTopUsedFoods(limit = 6)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
