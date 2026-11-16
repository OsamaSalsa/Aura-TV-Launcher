[English](README.md) | [العربية](README_ar.md) | **Deutsch** | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 📺 Aura TV Launcher

![Startbildschirm](docs/screenshots/home.png)

**Ein moderner, anpassbarer, ultraschneller und zu 100 % werbefreier Android TV Launcher & Media Center auf Basis von Jetpack Compose.**  
*Die All-in-One-TV-Lösung, die Dutzende einzelner Apps auf Ihrem Gerät ersetzt.*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 Die All-in-One-Lösung

Die meisten Smart-TV-Launcher sind heute mit erzwungener Werbung, schweren Videovorschauen, unerwünschten Empfehlungen und trägen Benutzeroberflächen überladen.

**Aura TV Launcher** wurde als das genaue Gegenteil entwickelt. Er ist nicht nur ein Startbildschirm, sondern ein All-in-One-Medienzentrum, das zahlreiche separate TV-Apps in einer übersichtlichen, schnellen und komplett werbefreien Zentrale vereint.

### ✨ Integrierte Kernmodule
1. **🚀 Ultraschneller & schlanker Launcher**: Blitzschneller App-Start, GPU-beschleunigte Fokusbewegung, paralleles Vorladen von Icons und 0 % CPU-Ballast.
2. **📺 Integrierter IPTV-Player & Favoritenleiste**: M3U-Playlist-Player mit sofortiger 1-Klick-Wiedergabe Ihrer Lieblingssender direkt auf dem Startbildschirm.
3. **🎥 MiniTube**: Schlankes YouTube TV Leanback-Erlebnis mit schneller Gastwiedergabe und voller TV-Fernbedienungssteuerung.
4. **📥 Mini Downloader**: Hochgeschwindigkeits-Datei- und APK-Downloader mit intelligentem 1-Klick-Paketinstaller.
5. **🛒 Aura Store**: Übersichtlicher Web-App-Marktplatz zum Durchsuchen und Installieren von Android-TV-Tools und Dienstprogrammen.
6. **🕹️ 4 Spieleportale**: Tausende sofort spielbare HTML5-Online-Spiele ohne Downloads (**CrazyGames**, **Poki**, **Playhop**, **EasyGames**).
7. **🛡️ Systemweiter Werbe- & Tracker-Blocker**: Integrierter AdGuard-DNS-Schutz schirmt Ihren gesamten Fernseher vor Popups, Bannern und Telemetrie ab.

---

## 🌟 Neue Funktionen & aktuelle Verbesserungen

### ⚡ Extreme Leistung & sofortiger App-Start
* **Paralleles Vorladen von Icons & Bannern**: App-Pakete, Symbole und Banner werden gleichzeitig im Hintergrund über alle CPU-Kerne dekodiert und erscheinen ohne Verzögerung beim Start.
* **Sofortige 110ms-D-Pad-Navigation**: Überarbeitetes hardwarebeschleunigtes `graphicsLayer`-Rendering und knackige 110ms-Übergänge beseitigen jede Cursor-Verzögerung beim Navigieren.
* **GPU-optimiertes dynamisches Umgebungsleuchten**: Schwere Echtzeit-Unschärfefilter wurden durch flüssige hardware-gerenderte Radialverläufe für butterweiche 60 FPS auf jeder TV-Hardware ersetzt.
* **Aufgeräumter Startbildschirm (Untere Edit-Taste entfernt)**: Der Fokus bleibt vollständig auf Apps und Sendern, während schnelles Umsortieren bequem per langem Druck auf `OK` oder die `Menü`-Taste möglich ist.

