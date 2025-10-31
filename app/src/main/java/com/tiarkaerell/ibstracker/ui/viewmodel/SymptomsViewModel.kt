package com.tiarkaerell.ibstracker.ui.viewmodel

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.model.Symptom
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class SymptomsViewModel(private val dataRepository: DataRepository) : ViewModel() {

    // UI state for error handling and loading indicators
    private val _uiState = MutableStateFlow<SymptomsUiState>(SymptomsUiState.Idle)
    val uiState: StateFlow<SymptomsUiState> = _uiState.asStateFlow()

    val symptoms: StateFlow<List<Symptom>> = dataRepository.getAllSymptoms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveSymptom(name: String, intensity: Int, date: Date = Date()) {
        viewModelScope.launch {
            _uiState.value = SymptomsUiState.Saving
            try {
                dataRepository.insertSymptom(Symptom(name = name, intensity = intensity, date = date))
                _uiState.value = SymptomsUiState.Success("Symptom added successfully")
                delay(2000)
                _uiState.value = SymptomsUiState.Idle
            } catch (e: SQLiteException) {
                _uiState.value = SymptomsUiState.Error("Database error: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = SymptomsUiState.Error("Failed to add symptom: ${e.message}")
            }
        }
    }

    fun updateSymptom(symptom: Symptom) {
        viewModelScope.launch {
            _uiState.value = SymptomsUiState.Saving
            try {
                dataRepository.updateSymptom(symptom)
                _uiState.value = SymptomsUiState.Success("Symptom updated successfully")
                delay(2000)
                _uiState.value = SymptomsUiState.Idle
            } catch (e: SQLiteException) {
                _uiState.value = SymptomsUiState.Error("Database error: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = SymptomsUiState.Error("Failed to update symptom: ${e.message}")
            }
        }
    }

    fun deleteSymptom(symptom: Symptom) {
        viewModelScope.launch {
            _uiState.value = SymptomsUiState.Deleting
            try {
                dataRepository.deleteSymptom(symptom)
                _uiState.value = SymptomsUiState.Success("Symptom deleted successfully")
                delay(2000)
                _uiState.value = SymptomsUiState.Idle
            } catch (e: SQLiteException) {
                _uiState.value = SymptomsUiState.Error("Database error: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = SymptomsUiState.Error("Failed to delete symptom: ${e.message}")
            }
        }
    }

    // ==================== VALIDATION ====================

    /**
     * Validates symptom name.
     * Rules:
     * - Must not be blank
     * - Must be between 1 and 100 characters
     */
    fun validateSymptomName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid("Symptom name cannot be empty")
            name.length > 100 -> ValidationResult.Invalid("Name too long (max 100 characters)")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates symptom intensity.
     * Rules:
     * - Must be between 0 and 10 inclusive
     */
    fun validateIntensity(intensity: Int): ValidationResult {
        return when {
            intensity < 0 -> ValidationResult.Invalid("Intensity must be at least 0")
            intensity > 10 -> ValidationResult.Invalid("Intensity cannot exceed 10")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates timestamp.
     * Rules:
     * - Must not be in the future
     */
    fun validateTimestamp(date: Date): ValidationResult {
        return if (date.after(Date())) {
            ValidationResult.Invalid("Date cannot be in the future")
        } else {
            ValidationResult.Valid
        }
    }

    /**
     * Dismisses the current UI state message.
     */
    fun dismissMessage() {
        _uiState.value = SymptomsUiState.Idle
    }
}