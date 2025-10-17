package com.tiarkaerell.ibstracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import com.tiarkaerell.ibstracker.data.repository.SettingsRepository

class ViewModelFactory(
    private val dataRepository: DataRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(dataRepository) as T
        }
        if (modelClass.isAssignableFrom(SymptomsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SymptomsViewModel(dataRepository) as T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}