### 🌙 Intelligenter Bildschirmschoner & System-Übergabe
* **Intelligente Übergabe an externe Bildschirmschoner (`Deaktiviert`)**: Beim Deaktivieren des internen Bildschirmschoners werden Wachsperren (`FLAG_KEEP_SCREEN_ON`) automatisch aufgehoben, sodass der Android-TV-Systembildschirmschoner frei starten kann.
* **Integrierter Ambient-Ruhemodus (`Aktiviert`)**: Bei Aktivierung (2, 5, 10, 15 oder 30 Min.) hält der Launcher das Display wach und startet den integrierten Bildschirmschoner:
  * **Uhr & Datum**: Leuchtende Zeitanzeige und Datum mit Einbrennschutz (Drift-Protection).
  * **4K-Hintergrundbild & Info-Modus**: 45-Grad-Eckenverlauf mit Uhr, Datum und dynamischer Begrüßung.
  * **Kinoreifer Video-Bildschirmschoner & Eigene Streams**: Vollbild-Kaminfeuer, Winterhütte oder tropische Wasserfälle ohne störende Banner, plus die Möglichkeit, **eigene YouTube-Video-IDs/URLs hinzuzufügen**, Vorlagen zu löschen/wiederherstellen und eine **Live-Vorschau** direkt in den Einstellungen zu starten.
  * **Foto-Diashow-Bildschirmschoner (`Slideshow`)**: Verwandeln Sie Ihren Fernseher in eine lebendige Fotogalerie aus einem eigenen Ordner (Gerät/USB), ausgewählten Fotos oder kuratierten 4K-Galerien:
    * **Anzeigedauer & Zufallswiedergabe**: Einstellbares Intervall pro Foto (`5s`, `10s`, `15s`, `30s`, `1m`, `5m`) und optionaler Zufallsmodus.
    * **Fotobewegung**: **Ken Burns (Schwenken & Zoomen)**, **Sanfter Zoom** oder **Statisch**.
    * **Übergangseffekte**: **Überblenden**, **Horizontal schieben**, **Zoom & Ausblenden**, **Sanfte Skalierung**, **3D-Perspektivwechsel** oder **Zufällig**.
    * **Atmosphärische Effekte**: **Schneefall ❄️**, **Glühwürmchen ✨**, **Sanfter Regen 🌧️**, **Herbstlaub 🍂**, **Kirschblüten 🌸**, **Funkelnde Sterne ⭐** oder **Bokeh-Lichter 🔮**.
    * **Eck-Uhr & Hintergrundmusik**: 45°-Eck-Uhr mit Datum und Begrüßung sowie integrierte Ambient-Klänge oder eigene MP3-Musik mit Lautstärke- und Nachtdimmungs-Steuerung.

### 🕒 Umfassende Anpassung von Uhr, Datum & Begrüßung
* **Mehrsprachige Typografie (21 Sprachen)**: Vollständige Lokalisierung in **Deutsch**, **Arabisch (Cairo-Schrift)**, **Englisch**, **Spanisch**, **Französisch**, **Italienisch**, **Russisch**, **Portugiesisch**, **Türkisch**, **Polnisch**, **Niederländisch**, **Griechisch**, **Indonesisch**, **Vietnamesisch**, **Thailändisch**, **Chinesisch**, **Japanisch**, **Koreanisch**, **Hindi**, **Urdu** und **Persisch**.
* **Uhr-Optionen**: 12-/24-Stunden-Umschaltung, optionale Live-Sekundenanzeige und 3 Schriftgrößen (Kompakt, Mittel, Groß).
* **Amerikanische & Internationale Datumsformate**: US-Numerisch (`MM/dd/yyyy`), US-Lang (`MMMM d, yyyy`), US-Vollständig (`EEEE, MMMM d, yyyy`), US-Kompakt (`EEE, MMM d`), International (`dd/MM/yyyy`), ISO (`yyyy-MM-dd`), Europäisch (`dd.MM.yyyy`), Vollständig oder Ausgeblendet.
* **Begrüßungsstile**: Poetische stündliche Grüße, herzliche Grüße, kurze Begrüßung, persönlicher Wunschtext oder ausgeblendet.

