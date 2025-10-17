package com.tiarkaerell.ibstracker.data.model

import androidx.annotation.StringRes
import com.tiarkaerell.ibstracker.R

enum class Language(val code: String, @StringRes val displayNameRes: Int) {
    ENGLISH("en", R.string.language_english),
    FRENCH("fr", R.string.language_french);

    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}
