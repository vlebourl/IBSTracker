package com.tiarkaerell.ibstracker.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun applyLocale(activity: Activity, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)

        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }
}
