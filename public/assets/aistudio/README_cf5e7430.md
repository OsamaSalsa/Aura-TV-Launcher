<div align="center">

# 📺 Aura TV Launcher

**A modern, customizable, ultra-fast, and 100% Ad-Free Android TV Launcher & Media Center built with Jetpack Compose.**

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Clean-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/your-username/aura-tv-launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/your-username/aura-tv-launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 Why Aura TV Launcher?

Most smart TV launchers today are cluttered with forced ads, heavy video previews, unwanted recommendations, and laggy user interfaces. 

**Aura TV Launcher is built to be the exact opposite:**
* ⚡ **Blazing Fast & Ultra-Lightweight**: Instant boot times, fluid 60fps animations, and zero CPU/RAM bloatware.
* 🛡️ **100% Ad-Free Experience**: Built-in **AdGuard DNS & Ad Blocker** blocks popups, video ads, tracking scripts, and web banners across all app stores and browser portals.
* 🧼 **Clean & Distraction-Free UI**: Pure control over your TV screen — no telemetry, no tracking, no unwanted recommended content.

---

## 🌟 Standout & Unique Features

### ⚡ Clean, Fast & Ad-Free System
* **Integrated AdGuard DNS Engine**: Built-in DNS blocker shields your TV experience from intrusive ads, popups, and malicious domains.
* **Instantaneous D-Pad Response**: Zero-lag navigation fine-tuned specifically for Android TV remotes and Firestick controllers.

### 🌅 Dynamic Hourly Optimistic Greetings
* **Time-Adaptive Warm Greetings**: Displays dynamic, uplifting, and inspiring greeting messages in the top header bar that update every single hour.
* **Localized Warmth**: Dynamically switches greetings based on time of day (morning, afternoon, evening, night) in both **English** and **Arabic**.

### 📥 Built-in Mini Downloader
* **High-Speed File & APK Downloader**: Download any APK file or media directly to your TV storage using URLs or direct links.
* **One-Click Auto-Installer**: Smart APK parser automatically detects downloaded `.apk` packages and offers instant 1-click installation.
* **Integrated Download Manager**: View, launch, keep, or delete downloaded APK files directly inside the app library.

### 🎨 Advanced Background & Wallpaper Customization
* **Dynamic Palette Extraction**: Automatically samples dominant colors from focused apps/channels to generate glowing ambient background gradients.
* **4K Wallpaper Gallery**: Continuous D-Pad scrolling across high-res wallpapers categorized into dynamic palettes, anime, abstract, nature, cyber, space, and dark themes.
* **Custom Local Photo Upload**: Pick and upload your own custom wallpapers directly from device internal storage or USB drives.
* **Glassmorphic Opacity Control**: Adjust the glass panel transparency to fit your aesthetic preference.

### 🌐 Complete Multi-Language & RTL Support
* **Native Arabic & English Support**: Full Arabic localization (RTL) featuring elegant **Cairo** typography alongside standard English (LTR).
* **Flexible Clock Formats**: Switch between 12-hour and 24-hour digital clock modes with localized date strings.

---

## 🎮 Built-in Portals & Media Center

* 📺 **Integrated IPTV Player**: M3U playlist support, category navigation, and 1-click home screen favorite channels auto-play.
* 🛒 **Aura Store**: Embedded web app market for discovering and downloading Android TV applications cleanly.
* 🕹️ **HTML5 Gaming Portals**: Direct TV access to top web games:
  * 🎯 **CrazyGames** | 🎲 **Poki** | 🕹️ **Playhop** | 🎮 **EasyGames**
* 🎥 **YouTube TV Portal**: Embedded YouTube Leanback interface with custom video player dialogs.

---

## 📱 Screenshots

<div align="center">

| Home Screen & Hourly Greetings | 4K Wallpaper & Custom Photos |
| :---: | :---: |
| *![Home Screen](docs/screenshots/home_screen.png)* | *![Wallpapers](docs/screenshots/wallpaper_grid.png)* |

| Integrated IPTV Player | Mini Downloader & Aura Store |
| :---: | :---: |
| *![IPTV Player](docs/screenshots/iptv_player.png)* | *![Aura Store](docs/screenshots/aura_store.png)* |

</div>

---

## 📥 APK Download & Installation

### Option 1: Direct APK Download
Download the latest compiled release APK directly to your TV or phone:

👉 **[Download Latest Aura TV Launcher APK](https://github.com/OsamaSalsa/Aura-TV-Launcher/raw/refs/heads/main/aura-tv-launcher.apk)**
👉 **[aftv.news/7099231](https://aftv.news/7099231)**
 
### Option 2: Installation via ADB
```bash
# Connect to your Android TV IP address
adb connect <YOUR_TV_IP_ADDRESS>:5555

# Install the APK
adb install -r aura-tv-launcher.apk
```

### Option 3: Setting as Default Home Launcher
1. Open **Settings** inside Aura TV Launcher.
2. Click **Set as Default Launcher**.
3. Select **Aura TV Launcher** and choose **Always**.

---

## 🎮 Remote Control Shortcuts

| Remote Key | Action |
| :--- | :--- |
| **D-Pad Arrows** | Navigate through apps, channels, settings, and wallpapers. |
| **OK / D-Pad Center** | Launch selected item or open channel. |
| **Long Press OK** | Open App/Channel Options Menu (Reorder, Hide, Uninstall). |
| **3-Lines Menu Key** (`KEYCODE_MENU`) | Opens **Settings** on Home Screen, or toggles **Virtual Mouse Pointer Mode** in web stores/games. |
| **Back Button** | Returns to previous screen or returns to Home Screen safely without exiting to OS. |

---

## 🛠️ Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
* **Architecture**: MVVM + Coroutines & StateFlow
* **Image Engine**: [Coil](https://coil-kt.github.io/coil/)
* **Networking**: HttpURLConnection / Ktor / Retrofit

---

## 📄 License & Copyright

Designed and developed by **© Osama Salsa**.

All rights reserved.

---

<div align="center">
  <sub>Made with ❤️ for a clean, fast, and ad-free Android TV experience worldwide.</sub>
</div>
