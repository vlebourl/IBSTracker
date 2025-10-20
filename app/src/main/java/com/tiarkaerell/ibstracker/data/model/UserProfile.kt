package com.tiarkaerell.ibstracker.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.Period
import java.util.Date

@Serializable
data class UserProfile(
    val dateOfBirth: Long? = null, // Timestamp
    val sex: Sex = Sex.NOT_SPECIFIED,
    val heightCm: Int? = null,
    val weightKg: Float? = null,
    val activityLevel: ActivityLevel = ActivityLevel.NOT_SPECIFIED,
    val ibsDiagnosisDate: Long? = null, // When they were diagnosed with IBS
    val ibsType: IBSType = IBSType.NOT_SPECIFIED,
    val hasAllergies: Boolean = false,
    val allergyNotes: String = "", // Common allergies/intolerances
    val medicationNotes: String = "", // Current medications that might affect digestion
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun getAge(): Int? {
        return dateOfBirth?.let { dob ->
            val birthDate = java.time.Instant.ofEpochMilli(dob)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            Period.between(birthDate, LocalDate.now()).years
        }
    }
    
    fun getBMI(): Float? {
        return if (heightCm != null && weightKg != null && heightCm > 0) {
            val heightM = heightCm / 100f
            weightKg / (heightM * heightM)
        } else null
    }
    
    fun getBMICategory(): BMICategory {
        val bmi = getBMI() ?: return BMICategory.UNKNOWN
        return when {
            bmi < 18.5f -> BMICategory.UNDERWEIGHT
            bmi < 25f -> BMICategory.NORMAL
            bmi < 30f -> BMICategory.OVERWEIGHT
            else -> BMICategory.OBESE
        }
    }
    
    fun getHeightInFeetInches(): Pair<Int, Int>? {
        return heightCm?.let { cm ->
            val totalInches = (cm / 2.54).toInt()
            val feet = totalInches / 12
            val inches = totalInches % 12
            Pair(feet, inches)
        }
    }
    
    fun getWeightInPounds(): Int? {
        return weightKg?.let { kg ->
            (kg * 2.20462).toInt()
        }
    }
    
    fun getIBSDurationYears(): Int? {
        return ibsDiagnosisDate?.let { diagnosisDate ->
            val diagnosisLocalDate = java.time.Instant.ofEpochMilli(diagnosisDate)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            Period.between(diagnosisLocalDate, LocalDate.now()).years
        }
    }
    
    fun hasCompleteBasicInfo(): Boolean {
        return dateOfBirth != null && sex != Sex.NOT_SPECIFIED && 
               heightCm != null && weightKg != null
    }
}

enum class Sex(val displayNameRes: Int) {
    NOT_SPECIFIED(com.tiarkaerell.ibstracker.R.string.sex_not_specified),
    MALE(com.tiarkaerell.ibstracker.R.string.sex_male),
    FEMALE(com.tiarkaerell.ibstracker.R.string.sex_female),
    OTHER(com.tiarkaerell.ibstracker.R.string.sex_other)
}

enum class BMICategory(val displayNameRes: Int, val colorRes: Int) {
    UNKNOWN(com.tiarkaerell.ibstracker.R.string.bmi_unknown, android.R.color.darker_gray),
    UNDERWEIGHT(com.tiarkaerell.ibstracker.R.string.bmi_underweight, android.R.color.holo_blue_light),
    NORMAL(com.tiarkaerell.ibstracker.R.string.bmi_normal, android.R.color.holo_green_light),
    OVERWEIGHT(com.tiarkaerell.ibstracker.R.string.bmi_overweight, android.R.color.holo_orange_light),
    OBESE(com.tiarkaerell.ibstracker.R.string.bmi_obese, android.R.color.holo_red_light)
}

enum class ActivityLevel(val displayNameRes: Int) {
    NOT_SPECIFIED(com.tiarkaerell.ibstracker.R.string.activity_not_specified),
    SEDENTARY(com.tiarkaerell.ibstracker.R.string.activity_sedentary),
    LIGHTLY_ACTIVE(com.tiarkaerell.ibstracker.R.string.activity_lightly_active),
    MODERATELY_ACTIVE(com.tiarkaerell.ibstracker.R.string.activity_moderately_active),
    VERY_ACTIVE(com.tiarkaerell.ibstracker.R.string.activity_very_active),
    EXTREMELY_ACTIVE(com.tiarkaerell.ibstracker.R.string.activity_extremely_active)
}

enum class IBSType(val displayNameRes: Int) {
    NOT_SPECIFIED(com.tiarkaerell.ibstracker.R.string.ibs_type_not_specified),
    IBS_C(com.tiarkaerell.ibstracker.R.string.ibs_type_c), // Constipation-predominant
    IBS_D(com.tiarkaerell.ibstracker.R.string.ibs_type_d), // Diarrhea-predominant
    IBS_M(com.tiarkaerell.ibstracker.R.string.ibs_type_m), // Mixed
    IBS_U(com.tiarkaerell.ibstracker.R.string.ibs_type_u)  // Unsubtyped
}