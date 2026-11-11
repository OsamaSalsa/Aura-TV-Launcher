package com.example.util

object GreetingHelper {
    private val greetings = mapOf(
        0 to mapOf("ar" to "ليلة هادئة وأحلام جميلة", "en" to "Sweet dreams and a calm night", "fr" to "Doux rêves et belle nuit calme", "ru" to "Спокойной ночи и сладких снов", "it" to "Sogni d'oro e notte serena", "es" to "Dulces sueños y noche tranquila"),
        1 to mapOf("ar" to "نوم هانئ وغدٌ أجمل", "en" to "Rest well, a brighter day awaits", "fr" to "Reposez-vous, demain sera plus beau", "ru" to "Отдыхайте, завтра будет лучше", "it" to "Riposa bene, domani sarà più bello", "es" to "Descansa bien, mañana será mejor"),
        2 to mapOf("ar" to "سكون الليل يمنحك الراحة", "en" to "Peaceful night, restful sleep", "fr" to "Nuit paisible, sommeil réparateur", "ru" to "Тихая ночь и крепкий сон", "it" to "Notte tranquilla, sonno sereno", "es" to "Noche serena y sueño reparador"),
        3 to mapOf("ar" to "فجر جديد على الأبواب", "en" to "A new dawn is on its way", "fr" to "Une nouvelle aube approche", "ru" to "Новый рассвет уже близко", "it" to "Una nuova alba è in arrivo", "es" to "Un nuevo amanecer se acerca"),
        4 to mapOf("ar" to "بداية يوم مشرق تقترب", "en" to "A bright day is almost here", "fr" to "Une belle journée se prépare", "ru" to "Светлый день совсем рядом", "it" to "Una splendida giornata si avvicina", "es" to "Un día brillante está por llegar"),
        5 to mapOf("ar" to "فجر سعيد وبداية موفقة", "en" to "Happy dawn, great start ahead", "fr" to "Belle aube, beau départ", "ru" to "Счастливого рассвета и удачного начала", "it" to "Buona alba e ottimo inizio", "es" to "Feliz amanecer y buen comienzo"),
        6 to mapOf("ar" to "صباح النشاط والأمل", "en" to "Good morning, full of energy and hope", "fr" to "Bonjour, plein d'énergie et d'espoir", "ru" to "Доброе утро, заряд бодрости и надежды", "it" to "Buongiorno, pieno di energia e speranza", "es" to "Buenos días, con energía y esperanza"),
        7 to mapOf("ar" to "صباح الخير وطاقة إيجابية", "en" to "Rise and shine, positive vibes today", "fr" to "Bon réveil, belle énergie aujourd'hui", "ru" to "С добрым утром, позитивного дня", "it" to "Buon risveglio, tanta positività oggi", "es" to "Buen despertar, mucha positividad hoy"),
        8 to mapOf("ar" to "يوم جديد وفرص جميلة", "en" to "A new day, new opportunities", "fr" to "Un nouveau jour, de nouvelles chances", "ru" to "Новый день, новые возможности", "it" to "Un nuovo giorno, nuove opportunità", "es" to "Un nuevo día, nuevas oportunidades"),
        9 to mapOf("ar" to "صباح مليء بالإنجاز", "en" to "A morning full of achievement", "fr" to "Une matinée pleine de réussite", "ru" to "Утро, полное успехов", "it" to "Una mattina piena di successi", "es" to "Una mañana llena de logros"),
        10 to mapOf("ar" to "استمتع بيومك بابتسامة", "en" to "Enjoy your day with a smile", "fr" to "Profitez de la journée avec le sourire", "ru" to "Наслаждайтесь днём с улыбкой", "it" to "Goditi la giornata con il sorriso", "es" to "Disfruta tu día con una sonrisa"),
        11 to mapOf("ar" to "ساعة جميلة لتحقيق أهدافك", "en" to "A great hour to reach your goals", "fr" to "Une belle heure pour atteindre vos objectifs", "ru" to "Отличный час для достижения целей", "it" to "Un'ora ideale per raggiungere i tuoi obiettivi", "es" to "Una gran hora para lograr tus metas"),
        12 to mapOf("ar" to "ظهيرة سعيدة ومشرقة", "en" to "Happy, sunny noon", "fr" to "Bon midi lumineux", "ru" to "Солнечного и радостного полудня", "it" to "Buon pranzo e splendida giornata", "es" to "Feliz y soleado mediodía"),
        13 to mapOf("ar" to "وقت رائع لاستراحة لطيفة", "en" to "A lovely time for a relaxing break", "fr" to "Un joli moment pour une pause détente", "ru" to "Время для приятного отдыха", "it" to "Un bel momento per una pausa rilassante", "es" to "Buen momento para una pausa relajante"),
        14 to mapOf("ar" to "ظهر هانئ وطاقة متجددة", "en" to "Easy afternoon, renewed energy", "fr" to "Après-midi tranquille, énergie renouvelée", "ru" to "Спокойный день и свежие силы", "it" to "Pomeriggio tranquillo, energie nuove", "es" to "Tarde tranquila, energía renovada"),
        15 to mapOf("ar" to "عصر جميل وأفكار مشرقة", "en" to "Good afternoon, bright ideas ahead", "fr" to "Bon après-midi, belles idées en vue", "ru" to "Добрый день, светлых идей", "it" to "Buon pomeriggio, idee luminose", "es" to "Buenas tardes, ideas brillantes"),
        16 to mapOf("ar" to "وقت ممتاز لتجديد النشاط", "en" to "A perfect time to recharge", "fr" to "Le moment idéal pour se ressourcer", "ru" to "Лучшее время набраться сил", "it" to "Il momento perfetto per ricaricarsi", "es" to "El momento ideal para recargar energías"),
        17 to mapOf("ar" to "المساء قريب وأجواء جميلة", "en" to "Evening is near, enjoy the moment", "fr" to "Le soir approche, savourez l'instant", "ru" to "Вечер близко, цените момент", "it" to "La sera si avvicina, goditi l'attimo", "es" to "La noche se acerca, disfruta el momento"),
        18 to mapOf("ar" to "مساء الخير والسرور", "en" to "Good evening and good cheer", "fr" to "Bonsoir et belle soirée", "ru" to "Добрый вечер и хорошего настроения", "it" to "Buonasera e tanta allegria", "es" to "Buena tarde y mucha alegría"),
        19 to mapOf("ar" to "مساء دافئ وأوقات سعيدة", "en" to "A warm evening, happy moments", "fr" to "Soirée chaleureuse, moments heureux", "ru" to "Тёплый вечер и счастливые мгновения", "it" to "Serata calda e momenti felici", "es" to "Una tarde cálida y momentos felices"),
        20 to mapOf("ar" to "ليلة ممتعة بانتظارك", "en" to "An enjoyable night awaits you", "fr" to "Une agréable soirée vous attend", "ru" to "Приятного вечера вам", "it" to "Una piacevole serata ti aspetta", "es" to "Que tengas una noche agradable"),
        21 to mapOf("ar" to "أمسية هادئة ولحظات لطيفة", "en" to "A calm evening, pleasant moments", "fr" to "Soirée calme, instants doux", "ru" to "Тихий вечер и добрые мгновения", "it" to "Serata calma e momenti dolci", "es" to "Noche tranquila y momentos bonitos"),
        22 to mapOf("ar" to "وقت مثالي للاسترخاء", "en" to "The perfect time to unwind", "fr" to "Le moment idéal pour se détendre", "ru" to "Время расслабиться", "it" to "Il momento perfetto per rilassarsi", "es" to "Hora perfecta para relajarse"),
        23 to mapOf("ar" to "تصبح على خير وأحلام سعيدة", "en" to "Good night and sweet dreams", "fr" to "Bonne nuit et beaux rêves", "ru" to "Спокойной ночи и добрых снов", "it" to "Buonanotte e sogni felici", "es" to "Buenas noches y felices sueños")
    )

