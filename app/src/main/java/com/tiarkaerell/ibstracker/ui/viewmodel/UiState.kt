package com.tiarkaerell.ibstracker.ui.viewmodel

/**
 * UI state for Food operations.
 *
 * Represents different states during food-related operations
 * (add, update, delete) to provide appropriate UI feedback.
 */
sealed class FoodUiState {
    /** Initial idle state - no operation in progress */
    object Idle : FoodUiState()

    /** Currently saving a food item (add or update) */
    object Saving : FoodUiState()

    /** Currently deleting a food item */
    object Deleting : FoodUiState()

    /** Operation completed successfully */
    data class Success(val message: String) : FoodUiState()

    /** Operation failed with an error */
    data class Error(val message: String) : FoodUiState()
}

/**
 * UI state for Symptom operations.
 *
 * Represents different states during symptom-related operations
 * (add, update, delete) to provide appropriate UI feedback.
 */
sealed class SymptomsUiState {
    /** Initial idle state - no operation in progress */
    object Idle : SymptomsUiState()

    /** Currently saving a symptom (add or update) */
    object Saving : SymptomsUiState()

    /** Currently deleting a symptom */
    object Deleting : SymptomsUiState()

    /** Operation completed successfully */
    data class Success(val message: String) : SymptomsUiState()

    /** Operation failed with an error */
    data class Error(val message: String) : SymptomsUiState()
}

/**
 * Validation result for input fields.
 *
 * Used to validate user inputs before saving to database,
 * providing clear error messages for invalid inputs.
 */
sealed class ValidationResult {
    /** Input is valid and can be saved */
    object Valid : ValidationResult()

    /** Input is invalid with specific error message */
    data class Invalid(val error: String) : ValidationResult()

    /**
     * Helper function to check if validation passed.
     */
    fun isValid(): Boolean = this is Valid

    /**
     * Helper function to get error message if validation failed.
     */
    fun getErrorOrNull(): String? = when (this) {
        is Invalid -> error
        is Valid -> null
    }
}
