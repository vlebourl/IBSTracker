package com.tiarkaerell.ibstracker.ui.viewmodel

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.model.CommonFood
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class FoodViewModel(private val dataRepository: DataRepository) : ViewModel() {

    // UI state for error handling and loading indicators
    private val _uiState = MutableStateFlow<FoodUiState>(FoodUiState.Idle)
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    val foodItems: StateFlow<List<FoodItem>> = dataRepository.getAllFoodItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getCommonFoodsByCategory(category: FoodCategory): Flow<List<CommonFood>> {
        return dataRepository.getCommonFoodsByCategory(category)
    }

    fun searchCommonFoods(query: String): Flow<List<CommonFood>> {
        return dataRepository.searchCommonFoods(query)
    }

    fun getTopUsedFoods(limit: Int = 4) = dataRepository.getTopUsedFoods(limit)

    fun saveFoodItem(
        name: String,
        category: FoodCategory = FoodCategory.OTHER,
        quantity: String = "",
        timestamp: Date = Date()
    ) {
        viewModelScope.launch {
            _uiState.value = FoodUiState.Saving
            try {
                dataRepository.insertFoodItem(
                    FoodItem(
                        name = name,
                        quantity = quantity,
                        timestamp = timestamp,
                        category = category
                    )
                )
                _uiState.value = FoodUiState.Success("Food added successfully")
                delay(2000)
                _uiState.value = FoodUiState.Idle
            } catch (e: SQLiteException) {
                _uiState.value = FoodUiState.Error("Database error: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = FoodUiState.Error("Failed to add food: ${e.message}")
            }
        }
    }

    fun updateFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            _uiState.value = FoodUiState.Saving
            try {
                dataRepository.updateFoodItem(foodItem)
                _uiState.value = FoodUiState.Success("Food updated successfully")
                delay(2000)
                _uiState.value = FoodUiState.Idle
            } catch (e: SQLiteException) {
                _uiState.value = FoodUiState.Error("Database error: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = FoodUiState.Error("Failed to update food: ${e.message}")
            }
        }
    }

    fun deleteFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            _uiState.value = FoodUiState.Deleting
            try {
                dataRepository.deleteFoodItem(foodItem)
                _uiState.value = FoodUiState.Success("Food deleted successfully")
                delay(2000)
                _uiState.value = FoodUiState.Idle
            } catch (e: SQLiteException) {
                _uiState.value = FoodUiState.Error("Database error: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = FoodUiState.Error("Failed to delete food: ${e.message}")
            }
        }
    }

    // ==================== VALIDATION ====================

    /**
     * Validates food name.
     * Rules:
     * - Must not be blank
     * - Must be between 1 and 100 characters
     */
    fun validateFoodName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid("Food name cannot be empty")
            name.length > 100 -> ValidationResult.Invalid("Name too long (max 100 characters)")
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
        _uiState.value = FoodUiState.Idle
    }
}