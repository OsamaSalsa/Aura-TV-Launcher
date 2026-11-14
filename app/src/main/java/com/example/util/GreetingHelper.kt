package com.example.util

object GreetingHelper {
    private val greetings = mapOf(
        0 to mapOf(
            "ar" to "ليلة هادئة وأحلام جميلة",
            "en" to "Sweet dreams and a calm night",
            "fr" to "Doux rêves et belle nuit calme",
            "ru" to "Спокойной ночи и сладких снов",
            "it" to "Sogni d'oro e notte serena",
            "es" to "Dulces sueños y noche tranquila",
            "de" to "Süße Träume und eine ruhige Nacht",
            "zh" to "祝您晚安，好梦相随",
            "ja" to "静かな夜に、素敵な夢を",
            "ko" to "평온한 밤, 좋은 꿈 꾸세요",
            "hi" to "शुभ रात्रि, मीठे सपने",
            "pt" to "Bons sonhos e uma noite tranquila",
            "tr" to "Tatlı rüyalar ve huzurlu bir gece",
            "id" to "Mimpi indah dan malam yang tenang",
            "ur" to "پرسکون رات اور سہانے خواب",
            "fa" to "شبی آرام و رویاهایی شیرین",
            "pl" to "Słodkich snów i spokojnej nocy",
            "nl" to "Zoete dromen en een rustige nacht",
            "vi" to "Chúc ngủ ngon và mơ đẹp",
            "th" to "ฝันดีและราตรีอันแสนสงบ",
            "el" to "Όνειρα γλυκά και ήρεμη νύχτα"
        ),
        1 to mapOf(
            "ar" to "نوم هانئ وغدٌ أجمل",
            "en" to "Rest well, a brighter day awaits",
            "fr" to "Reposez-vous, demain sera plus beau",
            "ru" to "Отдыхайте, завтра будет лучше",
            "it" to "Riposa bene, domani sarà più bello",
            "es" to "Descansa bien, mañana será mejor",
            "de" to "Ruhe dich gut aus, ein schöner Tag wartet",
            "zh" to "安心入眠，迎接更美好的一天",
            "ja" to "ゆっくり休んで、明るい明日へ",
            "ko" to "푹 쉬세요, 더 밝은 내일이 기다립니다",
            "hi" to "चैन की नींद सोएं, सुनहरा कल आपका इंतजार कर रहा है",
            "pt" to "Descanse bem, um dia melhor espera por si",
            "tr" to "İyi dinlenin, daha güzel bir gün sizi bekliyor",
            "id" to "Istirahatlah dengan tenang, esok yang cerah menanti",
            "ur" to "آرام کریں، ایک روشن کل آپ کا منتظر ہے",
            "fa" to "خوب استراحت کنید، فردایی روشن‌تر در راه است",
            "pl" to "Odpocznij dobrze, jutro będzie piękny dzień",
            "nl" to "Rust goed uit, er wacht een mooie dag",
            "vi" to "Nghỉ ngơi thật tốt, ngày mai tươi sáng đang chờ",
            "th" to "พักผ่อนให้เต็มที่ วันพรุ่งนี้ที่สดใสรออยู่",
            "el" to "Ξεκούραση τώρα, μια όμορφη μέρα ξημερώνει"
        ),
        2 to mapOf(
            "ar" to "سكون الليل يمنحك الراحة",
            "en" to "Peaceful night, restful sleep",
            "fr" to "Nuit paisible, sommeil réparateur",
            "ru" to "Тихая ночь и крепкий сон",
            "it" to "Notte tranquilla, sonno sereno",
            "es" to "Noche serena y sueño reparador",
            "de" to "Friedliche Nacht, erholsamer Schlaf",
            "zh" to "夜深人静，愿您安享好眠",
            "ja" to "穏やかな夜、安らかな眠りを",
            "ko" to "고요한 밤, 편안한 잠자리 되세요",
            "hi" to "शांत रात और सुकून भरी नींद",
            "pt" to "Noite serena e sono reparador",
            "tr" to "Huzurlu bir gece, dinlendirici bir uyku",
            "id" to "Malam yang damai, tidur yang nyenyak",
            "ur" to "رات کی خاموشی آپ کو سکون بخشے",
            "fa" to "آرامش شب خوابی راحت برایتان به ارمغان آورد",
            "pl" to "Spokojna noc i regenerujący sen",
            "nl" to "Vredige nacht, verkwikkende slaap",
            "vi" to "Đêm bình yên, giấc ngủ sâu",
            "th" to "ค่ำคืนที่เงียบสงบและการพักผ่อนที่แสนสบาย",
            "el" to "Γαλήνια νύχτα, ξεκούραστος ύπνος"
        ),
        3 to mapOf(
            "ar" to "فجر جديد على الأبواب",
            "en" to "A new dawn is on its way",
            "fr" to "Une nouvelle aube approche",
            "ru" to "Новый рассвет уже близко",
            "it" to "Una nuova alba è in arrivo",
            "es" to "Un nuevo amanecer se acerca",
            "de" to "Ein neuer Morgen bricht bald an",
            "zh" to "崭新的黎明即将到来",
            "ja" to "新しい夜明けが近づいています",
            "ko" to "새로운 새벽이 다가오고 있습니다",
            "hi" to "एक नई सुबह आने वाली है",
            "pt" to "Um novo amanhecer está a caminho",
            "tr" to "Yeni bir şafak yaklaşıyor",
            "id" to "Fajar baru segera menyingsing",
            "ur" to "ایک نئی سحر دروازے پر ہے",
            "fa" to "طلوعی تازه در راه است",
            "pl" to "Nowy świt jest już blisko",
            "nl" to "Een nieuwe dageraad is op komst",
            "vi" to "Bình minh mới đang đến gần",
            "th" to "รุ่งอรุณใหม่กำลังใกล้เข้ามา",
            "el" to "Μια νέα αυγή πλησιάζει"
        ),
        4 to mapOf(
            "ar" to "بداية يوم مشرق تقترب",
            "en" to "A bright day is almost here",
            "fr" to "Une belle journée se prépare",
            "ru" to "Светлый день совсем рядом",
            "it" to "Una splendida giornata si avvicina",
            "es" to "Un día brillante está por llegar",
            "de" to "Ein strahlender Tag steht bevor",
            "zh" to "光明美好的一天即将开启",
            "ja" to "明るい一日の始まりがもうすぐです",
            "ko" to "밝은 하루가 곧 시작됩니다",
            "hi" to "एक उज्जवल दिन बस शुरू होने ही वाला है",
            "pt" to "Um dia radiante está quase a chegar",
            "tr" to "Parlak bir gün başlamak üzere",
            "id" to "Hari yang cerah hampir tiba",
            "ur" to "ایک روشن دن کا آغاز قریب ہے",
            "fa" to "آغاز روزی درخشان نزدیک است",
            "pl" to "Jasny dzień już prawie nadszedł",
            "nl" to "Een stralende dag breekt bijna aan",
            "vi" to "Một ngày tươi sáng sắp bắt đầu",
            "th" to "วันใหม่ที่สดใสกำลังจะเริ่มต้น",
            "el" to "Μια φωτεινή μέρα είναι σχεδόν εδώ"
        ),
        5 to mapOf(
            "ar" to "فجر سعيد وبداية موفقة",
            "en" to "Happy dawn, great start ahead",
            "fr" to "Belle aube, beau départ",
            "ru" to "Счастливого рассвета и удачного начала",
            "it" to "Buona alba e ottimo inizio",
            "es" to "Feliz amanecer y buen comienzo",
            "de" to "Schöner Tagesanbruch, ein toller Start",
            "zh" to "晨曦微露，开启顺心的一天",
            "ja" to "素敵な夜明け、良いスタートを",
            "ko" to "행복한 새벽, 멋진 시작이 기다립니다",
            "hi" to "सुखद भोर, शानदार शुरुआत की शुभकामनाएं",
            "pt" to "Feliz alvorada e um excelente começo",
            "tr" to "Mutlu şafaklar, harika bir başlangıç olsun",
            "id" to "Fajar yang bahagia, awal yang luar biasa",
            "ur" to "خوشگوار صبح اور کامیاب آغاز",
            "fa" to "سپیده‌دم خوش و آغازی موفق",
            "pl" to "Dobrego świtu i udanego początku dnia",
            "nl" to "Mooie ochtendgloren, een geweldige start",
            "vi" to "Bình minh hạnh phúc, khởi đầu thuận lợi",
            "th" to "อรุณสวัสดิ์ เริ่มต้นวันใหม่อย่างมีความสุข",
            "el" to "Καλή αυγή και ένα υπέροχο ξεκίνημα"
        ),
        6 to mapOf(
            "ar" to "صباح النشاط والأمل",
            "en" to "Good morning, full of energy and hope",
            "fr" to "Bonjour, plein d'énergie et d'espoir",
            "ru" to "Доброе утро, заряд бодрости и надежды",
            "it" to "Buongiorno, pieno di energia e speranza",
            "es" to "Buenos días, con energía y esperanza",
            "de" to "Guten Morgen, voller Energie und Hoffnung",
            "zh" to "早安，愿您今天充满活力与希望",
            "ja" to "おはようございます、希望と活力の一日を",
            "ko" to "좋은 아침입니다, 활기차고 희망찬 하루 되세요",
            "hi" to "सुप्रभात, ऊर्जा और उम्मीदों से भरा दिन हो",
            "pt" to "Bom dia, cheio de energia e esperança",
            "tr" to "Günaydın, enerji ve umut dolu bir gün olsun",
            "id" to "Selamat pagi, penuh semangat dan harapan",
            "ur" to "صبح بخیر، توانائی اور امید سے بھرپور دن",
            "fa" to "صبح بخیر، سرشار از انرژی و امید",
            "pl" to "Dzień dobry, pełen energii i nadziei",
            "nl" to "Goedemorgen, vol energie en hoop",
            "vi" to "Chào buổi sáng tràn đầy năng lượng và hy vọng",
            "th" to "อรุณสวัสดิ์ เช้าที่เปี่ยมด้วยพลังและความหวัง",
            "el" to "Καλημέρα, γεμάτη ενέργεια και ελπίδα"
        ),
        7 to mapOf(
            "ar" to "صباح الخير وطاقة إيجابية",
            "en" to "Rise and shine, positive vibes today",
            "fr" to "Bon réveil, belle énergie aujourd'hui",
            "ru" to "С добрым утром, позитивного дня",
            "it" to "Buon risveglio, tanta positività oggi",
            "es" to "Buen despertar, mucha positividad hoy",
            "de" to "Guten Morgen, starte positiv in den Tag",
            "zh" to "早安晨光，今天也是满满正能量",
            "ja" to "おはようございます、前向きな気分で",
            "ko" to "상쾌한 아침, 오늘도 긍정적인 하루 보내세요",
            "hi" to "उठो और चमकें, आज का दिन सकारात्मक रहे",
            "pt" to "Bom dia, boas energias para hoje",
            "tr" to "Günaydın, bugün pozitif enerjiler sizinle olsun",
            "id" to "Selamat pagi, semangat positif hari ini",
            "ur" to "صبح بخیر اور مثبت توانائی",
            "fa" to "صبح بخیر و سرشار از انرژی مثبت",
            "pl" to "Dzień dobry, dużo pozytywnej energii na dziś",
            "nl" to "Goedemorgen, positieve energie vandaag",
            "vi" to "Chào buổi sáng, ngập tràn năng lượng tích cực",
            "th" to "สวัสดีตอนเช้า รับพลังบวกตลอดทั้งวัน",
            "el" to "Καλημέρα με θετική ενέργεια σήμερα"
        ),
        8 to mapOf(
            "ar" to "يوم جديد وفرص جميلة",
            "en" to "A new day, new opportunities",
            "fr" to "Un nouveau jour, de nouvelles chances",
            "ru" to "Новый день, новые возможности",
            "it" to "Un nuovo giorno, nuove opportunità",
            "es" to "Un nuevo día, nuevas oportunidades",
            "de" to "Ein neuer Tag, neue Chancen",
            "zh" to "新的一天，新的机遇在前方",
            "ja" to "新しい一日、新しいチャンス",
            "ko" to "새로운 하루, 새로운 기회가 열립니다",
            "hi" to "नया दिन, नए अवसर",
            "pt" to "Um novo dia, novas oportunidades",
            "tr" to "Yeni bir gün, yeni fırsatlar",
            "id" to "Hari baru, peluang baru",
            "ur" to "نیا دن اور خوبصورت مواقع",
            "fa" to "روزی نو و فرصت‌های زیبا",
            "pl" to "Nowy dzień, nowe możliwości",
            "nl" to "Een nieuwe dag, nieuwe kansen",
            "vi" to "Ngày mới, cơ hội mới",
            "th" to "วันใหม่และโอกาสดีๆ ที่รออยู่",
            "el" to "Μια νέα μέρα, νέες ευκαιρίες"
        ),
        9 to mapOf(
            "ar" to "صباح مليء بالإنجاز",
            "en" to "A morning full of achievement",
            "fr" to "Une matinée pleine de réussite",
            "ru" to "Утро, полное успехов",
            "it" to "Una mattina piena di successi",
            "es" to "Una mañana llena de logros",
            "de" to "Ein Morgen voller Erfolg",
            "zh" to "收获满满的美好清晨",
            "ja" to "実り豊かな朝になりますように",
            "ko" to "성취감 가득한 아침입니다",
            "hi" to "सफलता से भरी एक शानदार सुबह",
            "pt" to "Uma manhã cheia de conquistas",
            "tr" to "Başarılarla dolu bir sabah",
            "id" to "Pagi yang penuh pencapaian",
            "ur" to "کامیابیوں سے بھرپور صبح",
            "fa" to "صبحی پر از موفقیت و دستاورد",
            "pl" to "Poranek pełen sukcesów",
            "nl" to "Een ochtend vol succes",
            "vi" to "Buổi sáng tràn đầy thành công",
            "th" to "เช้าที่เต็มไปด้วยความสำเร็จ",
            "el" to "Ένα πρωινό γεμάτο επιτυχίες"
        ),
        10 to mapOf(
            "ar" to "استمتع بيومك بابتسامة",
            "en" to "Enjoy your day with a smile",
            "fr" to "Profitez de la journée avec le sourire",
            "ru" to "Наслаждайтесь днём с улыбкой",
            "it" to "Goditi la giornata con il sorriso",
            "es" to "Disfruta tu día con una sonrisa",
            "de" to "Genieße deinen Tag mit einem Lächeln",
            "zh" to "微笑面对今天，享受精彩时刻",
            "ja" to "笑顔で素敵な一日をお楽しみください",
            "ko" to "미소와 함께 즐거운 하루 보내세요",
            "hi" to "मुस्कान के साथ अपने दिन का आनंद लें",
            "pt" to "Aproveite o seu dia com um sorriso",
            "tr" to "Gününüzün tadını bir gülümsemeyle çıkarın",
            "id" to "Nikmati hari Anda dengan senyuman",
            "ur" to "مسکراہٹ کے ساتھ اپنے دن کا لطف اٹھائیں",
            "fa" to "با لبخند از روز خود لذت ببرید",
            "pl" to "Ciesz się dniem z uśmiechem",
            "nl" to "Geniet van je dag met een glimlach",
            "vi" to "Tận hưởng ngày mới với nụ cười",
            "th" to "สนุกกับวันของคุณด้วยรอยยิ้ม",
            "el" to "Απολαύστε τη μέρα σας με χαμόγελο"
        ),
        11 to mapOf(
            "ar" to "ساعة جميلة لتحقيق أهدافك",
            "en" to "A great hour to reach your goals",
            "fr" to "Une belle heure pour atteindre vos objectifs",
            "ru" to "Отличный час для достижения целей",
            "it" to "Un'ora ideale per raggiungere i tuoi obiettivi",
            "es" to "Una gran hora para lograr tus metas",
            "de" to "Eine großartige Stunde für deine Ziele",
            "zh" to "逐梦前行，美好时光正当时",
            "ja" to "目標に向かって進む素敵な時間",
            "ko" to "목표를 이루기 좋은 멋진 시간입니다",
            "hi" to "अपने लक्ष्यों को पाने का बेहतरीन समय",
            "pt" to "Uma ótima hora para alcançar os seus objetivos",
            "tr" to "Hedeflerinize ulaşmak için harika bir saat",
            "id" to "Waktu yang tepat untuk meraih tujuan Anda",
            "ur" to "اپنے مقاصد حاصل کرنے کا بہترین وقت",
            "fa" to "ساعتی عالی برای رسیدن به اهدافتان",
            "pl" to "Świetna pora na realizację celów",
            "nl" to "Een geweldig moment om je doelen te bereiken",
            "vi" to "Thời điểm tuyệt vời để đạt mục tiêu",
            "th" to "เวลาดีๆ ในการพิชิตเป้าหมายของคุณ",
            "el" to "Μια υπέροχη ώρα για να πετύχετε τους στόχους σας"
        ),
        12 to mapOf(
            "ar" to "ظهيرة سعيدة ومشرقة",
            "en" to "Happy, sunny noon",
            "fr" to "Bon midi lumineux",
            "ru" to "Солнечного и радостного полудня",
            "it" to "Buon pranzo e splendida giornata",
            "es" to "Feliz y soleado mediodía",
            "de" to "Schöner, sonniger Mittag",
            "zh" to "午安，祝您度过阳光明媚的中午",
            "ja" to "明るく楽しい昼下がりを",
            "ko" to "햇살 가득한 행복한 정오입니다",
            "hi" to "खुशहाल और सुनहरी दोपहर",
            "pt" to "Um meio-dia feliz e iluminado",
            "tr" to "Mutlu ve aydınlık bir öğle vakti",
            "id" to "Selamat siang yang cerah dan menyenangkan",
            "ur" to "خوشگوار اور روشن دوپہر",
            "fa" to "ظهری شاد و درخشان داشته باشید",
            "pl" to "Miłego i słonecznego południa",
            "nl" to "Fijne en zonnige middag",
            "vi" to "Buổi trưa vui vẻ và rạng rỡ",
            "th" to "สวัสดีตอนเที่ยงที่สดใสและมีความสุข",
            "el" to "Χαρούμενο και φωτεινό μεσημέρι"
        ),
        13 to mapOf(
            "ar" to "وقت رائع لاستراحة لطيفة",
            "en" to "A lovely time for a relaxing break",
            "fr" to "Un joli moment pour une pause détente",
            "ru" to "Время для приятного отдыха",
            "it" to "Un bel momento per una pausa rilassante",
            "es" to "Buen momento para una pausa relajante",
            "de" to "Eine schöne Zeit für eine entspannte Pause",
            "zh" to "小憩片刻，享受悠闲午后",
            "ja" to "リラックスした休憩にぴったりの時間",
            "ko" to "잠시 쉬어가며 여유를 즐기기 좋은 시간",
            "hi" to "आराम और सुकून के लिए बेहतरीन समय",
            "pt" to "Um ótimo momento para uma pausa relaxante",
            "tr" to "Keyifli bir mola için harika bir zaman",
            "id" to "Waktu yang menyenangkan untuk istirahat sejenak",
            "ur" to "خوشگوار وقفے کے لیے بہترین وقت",
            "fa" to "زمانی عالی برای یک استراحت دلنشین",
            "pl" to "Doskonały czas na chwilę relaksu",
            "nl" to "Een heerlijk moment voor een ontspannen pauze",
            "vi" to "Khoảng thời gian tuyệt vời để nghỉ ngơi",
            "th" to "ช่วงเวลาดีๆ สำหรับการพักผ่อนสบายๆ",
            "el" to "Ιδανική ώρα για ένα χαλαρωτικό διάλειμμα"
        ),
        14 to mapOf(
            "ar" to "ظهر هانئ وطاقة متجددة",
            "en" to "Easy afternoon, renewed energy",
            "fr" to "Après-midi tranquille, énergie renouvelée",
            "ru" to "Спокойный день и свежие силы",
            "it" to "Pomeriggio tranquillo, energie nuove",
            "es" to "Tarde tranquila, energía renovada",
            "de" to "Ruhiger Nachmittag, frische Energie",
            "zh" to "惬意午后，元气满满再出发",
            "ja" to "穏やかな午後、新たなエネルギーを",
            "ko" to "여유로운 오후, 새로운 활력을 채워보세요",
            "hi" to "शांत दोपहर और नई ऊर्जा का संचार",
            "pt" to "Tarde tranquila e energias renovadas",
            "tr" to "Huzurlu bir öğleden sonra, yenilenmiş enerji",
            "id" to "Siang yang tenang, energi yang diperbarui",
            "ur" to "پرسکون دوپہر اور تازہ دم توانائی",
            "fa" to "بعدازظهری آرام با انرژی تازه",
            "pl" to "Spokojne popołudnie i nowa energia",
            "nl" to "Rustige middag, nieuwe energie",
            "vi" to "Buổi chiều thư thái, tràn đầy năng lượng mới",
            "th" to "ยามบ่ายที่ผ่อนคลายพร้อมเติมพลังใหม่",
            "el" to "Ήρεμο μεσημέρι με ανανεωμένη ενέργεια"
        ),
        15 to mapOf(
            "ar" to "عصر جميل وأفكار مشرقة",
            "en" to "Good afternoon, bright ideas ahead",
            "fr" to "Bon après-midi, belles idées en vue",
            "ru" to "Добрый день, светлых идей",
            "it" to "Buon pomeriggio, idee luminose",
            "es" to "Buenas tardes, ideas brillantes",
            "de" to "Guten Nachmittag, helle Ideen voraus",
            "zh" to "下午好，灵感与精彩常伴左右",
            "ja" to "こんにちは、ひらめきあふれる午後を",
            "ko" to "좋은 오후입니다, 반짝이는 아이디어가 가득하길",
            "hi" to "शुभ दोपहर, शानदार विचारों के साथ आगे बढ़ें",
            "pt" to "Boa tarde, ideias brilhantes pela frente",
            "tr" to "İyi günler, parlak fikirler sizinle olsun",
            "id" to "Selamat sore, ide-ide cemerlang menanti",
            "ur" to "سہ پہر بخیر اور روشن خیالات",
            "fa" to "عصر بخیر و سرشار از ایده‌های روشن",
            "pl" to "Dobrego popołudnia i świetnych pomysłów",
            "nl" to "Goedemiddag, mooie ideeën in het verschiet",
            "vi" to "Chào buổi chiều, ngập tràn ý tưởng sáng tạo",
            "th" to "สวัสดีตอนบ่าย ขอให้เต็มไปด้วยไอเดียที่สดใส",
            "el" to "Καλό απόγευμα με λαμπρές ιδέες"
        ),
        16 to mapOf(
            "ar" to "وقت ممتاز لتجديد النشاط",
            "en" to "A perfect time to recharge",
            "fr" to "Le moment idéal pour se ressourcer",
            "ru" to "Лучшее время набраться сил",
            "it" to "Il momento perfetto per ricaricarsi",
            "es" to "El momento ideal para recargar energías",
            "de" to "Die perfekte Zeit zum Auftanken",
            "zh" to "正是放松充电的好时光",
            "ja" to "リフレッシュするのに最適な時間です",
            "ko" to "재충전하기에 완벽한 시간입니다",
            "hi" to "ताज़गी और ऊर्जा भरने का सही समय",
            "pt" to "O momento perfeito para recarregar energias",
            "tr" to "Enerji toplamak için mükemmel bir زمان",
            "id" to "Waktu yang sempurna untuk mengisi ulang energi",
            "ur" to "تازہ دم ہونے کا بہترین وقت",
            "fa" to "بهترین زمان برای تجدید قوا",
            "pl" to "Idealna pora na naładowanie baterii",
            "nl" to "Het perfecte moment om weer op te laden",
            "vi" to "Thời điểm hoàn hảo để nạp lại năng lượng",
            "th" to "เวลาที่เหมาะที่สุดในการชาร์จพลัง",
            "el" to "Η τέλεια στιγμή για αναζωογόνηση"
        ),
        17 to mapOf(
            "ar" to "المساء قريب وأجواء جميلة",
            "en" to "Evening is near, enjoy the moment",
            "fr" to "Le soir approche, savourez l'instant",
            "ru" to "Вечер близко, цените момент",
            "it" to "La sera si avvicina, goditi l'attimo",
            "es" to "La noche se acerca, disfruta el momento",
            "de" to "Der Abend naht, genieße den Moment",
            "zh" to "傍晚将至，尽情享受惬意时光",
            "ja" to "夕暮れが近づいています、今この瞬間を楽しんで",
            "ko" to "저녁이 다가옵니다, 이 순간을 즐겨보세요",
            "hi" to "शाम करीब है, इस खूबसूरत पल का आनंद लें",
            "pt" to "O entardecer aproxima-se, aproveite o momento",
            "tr" to "Akşam yaklaşıyor, anın tadını çıkarın",
            "id" to "Senja mendekat, nikmati setiap momennya",
            "ur" to "شام قریب ہے اور ماحول خوشگوار ہے",
            "fa" to "غروب نزدیک است، از لحظه لذت ببرید",
            "pl" to "Wieczór blisko, ciesz się chwilą",
            "nl" to "De avond nadert, geniet van het moment",
            "vi" to "Hoàng hôn đang xuống, hãy tận hưởng khoảnh khắc",
            "th" to "ใกล้ค่ำแล้ว ดื่มด่ำกับบรรยากาศดีๆ",
            "el" to "Το βράδυ πλησιάζει, απολαύστε τη στιγμή"
        ),
        18 to mapOf(
            "ar" to "مساء الخير والسرور",
            "en" to "Good evening and good cheer",
            "fr" to "Bonsoir et belle soirée",
            "ru" to "Добрый вечер и хорошего настроения",
            "it" to "Buonasera e tanta allegria",
            "es" to "Buena tarde y mucha alegría",
            "de" to "Guten Abend und viel Freude",
            "zh" to "晚上好，祝您心情愉悦",
            "ja" to "こんばんは、楽しく穏やかな夜を",
            "ko" to "안녕하세요, 즐거운 저녁 시간 보내세요",
            "hi" to "शुभ संध्या, आपका शाम खुशनुमा हो",
            "pt" to "Boa noite e muita alegria",
            "tr" to "İyi akşamlar ve bol neşe",
            "id" to "Selamat malam dan penuh keceriaan",
            "ur" to "شام بخیر اور خوشیاں آپ کے نام",
            "fa" to "عصر بخیر و شادی همراهتان باد",
            "pl" to "Dobry wieczór i dużo радоści",
            "nl" to "Goedenavond en veel gezelligheid",
            "vi" to "Chào buổi tối vui vẻ và ấm áp",
            "th" to "สวัสดีตอนเย็น ขอให้เต็มไปด้วยความสุข",
            "el" to "Καλησπέρα με χαρά και καλή διάθεση"
        ),
        19 to mapOf(
            "ar" to "مساء دافئ وأوقات سعيدة",
            "en" to "A warm evening, happy moments",
            "fr" to "Soirée chaleureuse, moments heureux",
            "ru" to "Тёплый вечер и счастливые мгновения",
            "it" to "Serata calda e momenti felici",
            "es" to "Una tarde cálida y momentos felices",
            "de" to "Ein warmer Abend, glückliche Momente",
            "zh" to "温馨夜色，欢乐时光相伴",
            "ja" to "温かな夕べ、幸せなひとときを",
            "ko" to "따뜻한 저녁, 행복한 시간 되세요",
            "hi" to "गर्मजोशी भरी शाम और खुशियों के पल",
            "pt" to "Uma noite acolhedora e momentos felizes",
            "tr" to "Sıcak bir akşam, mutlu anlar",
            "id" to "Malam yang hangat, momen yang membahagiakan",
            "ur" to "خوشگوار شام اور مسرت بھرے لمحات",
            "fa" to "شبی گرم و لحظاتی شاد",
            "pl" to "Ciepły wieczór i szczęśliwe chwile",
            "nl" to "Een warme avond, gelukkige momenten",
            "vi" to "Buổi tối ấm áp, khoảnh khắc hạnh phúc",
            "th" to "ค่ำคืนที่อบอุ่นและช่วงเวลาแห่งความสุข",
            "el" to "Μια ζεστή βραδιά με όμορφες στιγμές"
        ),
        20 to mapOf(
            "ar" to "ليلة ممتعة بانتظارك",
            "en" to "An enjoyable night awaits you",
            "fr" to "Une agréable soirée vous attend",
            "ru" to "Приятного вечера вам",
            "it" to "Una piacevole serata ti aspetta",
            "es" to "Que tengas una noche agradable",
            "de" to "Ein genussvoller Abend erwartet dich",
            "zh" to "精彩的夜晚正在等待着您",
            "ja" to "楽しい夜があなたを待っています",
            "ko" to "즐거운 밤이 기다리고 있습니다",
            "hi" to "एक शानदार रात आपका इंतजार कर रही है",
            "pt" to "Uma noite agradável espera por si",
            "tr" to "Keyifli bir gece sizi bekliyor",
            "id" to "Malam yang menyenangkan menanti Anda",
            "ur" to "ایک شاندار رات آپ کی منتظر ہے",
            "fa" to "شبی دلپذیر در انتظار شماست",
            "pl" to "Miłego wieczoru przed Tobą",
            "nl" to "Een fijne avond wacht op je",
            "vi" to "Một buổi tối thú vị đang chờ đón bạn",
            "th" to "ค่ำคืนที่แสนเพลิดเพลินรอคุณอยู่",
            "el" to "Μια απολαυστική βραδιά σας περιμένει"
        ),
        21 to mapOf(
            "ar" to "أمسية هادئة ولحظات لطيفة",
            "en" to "A calm evening, pleasant moments",
            "fr" to "Soirée calme, instants doux",
            "ru" to "Тихий вечер и добрые мгновения",
            "it" to "Serata calma e momenti dolci",
            "es" to "Noche tranquila y momentos bonitos",
            "de" to "Ein ruhiger Abend, angenehme Momente",
            "zh" to "宁静夜晚，尽享轻松与温馨",
            "ja" to "落ち着いた夜、心地よいひとときを",
            "ko" to "차분한 저녁, 기분 좋은 시간 보내세요",
            "hi" to "शांत शाम और सुहावने पल",
            "pt" to "Uma noite calma e momentos agradáveis",
            "tr" to "Sakin bir akşam, hoş dakikalar",
            "id" to "Malam yang tenang, saat-saat yang indah",
            "ur" to "پرسکون شام اور خوبصورت لمحات",
            "fa" to "شبی آرام و لحظاتی دلنشین",
            "pl" to "Spokojny wieczór i miłe chwile",
            "nl" to "Een rustige avond, fijne momenten",
            "vi" to "Buổi tối yên bình, những phút giây dễ chịu",
            "th" to "ค่ำคืนที่สงบและช่วงเวลาที่แสนดี",
            "el" to "Ήρεμο βράδυ και ευχάριστες στιγμές"
        ),
        22 to mapOf(
            "ar" to "وقت مثالي للاسترخاء",
            "en" to "The perfect time to unwind",
            "fr" to "Le moment idéal pour se détendre",
            "ru" to "Время расслабиться",
            "it" to "Il momento perfetto per rilassarsi",
            "es" to "Hora perfecta para relajarse",
            "de" to "Die perfekte Zeit zum Entspannen",
            "zh" to "放松身心的绝佳时刻",
            "ja" to "くつろぐのにぴったりの時間",
            "ko" to "하루의 피로를 풀기 완벽한 시간입니다",
            "hi" to "आराम करने का सबसे अच्छा समय",
            "pt" to "O momento ideal para relaxar",
            "tr" to "Dinlenmek için mükemmel bir zaman",
            "id" to "Waktu terbaik untuk bersantai",
            "ur" to "آرام اور سکون کا بہترین وقت",
            "fa" to "بهترین زمان برای آرامش و استراحت",
            "pl" to "Idealna chwila na odpoczynek",
            "nl" to "Het ideale moment om te ontspannen",
            "vi" to "Thời gian lý tưởng để thư giãn",
            "th" to "เวลาที่เหมาะที่สุดสำหรับการพักผ่อน",
            "el" to "Η ιδανική ώρα για χαλάρωση"
        ),
        23 to mapOf(
            "ar" to "تصبح على خير وأحلام سعيدة",
            "en" to "Good night and sweet dreams",
            "fr" to "Bonne nuit et beaux rêves",
            "ru" to "Спокойной ночи и добрых снов",
            "it" to "Buonanotte e sogni felici",
            "es" to "Buenas noches y felices sueños",
            "de" to "Gute Nacht und süße Träume",
            "zh" to "晚安，祝您做个甜美的梦",
            "ja" to "おやすみなさい、良い夢を",
            "ko" to "안녕히 주무세요, 달콤한 꿈 꾸세요",
            "hi" to "शुभ रात्रि और मीठे सपने",
            "pt" to "Boa noite e bons sonhos",
            "tr" to "İyi geceler ve tatlı rüyalar",
            "id" to "Selamat malam dan mimpi indah",
            "ur" to "شب بخیر اور سہانے خواب",
            "fa" to "شب بخیر و خواب‌های خوش",
            "pl" to "Dobranoc i kolorowych snów",
            "nl" to "Welterusten en zoete dromen",
            "vi" to "Chúc ngủ ngon và những giấc mơ đẹp",
            "th" to "ราตรีสวัสดิ์และฝันดี",
            "el" to "Καληνύχτα και όνειρα γλυκά"
        )
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
                "de" -> "Guten Morgen"
                "zh" -> "早上好"
                "ja" -> "おはようございます"
                "ko" -> "좋은 아침입니다"
                "hi" -> "सुप्रभात"
                "pt" -> "Bom dia"
                "tr" -> "Günaydın"
                "id" -> "Selamat pagi"
                "ur" -> "صبح بخیر"
                "fa" -> "صبح بخیر"
                "pl" -> "Dzień dobry"
                "nl" -> "Goedemorgen"
                "vi" -> "Chào buổi sáng"
                "th" -> "อรุณสวัสดิ์"
                "el" -> "Καλημέρα"
                else -> "Good morning"
            }
            1 -> when (lang) {
                "ar" -> "طاب يومك"
                "es" -> "Buenas tardes"
                "fr" -> "Bon après-midi"
                "it" -> "Buon pomeriggio"
                "ru" -> "Добрый день"
                "de" -> "Guten Tag"
                "zh" -> "下午好"
                "ja" -> "こんにちは"
                "ko" -> "좋은 오후입니다"
                "hi" -> "नमस्कार"
                "pt" -> "Boa tarde"
                "tr" -> "İyi günler"
                "id" -> "Selamat siang"
                "ur" -> "دوپہر بخیر"
                "fa" -> "روز بخیر"
                "pl" -> "Dzień dobry"
                "nl" -> "Goedemiddag"
                "vi" -> "Chào buổi chiều"
                "th" -> "สวัสดีตอนบ่าย"
                "el" -> "Καλό απόγευμα"
                else -> "Good afternoon"
            }
            2 -> when (lang) {
                "ar" -> "مساء الخير"
                "es" -> "Buenas noches"
                "fr" -> "Bonsoir"
                "it" -> "Buonasera"
                "ru" -> "Добрый вечер"
                "de" -> "Guten Abend"
                "zh" -> "晚上好"
                "ja" -> "こんばんは"
                "ko" -> "좋은 저녁입니다"
                "hi" -> "शुभ संध्या"
                "pt" -> "Boa noite"
                "tr" -> "İyi akşamlar"
                "id" -> "Selamat sore"
                "ur" -> "شام بخیر"
                "fa" -> "عصر بخیر"
                "pl" -> "Dobry wieczór"
                "nl" -> "Goedenavond"
                "vi" -> "Chào buổi tối"
                "th" -> "สวัสดีตอนเย็น"
                "el" -> "Καλησπέρα"
                else -> "Good evening"
            }
            else -> when (lang) {
                "ar" -> "تصبح على خير"
                "es" -> "Buenas noches"
                "fr" -> "Bonne nuit"
                "it" -> "Buonanotte"
                "ru" -> "Спокойной ночи"
                "de" -> "Gute Nacht"
                "zh" -> "晚安"
                "ja" -> "おやすみなさい"
                "ko" -> "안녕히 주무세요"
                "hi" -> "शुभ रात्रि"
                "pt" -> "Boa noite"
                "tr" -> "İyi geceler"
                "id" -> "Selamat malam"
                "ur" -> "شب بخیر"
                "fa" -> "شب بخیر"
                "pl" -> "Dobranoc"
                "nl" -> "Goedenacht"
                "vi" -> "Chúc ngủ ngon"
                "th" -> "ราตรีสวัสดิ์"
                "el" -> "Καληνύχτα"
                else -> "Good night"
            }
        }
    }

    fun getGreeting(hour: Int, languageCode: String, mode: String = "poetic", customText: String = ""): String {
        if (mode == "hidden") return ""
        if (mode == "custom" && customText.isNotBlank()) return customText
        val lang = LocaleHelper.getEffectiveLanguage(languageCode)
        if (mode == "classic") {
            return getClassicGreeting(hour, lang)
        }
        val hourGreetings = greetings[hour % 24] ?: greetings[12]!!
        return hourGreetings[lang] ?: hourGreetings["en"]!!
    }
}
