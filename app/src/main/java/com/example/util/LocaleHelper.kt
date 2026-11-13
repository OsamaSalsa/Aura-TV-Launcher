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

    private fun applyConfigIfNeeded(resources: android.content.res.Resources?, locale: Locale) {
        if (resources == null) return
        val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!resources.configuration.locales.isEmpty) resources.configuration.locales[0] else null
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale
        }
        if (currentLocale?.language == locale.language &&
            resources.configuration.layoutDirection == android.text.TextUtils.getLayoutDirectionFromLocale(locale)
        ) {
            return
        }
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        }
        try {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        } catch (_: Exception) {}
    }

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = getLocale(languageCode)
        if (Locale.getDefault().language != locale.language) {
            Locale.setDefault(locale)
        }

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        }

        applyConfigIfNeeded(context.resources, locale)
        applyConfigIfNeeded(context.applicationContext?.resources, locale)

        return context.createConfigurationContext(config)
    }

    fun applyLocaleToActivity(activity: android.app.Activity, languageCode: String) {
        val locale = getLocale(languageCode)
        if (Locale.getDefault().language != locale.language) {
            Locale.setDefault(locale)
        }
        applyConfigIfNeeded(activity.resources, locale)
        applyConfigIfNeeded(activity.applicationContext?.resources, locale)
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

