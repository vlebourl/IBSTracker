package com.tiarkaerell.ibstracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.model.Symptom
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class SymptomsViewModel(private val dataRepository: DataRepository) : ViewModel() {

    val symptoms: StateFlow<List<Symptom>> = dataRepository.getAllSymptoms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveSymptom(name: String, intensity: Int, date: Date = Date()) {
        viewModelScope.launch {
            dataRepository.insertSymptom(Symptom(name = name, intensity = intensity, date = date))
        }
    }

    fun updateSymptom(symptom: Symptom) {
        viewModelScope.launch {
            dataRepository.updateSymptom(symptom)
        }
    }

    fun deleteSymptom(symptom: Symptom) {
        viewModelScope.launch {
            dataRepository.deleteSymptom(symptom)
        }
    }
}