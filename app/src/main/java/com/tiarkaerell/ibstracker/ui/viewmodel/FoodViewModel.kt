package com.tiarkaerell.ibstracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import kotlinx.coroutines.launch
import java.util.Date

class FoodViewModel(private val dataRepository: DataRepository) : ViewModel() {
    fun saveFoodItem(name: String, quantity: String) {
        viewModelScope.launch {
            dataRepository.insertFoodItem(FoodItem(name = name, quantity = quantity, date = Date()))
        }
    }
}