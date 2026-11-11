[English](README.md) | [العربية](README_ar.md) | **Français** | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md)

<div align="center">

# 📺 Aura TV Launcher

![Écran d'accueil](docs/screenshots/home.png)

**Un lanceur et centre multimédia moderne, ultra-rapide, léger, hautement personnalisable et 100% sans publicité pour Android TV développé avec Jetpack Compose.**  
*La solution tout-en-un qui remplace des dizaines d'applications séparées sur votre téléviseur.*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 La solution tout-en-un

La plupart des lanceurs Android TV actuels sont saturés de publicités imposées, de bandes-annonces lourdes, de suggestions indésirables et d'interfaces lentes.

**Aura TV Launcher** a été conçu pour offrir l'expérience inverse : un hub multimédia épuré, fluide, léger et totalement exempt de publicités ou de télémétrie.

### ✨ 7 modules intégrés fondamentaux
1. **🚀 Lanceur ultra-rapide et léger** : Démarrage instantané, décodage parallèle des icônes sur processeurs multi-cœurs et navigation 60 FPS sans ralentissement.
2. **📺 Lecteur IPTV avec favoris en accueil** : Prise en charge des listes M3U et lecture en 1 clic de vos chaînes préférées depuis l'écran principal.
3. **🎥 MiniTube** : Passerelle YouTube Leanback optimisée pour TV avec lecture fluide en mode invité.
4. **📥 Mini Downloader** : Gestionnaire de téléchargement rapide de fichiers et d'APK avec installateur automatique intelligent.
5. **🛒 Aura Store** : Magasin web intégré pour découvrir et installer facilement des utilitaires Android TV.
6. **🕹️ 4 portails de jeux en ligne** : Des milliers de jeux HTML5 instantanés sans téléchargement (**CrazyGames**, **Poki**, **Playhop**, **EasyGames**).
7. **🛡️ Bloqueur de publicités à l'échelle du système** : Moteur AdGuard DNS intégré bloquant les fenêtres publicitaires et traceurs sur tout le téléviseur.

---

## 🌟 Nouvelles fonctionnalités et améliorations récentes

### ⚡ Performances extrêmes et réactivité immédiate
* **Préchargement parallèle des icônes et bannières** : Toutes les applications et leurs bannières sont décodées simultanément en arrière-plan dès le lancement, s'affichant immédiatement à l'écran.
* **Navigation D-Pad ultra-rapide (110 ms)** : Accélération matérielle complète via `graphicsLayer` avec des transitions instantanées à 110 ms pour éliminer toute latence de la télécommande.
* **Effet de halo ambiant optimisé GPU** : Remplacement des flous logiciels lourds par des dégradés radiaux matériels fluides, garantissant 60 FPS stables sur tous les boîtiers TV.
* **Écran d'accueil épuré (Bouton Edit supprimé)** : La barre inférieure encombrante a été retirée pour laisser place aux applications, avec réorganisation rapide par appui long sur `OK` ou la touche `Menu`.

### 🌙 Écran de veille intelligent et relais du système
* **Passerelle intelligente vers l'écran de veille externe (`Désactivé`)** : La désactivation de la veille interne libère automatiquement le verrouillage d'écran (`FLAG_KEEP_SCREEN_ON`), permettant au veille système ou externe de fonctionner normalement.
* **Écran de veille immersif intégré (`Activé`)** : En sélectionnant un délai d'inactivité (2, 5, 10, 15 ou 30 min), le lanceur maintient l'écran allumé et propose trois styles :
  * **Horloge et date** : Heure lumineuse avec protection anti-brûlure de dalle.
  * **Fond 4K avec infos en coin** : Dégradé à 45° mettant en valeur l'heure, la date et le message d'accueil.
  * **Vidéo cinématique plein écran** : Ambiance feu de cheminée, chalet d'hiver ou cascades en boucle sans boutons ni bannières.

### 🕒 Personnalisation complète de l'horloge, date et salutation
* **Typographie multilingue (6 langues)** : Traduction intégrale en **Français**, **Arabe (Police Cairo)**, **Anglais**, **Espagnol**, **Italien** et **Russe**.
* **Options d'horloge** : Format 12h / 24h, affichage optionnel des secondes et 3 tailles réglables (Compact, Moyen, Grand).
* **Styles de date** : Date complète avec jour, Date moyenne, Date numérique (`JJ/MM/AAAA`) ou masquée.
* **Salutations horaires** : Messages d'accueil poétiques, chaleureux, brefs, personnalisés ou masqués.

### 🎛️ Personnalisation de l'affichage d'accueil
* **Curseur par défaut sur les paramètres** : Dès le lancement, la télécommande se positionne sur l'icône Paramètres pour une configuration rapide.
* **Décalage vertical des applications vers le bas** : Possibilité d'abaisser les rangées d'applications (de 180dp à 380dp) pour admirer les fonds d'écran 4K.
* **Densité de la grille** : Choix entre 4, 5, 6 ou 7 applications par rangée.
* **Opacité de l'effet verre** : Ajustement de la transparence des tuiles de 10% à 100%.

### 🔒 Sécurité, verrouillage PIN et organisation
* **Verrouillage par code PIN** : Sécurisation des applications privées avec un code à 4 chiffres.
* **Gestion des applications** : Déplacement, classement (Applications vs Jeux) ou masquage des applications système.
* **Sécurité du bouton Retour** : Empêche la fermeture accidentelle du lanceur vers le bureau Android.

---

## 📥 Téléchargement et installation

### Option 1 : Téléchargement direct APK
Téléchargez directement le fichier APK prêt à l'emploi :

👉 **[Télécharger le dernier APK d'Aura TV Launcher](https://github.com/OsamaSalsa/Aura-TV-Launcher/raw/refs/heads/main/aura-tv-launcher.apk)**  
👉 **[Code court Downloader : aftv.news/7099231](https://aftv.news/7099231)**

### Option 2 : Installation via ADB
```bash
adb connect <IP_DE_VOTRE_TV>:5555
adb install -r aura-tv-launcher.apk
```

---

## 🎮 Raccourcis télécommande

| Touche | Action |
| :--- | :--- |
| **Flèches directionnelles** | Navigation rapide et fluide entre applications et chaînes. |
| **Bouton OK / Centre** | Lance l'application ou la chaîne sélectionnée. |
| **Appui long OK / Menu (`KEYCODE_MENU`)** | Ouvre les options (Réorganiser, Verrouiller par PIN, Masquer). |
| **Bouton Retour** | Retourne à l'écran précédent ou à l'accueil en toute sécurité. |

---

## 📄 Licence et droits d'auteur

Conçu et développé par **© Osama Salsa**.  
Tous droits réservés.

<div align="center">
  <sub>Fait avec ❤️ pour une expérience Android TV rapide, épurée et sans publicité dans le monde entier.</sub>
</div>
