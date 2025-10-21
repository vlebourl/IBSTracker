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

        // For API 26+, we need to create a new configuration context
        // updateConfiguration is deprecated and doesn't work reliably
        val context = activity.createConfigurationContext(config)
        activity.resources.updateConfiguration(context.resources.configuration, activity.resources.displayMetrics)
    }
}
