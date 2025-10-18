package com.tiarkaerell.ibstracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class FoodViewModel(private val dataRepository: DataRepository) : ViewModel() {

    val foodItems: StateFlow<List<FoodItem>> = dataRepository.getAllFoodItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveFoodItem(name: String, quantity: String, date: Date = Date()) {
        viewModelScope.launch {
            dataRepository.insertFoodItem(FoodItem(name = name, quantity = quantity, date = date))
        }
    }

    fun updateFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            dataRepository.updateFoodItem(foodItem)
        }
    }

    fun deleteFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            dataRepository.deleteFoodItem(foodItem)
        }
    }
}