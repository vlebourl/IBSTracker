package com.example.ibstracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ibstracker.data.model.FoodItem
import com.example.ibstracker.data.repository.DataRepository
import kotlinx.coroutines.launch
import java.util.Date

class FoodViewModel(private val dataRepository: DataRepository) : ViewModel() {
    fun saveFoodItem(name: String, quantity: String) {
        viewModelScope.launch {
            dataRepository.insertFoodItem(FoodItem(name = name, quantity = quantity, date = Date()))
        }
    }
}