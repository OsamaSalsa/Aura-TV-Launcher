[English](README.md) | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | **Italiano** | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 📺 Aura TV Launcher

![Schermata Principale](docs/screenshots/home.png)

**Un launcher e media center per Android TV moderno, ultra-veloce, leggero, completamente personalizzabile e al 100% senza pubblicità sviluppato con Jetpack Compose.**  
*La soluzione All-in-One che sostituisce dozzine di app separate sul tuo televisore.*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 La soluzione All-in-One

La maggior parte dei launcher TV attuali è piena di annunci forzati, pesanti anteprime video e menu lenti.

**Aura TV Launcher** è concepito per essere l'esatto opposto: un media center pulito, scattante, leggero e privo di qualsiasi pubblicità o telemetria.

### ✨ 7 moduli integrati fondamentali
1. **🚀 Launcher ultra-veloce e leggero**: Avvio immediato, precaricamento parallelo delle icone su processori multi-core e navigazione fluida a 60 FPS.
2. **📺 Player IPTV con canali preferiti in home**: Supporto playlist M3U e avvio con 1 clic dei canali preferiti.
3. **🎥 MiniTube**: Esperienza YouTube Leanback ottimizzata per TV con riproduzione ospite rapida.
4. **📥 Mini Downloader**: Downloader ad alta velocità per file e APK con installatore automatico intelligente.
5. **🛒 Aura Store**: Negozio web integrato per esplorare e installare comodamente app per Android TV.
6. **🕹️ 4 portali di giochi online**: Migliaia di giochi HTML5 istantanei senza download (**CrazyGames**, **Poki**, **Playhop**, **EasyGames**).
7. **🛡️ Blocco pubblicità a livello di sistema**: Protezione AdGuard DNS integrata per bloccare annunci e tracker su tutto il dispositivo.

---

## 🌟 Nuove funzionalità e ultimi miglioramenti

### ⚡ Prestazioni estreme e risposta immediata
* **Caricamento immediato di icone e banner**: Tutte le app vengono decodificate in background su tutti i core della CPU, apparendo all'istante all'avvio.
* **Navigazione D-Pad fulminea (110 ms)**: Accelerazione hardware tramite `graphicsLayer` e transizioni a 110 ms per eliminare qualsiasi ritardo del telecomando.
* **Effetto alone ambientale ottimizzato GPU**: Sostituzione dei filtri di sfocatura pesanti con gradienti radiali nativi, garantendo 60 FPS stabili.
* **Schermata iniziale pulita (Pulsante Edit inferiore rimosso)**: Riorganizzazione immediata tramite pressione prolungata di `OK` o del tasto `Menu`.

