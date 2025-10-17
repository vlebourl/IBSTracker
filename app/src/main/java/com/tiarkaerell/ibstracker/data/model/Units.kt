package com.tiarkaerell.ibstracker.data.model

import androidx.annotation.StringRes
import com.tiarkaerell.ibstracker.R

enum class Units(@StringRes val displayNameRes: Int) {
    METRIC(R.string.units_metric),
    IMPERIAL(R.string.units_imperial);

    companion object {
        fun fromName(name: String): Units {
            return entries.find { it.name == name } ?: METRIC
        }
    }
}
