package com.tiarkaerell.ibstracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.analytics.AnalyticsEngine
import com.tiarkaerell.ibstracker.data.model.InsightSummary
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AnalyticsViewModel(private val dataRepository: DataRepository) : ViewModel() {
    
    private val analyticsEngine = AnalyticsEngine()
    
    private val _insights = MutableStateFlow<InsightSummary?>(null)
    val insights: StateFlow<InsightSummary?> = _insights.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        generateInsights()
    }
    
    fun generateInsights() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Combine food items and symptoms
                val foodItems = dataRepository.getAllFoodItems().first()
                val symptoms = dataRepository.getAllSymptoms().first()
                
                // Generate insights
                val summary = analyticsEngine.generateInsights(foodItems, symptoms)
                _insights.value = summary
                
            } catch (e: Exception) {
                // Handle error - could add error state if needed
                _insights.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshInsights() {
        generateInsights()
    }
}