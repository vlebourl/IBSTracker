package com.tiarkaerell.ibstracker.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.tiarkaerell.ibstracker.data.model.AnalysisFilters
import com.tiarkaerell.ibstracker.data.model.AnalysisTimeWindow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FilterPreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "analysis_filters", 
        Context.MODE_PRIVATE
    )
    
    private val _savedFilters = MutableStateFlow(loadFilters())
    val savedFilters: Flow<AnalysisFilters> = _savedFilters.asStateFlow()
    
    private val _savedTimeWindow = MutableStateFlow(loadTimeWindow())
    val savedTimeWindow: Flow<AnalysisTimeWindow> = _savedTimeWindow.asStateFlow()
    
    companion object {
        private const val KEY_SEVERITY_THRESHOLD = "severity_threshold"
        private const val KEY_SYMPTOM_TYPES = "symptom_types"
        private const val KEY_FOOD_CATEGORIES = "food_categories"
        private const val KEY_EXCLUDE_FOODS = "exclude_foods"
        private const val KEY_MINIMUM_CONFIDENCE = "minimum_confidence"
        private const val KEY_SHOW_LOW_OCCURRENCE = "show_low_occurrence"
        
        private const val KEY_START_DATE = "start_date"
        private const val KEY_END_DATE = "end_date"
        private const val KEY_WINDOW_SIZE_HOURS = "window_size_hours"
        private const val KEY_MINIMUM_OCCURRENCES = "minimum_occurrences"
        private const val KEY_MINIMUM_OBSERVATION_DAYS = "minimum_observation_days"
        
        private const val DEFAULT_WINDOW_SIZE_HOURS = 8
        private const val DEFAULT_MINIMUM_OCCURRENCES = 3
        private const val DEFAULT_MINIMUM_OBSERVATION_DAYS = 14
    }
    
    fun saveFilters(filters: AnalysisFilters) {
        prefs.edit().apply {
            // Save severity threshold
            if (filters.severityThreshold != null) {
                putInt(KEY_SEVERITY_THRESHOLD, filters.severityThreshold)
            } else {
                remove(KEY_SEVERITY_THRESHOLD)
            }
            
            // Save symptom types
            putStringSet(KEY_SYMPTOM_TYPES, filters.symptomTypes)
            
            // Save food categories
            putStringSet(KEY_FOOD_CATEGORIES, filters.foodCategories)
            
            // Save exclude foods
            putStringSet(KEY_EXCLUDE_FOODS, filters.excludeFoods)
            
            // Save minimum confidence
            putFloat(KEY_MINIMUM_CONFIDENCE, filters.minimumConfidence.toFloat())
            
            // Save show low occurrence correlations
            putBoolean(KEY_SHOW_LOW_OCCURRENCE, filters.showLowOccurrenceCorrelations)
            
            apply()
        }
        
        _savedFilters.value = filters
    }
    
    fun saveTimeWindow(timeWindow: AnalysisTimeWindow) {
        prefs.edit().apply {
            putString(KEY_START_DATE, timeWindow.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            putString(KEY_END_DATE, timeWindow.endDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            putInt(KEY_WINDOW_SIZE_HOURS, timeWindow.windowSizeHours)
            putInt(KEY_MINIMUM_OCCURRENCES, timeWindow.minimumOccurrences)
            putInt(KEY_MINIMUM_OBSERVATION_DAYS, timeWindow.minimumObservationDays)
            apply()
        }
        
        _savedTimeWindow.value = timeWindow
    }
    
    fun loadFilters(): AnalysisFilters {
        return AnalysisFilters(
            severityThreshold = if (prefs.contains(KEY_SEVERITY_THRESHOLD)) {
                prefs.getInt(KEY_SEVERITY_THRESHOLD, 1)
            } else null,
            symptomTypes = prefs.getStringSet(KEY_SYMPTOM_TYPES, emptySet()) ?: emptySet(),
            foodCategories = prefs.getStringSet(KEY_FOOD_CATEGORIES, emptySet()) ?: emptySet(),
            excludeFoods = prefs.getStringSet(KEY_EXCLUDE_FOODS, emptySet()) ?: emptySet(),
            minimumConfidence = prefs.getFloat(KEY_MINIMUM_CONFIDENCE, 0.0f).toDouble(),
            showLowOccurrenceCorrelations = prefs.getBoolean(KEY_SHOW_LOW_OCCURRENCE, true)
        )
    }
    
    fun loadTimeWindow(): AnalysisTimeWindow {
        val startDateString = prefs.getString(KEY_START_DATE, null)
        val endDateString = prefs.getString(KEY_END_DATE, null)
        
        val startDate = if (startDateString != null) {
            try {
                LocalDate.parse(startDateString, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                LocalDate.now().minusDays(30)
            }
        } else {
            LocalDate.now().minusDays(30)
        }
        
        val endDate = if (endDateString != null) {
            try {
                LocalDate.parse(endDateString, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                LocalDate.now()
            }
        } else {
            LocalDate.now()
        }
        
        return AnalysisTimeWindow(
            startDate = startDate,
            endDate = endDate,
            windowSizeHours = prefs.getInt(KEY_WINDOW_SIZE_HOURS, DEFAULT_WINDOW_SIZE_HOURS),
            minimumOccurrences = prefs.getInt(KEY_MINIMUM_OCCURRENCES, DEFAULT_MINIMUM_OCCURRENCES),
            minimumObservationDays = prefs.getInt(KEY_MINIMUM_OBSERVATION_DAYS, DEFAULT_MINIMUM_OBSERVATION_DAYS)
        )
    }
    
    fun clearAllFilters() {
        prefs.edit().apply {
            remove(KEY_SEVERITY_THRESHOLD)
            remove(KEY_SYMPTOM_TYPES)
            remove(KEY_FOOD_CATEGORIES)
            remove(KEY_EXCLUDE_FOODS)
            putFloat(KEY_MINIMUM_CONFIDENCE, 0.0f)
            putBoolean(KEY_SHOW_LOW_OCCURRENCE, true)
            apply()
        }
        
        _savedFilters.value = AnalysisFilters()
    }
    
    fun resetTimeWindowToDefault() {
        val defaultTimeWindow = AnalysisTimeWindow(
            startDate = LocalDate.now().minusDays(30),
            endDate = LocalDate.now(),
            windowSizeHours = DEFAULT_WINDOW_SIZE_HOURS,
            minimumOccurrences = DEFAULT_MINIMUM_OCCURRENCES,
            minimumObservationDays = DEFAULT_MINIMUM_OBSERVATION_DAYS
        )
        
        saveTimeWindow(defaultTimeWindow)
    }
    
    fun hasCustomFilters(): Boolean {
        val filters = loadFilters()
        return filters.severityThreshold != null ||
               filters.symptomTypes.isNotEmpty() ||
               filters.foodCategories.isNotEmpty() ||
               filters.excludeFoods.isNotEmpty() ||
               filters.minimumConfidence > 0.0 ||
               !filters.showLowOccurrenceCorrelations
    }
    
    fun hasCustomTimeWindow(): Boolean {
        val savedWindow = loadTimeWindow()
        val defaultWindow = AnalysisTimeWindow(
            startDate = LocalDate.now().minusDays(30),
            endDate = LocalDate.now(),
            windowSizeHours = DEFAULT_WINDOW_SIZE_HOURS,
            minimumOccurrences = DEFAULT_MINIMUM_OCCURRENCES,
            minimumObservationDays = DEFAULT_MINIMUM_OBSERVATION_DAYS
        )
        
        return savedWindow.windowSizeHours != defaultWindow.windowSizeHours ||
               savedWindow.minimumOccurrences != defaultWindow.minimumOccurrences ||
               savedWindow.minimumObservationDays != defaultWindow.minimumObservationDays
    }
}