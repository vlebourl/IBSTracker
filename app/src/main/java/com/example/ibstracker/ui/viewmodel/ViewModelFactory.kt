package com.example.ibstracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ibstracker.data.repository.DataRepository

class ViewModelFactory(private val dataRepository: DataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(dataRepository) as T
        }
        if (modelClass.isAssignableFrom(SymptomsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SymptomsViewModel(dataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}