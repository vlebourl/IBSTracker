package com.example.ibstracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ibstracker.data.model.Symptom
import com.example.ibstracker.data.repository.DataRepository
import kotlinx.coroutines.launch
import java.util.Date

class SymptomsViewModel(private val dataRepository: DataRepository) : ViewModel() {
    fun saveSymptom(name: String, intensity: Int) {
        viewModelScope.launch {
            dataRepository.insertSymptom(Symptom(name = name, intensity = intensity, date = Date()))
        }
    }
}