### 🎛️ Anpassung von Launcher & Startbildschirm-Layout
* **Kopfzeilen-Modus (Kontrollzentrum / Direkte Einstellungen / Beide)**: Wählen Sie, ob oben auf dem Startbildschirm das **Schnell-Kontrollzentrum**, direkt das **Einstellungen-Symbol** (Kontrollzentrum überspringen) oder **beide Symbole gleichzeitig** angezeigt werden.
* **Standardfokus auf Einstellungen**: Der Launcher startet mit dem Cursor direkt auf dem Symbol oben rechts für eine schnelle Einrichtung.
* **Vertikaler App-Versatz nach unten**: Verschieben Sie App-Reihen nach unten (6 benannte Stufen, Standard 450), um hochauflösende 4K-Hintergründe und Aurora-Lichteffekte optimal zur Geltung zu bringen.
* **Benutzerdefinierte Rasterdichte**: Wählen Sie zwischen 4, 5, 6 oder 7 Apps pro Zeile passend zu Ihrem Fernseher.
* **Glasmorphismus-Transparenz & Dynamische Material-You-Farben**: Passen Sie die Durchsichtigkeit der Kacheln von 10 % bis 100 % an und aktivieren Sie optional **Dynamische Material-You-Farben**.
* **Sideload- / Nicht-TV-Apps umschalten**: Wechseln Sie zwischen **Nur TV-Apps** oder **Alle installierten Android-Apps** (einschließlich per APK installierter Mobil-Apps), inklusive 1-Klick-Optionen zum Wiederherstellen ausgeblendeter Apps oder Zurücksetzen der Sortierung.

### 🎙️ Universelle Sprachsuche, System-Sprachassistent & Interaktives IPTV
* **Universelle Sprach- & Sofortsuche (`🎤`)**: Durchsuchen Sie alle installierten TV-Apps, Live-IPTV-Kanäle und Web-/YouTube-Anfragen gleichzeitig per Sprachbefehl über das Fernbedienungsmikrofon oder die Bildschirmtastatur.
* **Standard-Sprachassistent auf Systemebene**: Integrierter `AuraVoiceInteractionService` und `AuraRecognitionService` ermöglichen es, **Aura TV** als Standard-Sprachassistenten des Android-TVs festzulegen, sodass die Mikrofontaste der Fernbedienung nativ funktioniert.
* **Schnelles Kontrollzentrum (`Control Center`)**: Glasmorphe Schnellzugriffsleiste zum Umschalten des 12h/24h-Formats, des dynamischen Hintergrunds, zum sofortigen Starten des Bildschirmschoners, der Sprachsuche sowie der WLAN- und Systemeinstellungen.
* **Multi-Playlist IPTV-Manager & Interaktive Infoleiste**: Mehrere **M3U-Wiedergabelisten** und eigene XMLTV-EPG-URLs verwalten, wechseln oder aktualisieren. Beim IPTV-Schauen öffnet ein Tippen auf den Touchscreen oder die `OK`-Taste die Infoleiste inklusive horizontaler **Kanalleiste (`◀ / ▶`)**, Live-EPG, Sleep-Timer, Seitenverhältnis, Kategorie-PIN-Sperre und automatischer Fortsetzung des letzten Kanals.
* **Sauberer Startbildschirm & Intelligente 240dp-Uhr-Verkleinerung**: Apps und Kanäle starten unten am Bildschirmrand (6 benannte Stufen, Standard `450`) ohne störende Cursor-Markierung beim Start; die große Uhr verkleinert sich automatisch, sobald die App-Reihen `240dp` vom oberen Rand erreichen.
* **Intelligente Web-Lesezeichen (HD-Icons & YouTube-TV-Modus)**: Eigene Webseiten-Kacheln laden automatisch hochauflösende Favicons/Touch-Icons, und YouTube-Lesezeichen öffnen sich direkt in der fernbedienungsfreundlichen **YouTube-TV-Leanback**-Oberfläche.
* **Zuletzt verwendete Apps & GitHub OTA-Updater**: Optionale Leiste für zuletzt geöffnete Apps sowie 1-Klick-OTA-Updates direkt über GitHub.

### 🔒 Datenschutz, App-Speichermanager, Familien-DNS & Organisation
* **Integrierter App-Speichermanager**: Zeigt alle installierten Apps sortiert nach Größe (`MB`) an, öffnet die System-App-Infos oder **deinstalliert Apps direkt** zur Speicherfreigabe.
* **8 Schnellzugriffe auf Systemeinstellungen**: Direkter Zugriff auf **WLAN**, **Ethernet**, **Anzeige**, **Ton**, **Datum & Uhrzeit**, **Alle Systemeinstellungen**, **App-Info** und **Speichermanager**.
* **Dual-Modus DNS-Schutz (Werbeblocker & Familienschutz)**: Wählen Sie zwischen **Werbung & Tracker blockieren** oder dem **Familienschutz-Modus** (blockiert zusätzlich jugendgefährdende Webseiten systemweit).
* **PIN-geschützte App-Sperre**: Schützen Sie sensible Apps und IPTV-Kategorien mit einem 4-stelligen PIN-Code.
* **Apps neu anordnen & ausblenden**: Verschieben, kategorisieren (Apps vs. Spiele) oder verstecken Sie unerwünschte System-Apps.
* **Sichere Zurück-Navigation**: Verhindert versehentliches Beenden auf den System-Desktop beim Drücken der Zurück-Taste auf dem Startbildschirm.

