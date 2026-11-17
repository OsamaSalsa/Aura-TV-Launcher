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

    fun isRtlLanguage(languageCode: String): Boolean {
        val code = languageCode.lowercase().trim()
        val effective = getEffectiveLanguage(code)
        return code == "ar_rtl" ||
            effective in setOf("ur", "fa") ||
            (code == "system" && effective in setOf("ar", "ur", "fa"))
    }

    private fun applyConfigIfNeeded(
        resources: android.content.res.Resources?,
        locale: Locale,
        useRtlDirection: Boolean
    ) {
        if (resources == null) return
        val desiredDirection = if (useRtlDirection) {
            android.util.LayoutDirection.RTL
        } else {
            android.util.LayoutDirection.LTR
        }
        val currentLocale = if (!resources.configuration.locales.isEmpty) resources.configuration.locales[0] else null
        if (currentLocale?.language == locale.language &&
            resources.configuration.layoutDirection == desiredDirection
        ) {
            return
        }
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(if (useRtlDirection) locale else Locale.ENGLISH)
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)
        try {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        } catch (_: Exception) {}
    }

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = getLocale(languageCode)
        val useRtl = isRtlLanguage(languageCode)
        if (Locale.getDefault().language != locale.language) {
            Locale.setDefault(locale)
        }

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(if (useRtl) locale else Locale.ENGLISH)

        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)

        return context.createConfigurationContext(config)
    }

    fun applyLocaleToActivity(activity: android.app.Activity, languageCode: String) {
        val locale = getLocale(languageCode)
        val useRtl = isRtlLanguage(languageCode)
        if (Locale.getDefault().language != locale.language) {
            Locale.setDefault(locale)
        }
        applyConfigIfNeeded(activity.applicationContext?.resources, locale, useRtl)
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

    fun isArabicOrRtlLanguage(languageCode: String): Boolean {
        val effective = getEffectiveLanguage(languageCode)
        return effective == "ar" || effective == "ur" || effective == "fa"
    }

    fun containsArabicScript(text: String): Boolean {
        for (ch in text) {
            val cp = ch.code
            if (cp in 0x0600..0x06FF ||
                cp in 0x0750..0x077F ||
                cp in 0x08A0..0x08FF ||
                cp in 0xFB50..0xFDFF ||
                cp in 0xFE70..0xFEFF
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Strips invisible ICU Bidi control marks (such as U+061C Arabic Letter Mark, U+200E, U+200F)
     * and normalizes U+202F (Narrow No-Break Space) to standard ASCII space so Android's Minikin
     * font shaper never falls back from Cairo.ttf to the system default Arabic font.
     */
    fun sanitizeForCairoFont(text: String): String {
        if (text.isEmpty()) return text
        val sb = StringBuilder(text.length)
        for (ch in text) {
            when (ch.code) {
                0x061C, // Arabic Letter Mark (missing from Cairo.ttf cmap -> triggers system font fallback!)
                0x200B, 0x200C, 0x200D, 0x200E, 0x200F,
                0x202A, 0x202B, 0x202C, 0x202D, 0x202E,
                0x2066, 0x2067, 0x2068, 0x2069,
                0xFEFF -> {
                    // Skip invisible directional marks
                }
                0x00A0, 0x202F -> sb.append(' ')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
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
        val raw = sanitizeForCairoFont(sdf.format(date))
        return raw.map { ch ->
            val digit = Character.getNumericValue(ch)
            if (digit in 0..9 && ch !in '0'..'9' && ch !in 'a'..'z' && ch !in 'A'..'Z') {
                digit.toString()[0]
            } else ch
        }.joinToString("")
    }
}