### 🌙 Salvaschermo intelligente e gestione del sistema
* **Passaggio automatico al salvaschermo esterno (`Disattivato`)**: Disattivando il salvaschermo interno, il launcher rilascia il blocco schermo (`FLAG_KEEP_SCREEN_ON`), consentendo al salvaschermo di sistema o ad app esterne di avviarsi liberamente.
* **Salvaschermo integrato immersivo (`Attivo`)**: Con timer a 2, 5, 10, 15 o 30 minuti, offre tre stili distinti:
  * **Orologio e data**: Grande orologio luminoso con protezione burn-in per pannelli TV.
  * **Sfondo 4K con info in basso**: Gradiente a 45° che risalta orario, data e messaggio di benvenuto.
  * **Video cinematografico a schermo intero**: Camini caldi, chalet innevati o cascate in loop continuo senza pulsanti o finestre fastidiose.
  * **Presentazione foto (`Slideshow`)**: Mostra le foto da una cartella su memoria/USB, foto selezionate o gallerie 4K con durata regolabile (`5s`–`5m`), movimento **Ken Burns / Zoom delicato / Statico**, 6 transizioni di ingresso, effetti particellari (**Neve ❄️**, **Lucciole ✨**, **Pioggia 🌧️**, **Foglie d'autunno 🍂**, **Sakura 🌸**, **Stelle ⭐**, **Bokeh 🔮**), orologio in angolo e musica rilassante o MP3 personalizzato.

### 🕒 Personalizzazione totale di orologio, data e saluti
* **Tipografia multilingue (21 lingue)**: Traduzione completa in **Italiano**, **Arabo (Font Cairo)**, **Inglese**, **Tedesco**, **Francese**, **Spagnolo**, **Russo**, **Portoghese**, **Turco**, **Polacco**, **Olandese**, **Greco**, **Indonesiano**, **Vietnamita**, **Thailandese**, **Cinese**, **Giapponese**, **Coreano**, **Hindi**, **Urdu** e **Persiano**.
* **Opzioni orologio**: Formato 12h / 24h, secondi in tempo reale opzionali e 3 dimensioni (Compatto, Medio, Grande).
* **Formati data USA e internazionali**: USA numerico (`MM/dd/yyyy`), USA esteso (`MMMM d, yyyy`), USA completo (`EEEE, MMMM d, yyyy`), USA compatto (`EEE, MMM d`), Internazionale (`dd/MM/yyyy`), ISO (`yyyy-MM-dd`), Europeo (`dd.MM.yyyy`), Esteso o nascosta.
* **Stili di saluto**: Saluti poetici orari, calorosi, brevi, testo personalizzato o disattivato.

### 🎛️ Personalizzazione layout e home screen
* **Modalità icona barra superiore (Centro di controllo / Impostazioni dirette / Entrambi)**: Scegli se mostrare l'icona del Centro di controllo, saltare il Centro di controllo per aprire direttamente le Impostazioni, o mostrare entrambe le icone insieme sulla schermata Home.
* **Cursore predefinito su Impostazioni**: All'avvio dell'app il cursore è posizionato direttamente sull'icona delle Impostazioni per una configurazione rapida.
* **Spostamento verticale delle app verso il basso**: Possibilità di abbassare le righe delle app (da 180dp a 380dp) per dare risalto agli sfondi 4K.
* **Densità griglia**: Scelta tra 4, 5, 6 o 7 app per riga.
* **Opacità effetto vetro**: Regolazione della trasparenza delle schede da 10% a 100%.

### 🔒 Privacy, blocco PIN e gestione app
* **Blocco app con codice PIN a 4 cifre**: Protezione sicura per le applicazioni riservate.
* **Gestione app**: Sposta, classifica (App vs Giochi) o nascondi le app di sistema non necessarie.
* **Pulsante Indietro sicuro**: Previene l'uscita involontaria alla schermata di sistema del televisore.

---

## 📥 Download e installazione APK

### Opzione 1: Download diretto APK
Scarica il file APK pronto all'uso direttamente sul televisore o telefono:

👉 **[Scarica l'ultimo APK di Aura TV Launcher](https://github.com/OsamaSalsa/Aura-TV-Launcher/raw/refs/heads/main/aura-tv-launcher.apk)**  
👉 **[Codice breve per app Downloader: aftv.news/7099231](https://aftv.news/7099231)**

### Opzione 2: Installazione via ADB
```bash
adb connect <IP_DELLA_TV>:5555
adb install -r aura-tv-launcher.apk
```

---

## 🎮 Scorciatoie telecomando

| Tasto | Azione |
| :--- | :--- |
| **Frecce direzionali (D-Pad)** | Navigazione rapida e fluida tra applicazioni, canali e impostazioni. |
| **Tasto OK / Centro** | Avvia l'applicazione o riproduce il canale selezionato. |
| **Pressione prolungata OK / Menu (`☰` Tre linee)** | Apre le opzioni dell'app (Sposta, Blocca con PIN, Nascondi) o salta direttamente alle app dalla barra superiore. |
| **Tasto Voce / Google Assistant (`🎤`)** | Apre la ricerca vocale integrata per trovare app o canali IPTV. |
| **Tasto Indietro** | Torna alla schermata precedente o alla home in sicurezza. |

---

## 📄 Licenza e copyright

Progettato e sviluppato da **© Osama Salsa**.  
Tutti i diritti riservati.

<div align="center">
  <sub>Creato con ❤️ per un'esperienza Android TV veloce, pulita e senza pubblicità in tutto il mondo.</sub>
</div>