---

## 📱 Screenshots

<div align="center">

| Startbildschirm & Umgebungslicht | 4K-Hintergründe & Eigene Fotos |
| :---: | :---: |
| *![Startbildschirm](docs/screenshots/home_screen.png)* | *![Hintergrundbilder](docs/screenshots/wallpaper_grid.png)* |

| Integrierter IPTV-Player | Aura Store |
| :---: | :---: |
| *![IPTV-Player](docs/screenshots/iptv_player.png)* | *![Aura Store](docs/screenshots/aura_store.png)* |

| Spieleportale | Mini Downloader |
| :---: | :---: |
| *![Spieleportale](docs/screenshots/gaming_portals.png)* | *![Mini Downloader](docs/screenshots/mini_downloader.png)* |

</div>

---

## 📥 APK-Download & Installation

### Option 1: Direkter APK-Download
Laden Sie das aktuell kompilierte APK direkt auf Ihren Fernseher oder Ihr Smartphone herunter:

👉 **[Neuestes Aura TV Launcher APK herunterladen](https://github.com/OsamaSalsa/Aura-TV-Launcher/raw/refs/heads/main/aura-tv-launcher.apk)**  
👉 **[Downloader-Kurzcode / Direktlink: aftv.news/7099231](https://aftv.news/7099231)**

### Option 2: Installation über ADB
```bash
# Mit der IP-Adresse Ihres Android TV verbinden
adb connect <IHRE_TV_IP_ADRESSE>:5555

# APK installieren
adb install -r aura-tv-launcher.apk
```

### Option 3: Als Standard-Launcher festlegen
1. Öffnen Sie die **Einstellungen** im Aura TV Launcher.
2. Wählen Sie **Als Standard-Launcher festlegen**.
3. Wählen Sie **Aura TV Launcher** und klicken Sie auf **Immer**.

---

## 🎮 Fernbedienungs-Kurzbefehle

| Fernbedienungstaste | Aktion |
| :--- | :--- |
| **Steuerkreuz (D-Pad)** | Flüssige Hochgeschwindigkeits-Navigation durch Apps, Sender und Einstellungen. |
| **OK / D-Pad-Mitte** | Ausgewählte App starten oder IPTV-Sender abspielen. |
| **OK gedrückt halten / Menü (`☰` Drei Linien)** | App-/Sender-Verwaltungsmenü öffnen (Sortieren, PIN-Sperre, Ausblenden, Spiele-Kategorie) oder von der oberen Leiste direkt zu Apps/Sendern springen. |
| **Sprach- / Google Assistant-Taste (`🎤`)** | Öffnet die integrierte Sprachsuche für Apps, IPTV-Sender oder Websuchen. |
| **Einstellungen-Taste (`KEYCODE_SETTINGS`)** | Öffnet das Kontrollzentrum oder direkt die Einstellungen (je nach gewähltem Kopfzeilen-Modus). |
| **Zurück-Taste** | Kehrt zum vorherigen Bildschirm oder sicher zum Startbildschirm zurück. |

---

## 🛠️ Tech-Stack

* **Sprache**: [Kotlin](https://kotlinlang.org/)
* **UI-Framework**: [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
* **Architektur**: MVVM + Coroutines & StateFlow
* **Bild-Engine**: [Coil](https://coil-kt.github.io/coil/)
* **Medienwiedergabe**: [Media3 / ExoPlayer](https://developer.android.com/media/media3)

---

## 📄 Lizenz & Urheberrecht

Entworfen und entwickelt von **© Osama Salsa**.  
Alle Rechte vorbehalten.

<div align="center">
  <sub>Mit ❤️ entwickelt für ein sauberes, schnelles und werbefreies Android-TV-Erlebnis weltweit.</sub>
</div>
