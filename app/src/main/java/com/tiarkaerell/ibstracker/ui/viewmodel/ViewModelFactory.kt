package com.tiarkaerell.ibstracker.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tiarkaerell.ibstracker.data.auth.AuthorizationManager
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.repository.AnalysisRepository
import com.tiarkaerell.ibstracker.data.repository.BackupRepository
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import com.tiarkaerell.ibstracker.data.repository.SettingsRepository
import com.tiarkaerell.ibstracker.data.preferences.FilterPreferencesManager

class ViewModelFactory(
    private val dataRepository: DataRepository,
    private val settingsRepository: SettingsRepository,
    private val analysisRepository: AnalysisRepository,
    private val backupRepository: BackupRepository,
    private val authorizationManager: AuthorizationManager,
    private val context: Context,
    private val database: AppDatabase
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
            return SettingsViewModel(settingsRepository, context, database) as T
        }
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            val filterPreferencesManager = FilterPreferencesManager(context)
            @Suppress("UNCHECKED_CAST") // Safe: type checked with isAssignableFrom() above
            return AnalyticsViewModel(analysisRepository, dataRepository, filterPreferencesManager) as T
        }
        if (modelClass.isAssignableFrom(FoodUsageStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodUsageStatsViewModel(dataRepository) as T
        }
        if (modelClass.isAssignableFrom(BackupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BackupViewModel(backupRepository, authorizationManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}