    private fun getClassicGreeting(hour: Int, lang: String): String {
        val period = when (hour % 24) {
            in 5..11 -> 0 // Morning
            in 12..17 -> 1 // Afternoon
            in 18..22 -> 2 // Evening
            else -> 3 // Night
        }
        return when (period) {
            0 -> when (lang) {
                "ar" -> "صباح الخير"
                "es" -> "Buenos días"
                "fr" -> "Bonjour"
                "it" -> "Buongiorno"
                "ru" -> "Доброе утро"
                else -> "Good morning"
            }
            1 -> when (lang) {
                "ar" -> "طاب يومك"
                "es" -> "Buenas tardes"
                "fr" -> "Bon après-midi"
                "it" -> "Buon pomeriggio"
                "ru" -> "Добрый день"
                else -> "Good afternoon"
            }
            2 -> when (lang) {
                "ar" -> "مساء الخير"
                "es" -> "Buenas noches"
                "fr" -> "Bonsoir"
                "it" -> "Buonasera"
                "ru" -> "Добрый вечер"
                else -> "Good evening"
            }
            else -> when (lang) {
                "ar" -> "تصبح على خير"
                "es" -> "Buenas noches"
                "fr" -> "Bonne nuit"
                "it" -> "Buonanotte"
                "ru" -> "Спокойной ночи"
                else -> "Good night"
            }
        }
    }

    fun getGreeting(hour: Int, languageCode: String, mode: String = "poetic", customText: String = ""): String {
        if (mode == "hidden") return ""
        if (mode == "custom" && customText.isNotBlank()) return customText
        val lang = when (languageCode) {
            "system" -> "en"
            "ar_rtl" -> "ar"
            else -> languageCode
        }
        if (mode == "classic") {
            return getClassicGreeting(hour, lang)
        }
        val hourGreetings = greetings[hour % 24] ?: greetings[12]!!
        return hourGreetings[lang] ?: hourGreetings["en"]!!
    }
}
