package com.tiarkaerell.ibstracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tiarkaerell.ibstracker.data.model.Language
import com.tiarkaerell.ibstracker.data.model.Units
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val UNITS_KEY = stringPreferencesKey("units")
    }

    val languageFlow: Flow<Language> = context.dataStore.data
        .map { preferences ->
            val languageCode = preferences[LANGUAGE_KEY] ?: "en"
            Language.fromCode(languageCode)
        }

    val unitsFlow: Flow<Units> = context.dataStore.data
        .map { preferences ->
            val unitsName = preferences[UNITS_KEY] ?: "METRIC"
            Units.fromName(unitsName)
        }

    suspend fun setLanguage(language: Language) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.code
        }
    }

    suspend fun setUnits(units: Units) {
        context.dataStore.edit { preferences ->
            preferences[UNITS_KEY] = units.name
        }
    }
}
