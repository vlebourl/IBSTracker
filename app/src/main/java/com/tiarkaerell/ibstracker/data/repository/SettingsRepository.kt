package com.tiarkaerell.ibstracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tiarkaerell.ibstracker.data.model.Language
import com.tiarkaerell.ibstracker.data.model.Units
import com.tiarkaerell.ibstracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val UNITS_KEY = stringPreferencesKey("units")
        private val USER_PROFILE_KEY = stringPreferencesKey("user_profile")
    }
    
    private val json = Json { ignoreUnknownKeys = true }

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

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data
        .map { preferences ->
            val profileJson = preferences[USER_PROFILE_KEY]
            if (profileJson != null) {
                try {
                    json.decodeFromString<UserProfile>(profileJson)
                } catch (e: Exception) {
                    UserProfile() // Return default profile if parsing fails
                }
            } else {
                UserProfile() // Return default profile if no data exists
            }
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

    suspend fun updateUserProfile(userProfile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[USER_PROFILE_KEY] = json.encodeToString(userProfile)
        }
    }
}
