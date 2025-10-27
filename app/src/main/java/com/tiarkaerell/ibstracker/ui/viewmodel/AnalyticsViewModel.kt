package com.tiarkaerell.ibstracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.model.*
import com.tiarkaerell.ibstracker.data.repository.AnalysisRepository
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import com.tiarkaerell.ibstracker.data.preferences.FilterPreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class AnalyticsViewModel(
    private val analysisRepository: AnalysisRepository,
    private val dataRepository: DataRepository,
    private val filterPreferencesManager: FilterPreferencesManager
) : ViewModel() {
    
    private val _analysisResult = MutableStateFlow<AnalysisResult?>(null)
    val analysisResult: StateFlow<AnalysisResult?> = _analysisResult.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _currentTimeWindow = MutableStateFlow(filterPreferencesManager.loadTimeWindow())
    val currentTimeWindow: StateFlow<AnalysisTimeWindow> = _currentTimeWindow.asStateFlow()
    
    private val _currentFilters = MutableStateFlow(filterPreferencesManager.loadFilters())
    val currentFilters: StateFlow<AnalysisFilters> = _currentFilters.asStateFlow()
    
    init {
        generateAnalysis()

        // Observe analysis results from repository
        viewModelScope.launch {
            analysisRepository.observeAnalysisResults()
                .collect { result ->
                    _analysisResult.value = result
                }
        }

        // Automatically refresh analysis when food items change
        viewModelScope.launch {
            dataRepository.getAllFoodItems()
                .drop(1) // Skip initial emission to avoid double analysis on init
                .collect {
                    // Invalidate cache to force fresh analysis
                    analysisRepository.invalidateCache(java.time.Instant.now())
                    generateAnalysis()
                }
        }

        // Automatically refresh analysis when symptoms change
        viewModelScope.launch {
            dataRepository.getAllSymptoms()
                .drop(1) // Skip initial emission to avoid double analysis on init
                .collect {
                    // Invalidate cache to force fresh analysis
                    analysisRepository.invalidateCache(java.time.Instant.now())
                    generateAnalysis()
                }
        }
    }
    
    fun generateAnalysis() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = analysisRepository.generateAnalysis(
                    timeWindow = _currentTimeWindow.value,
                    filters = _currentFilters.value
                )
                _analysisResult.value = result
                
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to generate analysis"
                _analysisResult.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateTimeWindow(timeWindow: AnalysisTimeWindow) {
        _currentTimeWindow.value = timeWindow
        filterPreferencesManager.saveTimeWindow(timeWindow)
        generateAnalysis()
    }
    
    fun updateFilters(filters: AnalysisFilters) {
        _currentFilters.value = filters
        filterPreferencesManager.saveFilters(filters)
        generateAnalysis()
    }
    
    fun updateSeverityThreshold(threshold: Int?) {
        val updatedFilters = _currentFilters.value.copy(severityThreshold = threshold)
        updateFilters(updatedFilters)
    }
    
    fun updateSymptomTypes(symptomTypes: Set<String>) {
        val updatedFilters = _currentFilters.value.copy(symptomTypes = symptomTypes)
        updateFilters(updatedFilters)
    }
    
    fun updateMinimumConfidence(confidence: Double) {
        val updatedFilters = _currentFilters.value.copy(minimumConfidence = confidence)
        updateFilters(updatedFilters)
    }
    
    fun refreshAnalysis() {
        viewModelScope.launch {
            // Invalidate cache to force fresh analysis
            analysisRepository.invalidateCache(java.time.Instant.now())
            generateAnalysis()
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun clearAllFilters() {
        filterPreferencesManager.clearAllFilters()
        _currentFilters.value = AnalysisFilters()
        generateAnalysis()
    }
    
    fun resetTimeWindowToDefault() {
        filterPreferencesManager.resetTimeWindowToDefault()
        _currentTimeWindow.value = filterPreferencesManager.loadTimeWindow()
        generateAnalysis()
    }
    
    fun hasCustomFilters(): Boolean {
        return filterPreferencesManager.hasCustomFilters()
    }
    
    fun hasCustomTimeWindow(): Boolean {
        return filterPreferencesManager.hasCustomTimeWindow()
    }
    
    private fun createDefaultTimeWindow(): AnalysisTimeWindow {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(30)
        
        return AnalysisTimeWindow(
            startDate = startDate,
            endDate = endDate,
            windowSizeHours = 8,
            minimumOccurrences = 3,
            minimumObservationDays = 14
        )
    }
}