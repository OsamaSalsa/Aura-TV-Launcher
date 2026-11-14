package com.example.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    val SUPPORTED_LANGUAGES = setOf(
        "ar", "en", "fr", "es", "it", "ru", "zh", "hi", "ko", "ja", "de",
        "pt", "tr", "id", "in", "ur", "fa", "pl", "nl", "vi", "th", "el"
    )

    /**
     * Resolves the effective language code.
     * Default is system language if supported in the app;
     * If system language is not supported, falls back to English ("en").
     */
    fun getEffectiveLanguage(languageCode: String): String {
        val code = languageCode.lowercase().trim()
        if (code == "ar" || code == "ar_rtl") return "ar"
        if (code == "id" || code == "in") return "id"
        if (code in SUPPORTED_LANGUAGES) return code

        // If "system", empty, or unknown:
        val systemLang = Locale.getDefault().language.lowercase()
        if (systemLang == "in" || systemLang == "id") return "id"
        return if (systemLang in SUPPORTED_LANGUAGES) {
            systemLang
        } else {
            "en" // Fallback to English if system language is not supported
        }
    }

    fun getLocale(languageCode: String): Locale {
        val effective = getEffectiveLanguage(languageCode)
        // Android uses legacy "in" resource qualifier for Indonesian
        return if (effective == "id") Locale("in") else Locale(effective)
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
            "fa" -> {
                when (hourOfDay) {
                    in 5..11 -> "صبح"
                    in 12..16 -> "بعدازظهر"
                    in 17..20 -> "عصر"
                    else -> "شب"
                }
            }
            "ur" -> {
                when (hourOfDay) {
                    in 5..11 -> "صبح"
                    in 12..16 -> "دوپہر"
                    in 17..20 -> "شام"
                    else -> "رات"
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
            "zh" -> if (hourOfDay < 12) "上午" else "下午"
            "ja" -> if (hourOfDay < 12) "午前" else "午後"
            "ko" -> if (hourOfDay < 12) "오전" else "오후"
            "hi" -> {
                when (hourOfDay) {
                    in 5..11 -> "सुबह"
                    in 12..16 -> "दोपहर"
                    in 17..20 -> "शाम"
                    else -> "रात"
                }
            }
            "de" -> if (hourOfDay < 12) "vorm." else "nachm."
            "el" -> if (hourOfDay < 12) "π.μ." else "μ.μ."
            "vi" -> if (hourOfDay < 12) "SA" else "CH"
            "th" -> if (hourOfDay < 12) "ก่อนเที่ยง" else "หลังเที่ยง"
            "tr" -> if (hourOfDay < 12) "ÖÖ" else "ÖS"
            else -> if (hourOfDay < 12) "AM" else "PM"
        }
    }

    fun formatDate(date: java.util.Date, style: String, languageCode: String): String {
        if (style == "hidden") return ""
        val dateLocale = getLocale(languageCode)
        val pattern = when (style) {
            "full" -> "EEEE, d MMMM"
            "full_year" -> "EEEE, d MMMM yyyy"
            "us_full" -> "EEEE, MMMM d, yyyy"
            "us_written" -> "MMMM d, yyyy"
            "us_short" -> "EEE, MMM d"
            "medium" -> "d MMMM"
            "numeric_us" -> "MM/dd/yyyy"
            "numeric" -> "dd/MM/yyyy"
            "numeric_iso" -> "yyyy-MM-dd"
            "numeric_dots" -> "dd.MM.yyyy"
            else -> "EEEE, d MMMM"
        }
        val sdf = java.text.SimpleDateFormat(pattern, dateLocale)
        val raw = sdf.format(date)
        return raw.map { ch ->
            val digit = Character.getNumericValue(ch)
            if (digit in 0..9 && ch !in '0'..'9' && ch !in 'a'..'z' && ch !in 'A'..'Z') {
                digit.toString()[0]
            } else ch
        }.joinToString("")
    }
}


