package com.example.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    val SUPPORTED_LANGUAGES = setOf("ar", "en", "fr", "es", "it", "ru")

    /**
     * Resolves the effective language code.
     * Default is system language if supported in the app;
     * If system language is not supported, falls back to English ("en").
     */
    fun getEffectiveLanguage(languageCode: String): String {
        val code = languageCode.lowercase().trim()
        if (code == "ar" || code == "ar_rtl") return "ar"
        if (code in SUPPORTED_LANGUAGES) return code

        // If "system", empty, or unknown:
        val systemLang = Locale.getDefault().language.lowercase()
        return if (systemLang in SUPPORTED_LANGUAGES) {
            systemLang
        } else {
            "en" // Fallback to English if system language is not supported
        }
    }

    fun getLocale(languageCode: String): Locale {
        val effective = getEffectiveLanguage(languageCode)
        return Locale(effective)
    }

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = getLocale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        }

        try {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        } catch (_: Exception) {}

        try {
            context.applicationContext?.resources?.let { appRes ->
                @Suppress("DEPRECATION")
                appRes.updateConfiguration(config, appRes.displayMetrics)
            }
        } catch (_: Exception) {}

        return context.createConfigurationContext(config)
    }

    fun applyLocaleToActivity(activity: android.app.Activity, languageCode: String) {
        val locale = getLocale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        }

        try {
            @Suppress("DEPRECATION")
            activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        } catch (_: Exception) {}

        try {
            activity.applicationContext?.resources?.let { appRes ->
                @Suppress("DEPRECATION")
                appRes.updateConfiguration(config, appRes.displayMetrics)
            }
        } catch (_: Exception) {}
    }

    fun getAmPmString(hourOfDay: Int, languageCode: String): String {
        val effective = getEffectiveLanguage(languageCode)
        return when (effective) {
            "ar" -> {
                when (hourOfDay) {
                    in 5..11 -> "صباحاً"
                    in 12..15 -> "ظهراً"
                    in 16..18 -> "عصراً"
                    else -> "مساءً"
                }
            }
            "ru" -> {
                when (hourOfDay) {
                    in 5..11 -> "утра"
                    in 12..16 -> "дня"
                    in 17..22 -> "вечера"
                    else -> "ночи"
                }
            }
            "es" -> if (hourOfDay < 12) "a. m." else "p. m."
            else -> if (hourOfDay < 12) "AM" else "PM"
        }
    }
}

