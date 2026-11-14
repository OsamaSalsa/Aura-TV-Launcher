[English](README.md) | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | **日本語** | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 📺 Aura TV Launcher

![ホーム画面](docs/screenshots/home.png)

**Jetpack Compose で構築された、モダンで高いカスタマイズ性、超高速、そして100%広告なしの Android TV ランチャー＆メディアセンター。**  
*テレビ上の数十もの個別アプリをひとつに統合するオールインワン TV ソリューション。*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 オールインワン・ソリューション

今日の多くのスマート TV ランチャーは、強制広告、重い動画プレビュー、不要なおすすめ表示、動作の重い UI で溢れています。

**Aura TV Launcher** はその正反対を目指して設計されました。単なるホーム画面ではなく、必要な TV 機能をすべてひとつのクリーンで高速、完全広告なしのハブに集約したメディアセンターです。

### ✨ 統合されたコアモジュール
1. **🚀 超高速＆軽量ランチャー**：1秒未満の起動、GPU アクセラレーションによるフォーカス移動、並列アイコン事前読み込み、CPU 負荷ゼロ。
2. **📺 内蔵 IPTV ＆ お気に入り行**：M3U プレイリストに対応し、ホーム画面から1クリックでお気に入りチャンネルを再生。
3. **🎥 MiniTube**：TV リモコン操作とゲスト再生に対応した軽快な YouTube TV Leanback 体験。
4. **📥 Mini Downloader**：スマートな1クリックインストーラーを備えた高速ファイル＆ APK ダウンローダー。
5. **🛒 Aura Store**：Android TV 向けツールやユーティリティを簡単に閲覧・導入できる Web アプリストア。
6. **🕹️ 4つのゲームポータル**：ダウンロード不要ですぐ遊べる数千本の HTML5 オンラインゲーム（**CrazyGames**、**Poki**、**Playhop**、**EasyGames**）。
7. **🛡️ システム全体の広告＆トラッカーブロック**：内蔵 AdGuard DNS 保護により、テレビ全体のポップアップ、バナー、追跡をブロック。

---

## 🌟 新機能と最新の強化点

### ⚡ 圧倒的なパフォーマンスと瞬時の起動
* **アイコンとバナーの並列プリロード**：アプリ、アイコン、バナーをバックグラウンドのマルチコア CPU で並列デコードし、起動直後から即座に表示します。
* **110ms の超高速 D-Pad ナビゲーション**：ハードウェア高速化された `graphicsLayer` 描画と 110ms の俊敏なアニメーションにより、リモコン操作の遅延を解消。
* **GPU 最適化アンビエントグロー**：重いブラー処理やノイズレイヤーを滑らかな放射状グラデーションに置き換え、あらゆる TV で安定した 60fps を実現。
* **クリーンなホーム画面（下部編集ボタンを削除）**：画面下部の余計なボタンを省き、リモコンの `OK` または `メニュー` 長押しで手軽に並び替えできます。

### 🌙 インテリジェント・スクリーンセーバーとシステム引き継ぎ
* **外部スクリーンセーバーへの自動引き継ぎ（`無効` モード）**：内蔵スクリーンセーバーを無効にするとスリープ抑制ロック（`FLAG_KEEP_SCREEN_ON`）が解除され、Android TV 標準や外部スクリーンセーバーが正常に起動します。
* **内蔵アンビエント・スリープモード（`有効`）**：タイマー（2、5、10、15、30分）を設定すると、3つのスタイルから選んで表示できます：
  * **時計と日付モード**：焼き付き防止（ドリフト保護）付きの明るいデジタル時計。
  * **4K 壁紙＆情報モード**：45度のコーナーグラデーション上に時刻、日付、挨拶を美しく表示。
  * **シネマティック動画スクリーンセーバー**：暖炉、冬の山小屋、熱帯の滝をバナーやログイン要求なしの全画面で再生。
  * **フォトスライドショー・スクリーンセーバー（`Slideshow`）**：本体ストレージ／USBの指定フォルダ、選択した写真、または内蔵4Kギャラリーをスライド再生。表示時間（`5秒`〜`5分`）、モーション（**Ken Burns**／**ソフトズーム**／**静止**）、6種類の切り替え効果、7種類の環境エフェクト（**降雪 ❄️**、**ホタル ✨**、**小雨 🌧️**、**紅葉 🍂**、**桜吹雪 🌸**、**星空 ⭐**、**ボケ光 🔮**）、コーナー時計、環境音楽・MP3再生に対応。

### 🕒 時計・日付・挨拶メッセージの高度なカスタマイズ
* **21言語対応タイポグラフィ**：**日本語**、**アラビア語（Cairo フォント）**、**英語**、**ドイツ語**、**フランス語**、**イタリア語**、**スペイン語**、**ロシア語**、**ポルトガル語**、**トルコ語**、**ポーランド語**、**オランダ語**、**ギリシャ語**、**インドネシア語**、**ベトナム語**、**タイ語**、**中国語**、**韓国語**、**ヒンディー語**、**ウルドゥー語**、**ペルシア語** に完全対応。
* **時計設定**：12時間／24時間表示の切り替え、秒表示のオン／オフ、3段階サイズ（コンパクト、中、大）。
* **米国式＆国際日付フォーマット**：米国数値（`MM/dd/yyyy`）、米国長形式（`MMMM d, yyyy`）、米国曜日付き（`EEEE, MMMM d, yyyy`）、米国短縮（`EEE, MMM d`）、国際形式（`dd/MM/yyyy`）、ISO（`yyyy-MM-dd`）、欧州ドット形式（`dd.MM.yyyy`）、詳細形式、または非表示。
* **挨拶スタイル**：時間帯ごとの詩的な挨拶、温かな挨拶、簡潔な挨拶、カスタムテキスト、または非表示。

### 🎛️ ランチャーとホーム画面レイアウトの調整
* **上部バーのアイコンモード（コントロールセンター／直接設定／両方表示）**：ホーム画面右上に「コントロールセンター」を表示するか、コントロールセンターをスキップして「設定アイコン」を直接表示するか、「両方のアイコンを同時に表示」するかを選択できます。
* **設定アイコンへの初期フォーカス**：起動時にカーソルが右上の設定アイコンへ自動的に合わさり、素早く設定できます。
* **アプリ行の垂直オフセット**：アプリの行位置を下げて（180dp〜380dp）、高精細な 4K 壁紙やオーロラ背景を引き立てます。
* **グリッド密度の調整**：テレビ画面に合わせて 1行あたり 4、5、6、7 個から選択可能。
* **グラスモーフィズム透過率**：カードの透明感を 10%〜100% で調整可能。

### 🔒 プライバシー、アプリロック、整理機能
* **PIN コードによるアプリロック**：4桁の PIN コードで大切なアプリを保護。
* **並び替えと非表示**：アプリの移動、カテゴリ分け（アプリ／ゲーム）、不要なシステムアプリの非表示。
* **安全な戻るナビゲーション**：ホーム画面で戻るボタンを押しても誤って OS 画面へ終了するのを防ぎます。

---

## 📱 スクリーンショット

<div align="center">

| ホーム画面とアンビエント光 | 4K 壁紙とカスタム写真 |
| :---: | :---: |
| *![ホーム画面](docs/screenshots/home_screen.png)* | *![壁紙](docs/screenshots/wallpaper_grid.png)* |

| 内蔵 IPTV プレーヤー | Aura Store |
| :---: | :---: |
| *![IPTV プレーヤー](docs/screenshots/iptv_player.png)* | *![Aura Store](docs/screenshots/aura_store.png)* |

| ゲームポータル | Mini Downloader |
| :---: | :---: |
| *![ゲームポータル](docs/screenshots/gaming_portals.png)* | *![Mini Downloader](docs/screenshots/mini_downloader.png)* |

</div>

---

## 📥 APK のダウンロードとインストール

### 方法 1：APK の直接ダウンロード
最新のビルド済み APK をテレビやスマートフォンに直接ダウンロードします：

👉 **[最新の Aura TV Launcher APK をダウンロード](https://github.com/OsamaSalsa/Aura-TV-Launcher/raw/refs/heads/main/aura-tv-launcher.apk)**  
👉 **[Downloader 短縮コード / 直リンク：aftv.news/7099231](https://aftv.news/7099231)**

### 方法 2：ADB を使ったインストール
```bash
# Android TV の IP アドレスに接続
adb connect <TV_IP_ADDRESS>:5555

# APK をインストール
adb install -r aura-tv-launcher.apk
```

### 方法 3：デフォルトのホームランチャーに設定
1. Aura TV Launcher 内の **設定** を開きます。
2. **デフォルトランチャーに設定** を選択します。
3. **Aura TV Launcher** を選び、**常時** をクリックします。

---

## 🎮 リモコンのショートカット

| リモコンのキー | 動作 |
| :--- | :--- |
| **十字キー (D-Pad)** | アプリ、チャンネル、設定の間を滑らかかつ高速に移動。 |
| **OK / 決定キー** | 選択したアプリの起動または IPTV チャンネルの再生。 |
| **OK 長押し / メニュー (`☰` 三本線)** | アプリ／チャンネル管理メニューを開く（並び替え、PIN ロック、非表示、ゲーム分類）、または上部バーからアプリアイコンへ直接ジャンプ。 |
| **音声 / Google アシスタント (`🎤`)** | アプリ内の音声検索を開き、アプリや IPTV チャンネルを素早く検索。 |
| **戻るボタン** | 前の画面に戻る、またはホーム画面から終了せず安全に待機。 |

---

## 🛠️ 使用技術

* **言語**：[Kotlin](https://kotlinlang.org/)
* **UI フレームワーク**：[Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
* **アーキテクチャ**：MVVM + Coroutines & StateFlow
* **画像エンジン**：[Coil](https://coil-kt.github.io/coil/)
* **メディア再生**：[Media3 / ExoPlayer](https://developer.android.com/media/media3)

---

## 📄 ライセンスと著作権

設計・開発：**© Osama Salsa**.  
All rights reserved.

<div align="center">
  <sub>世界中のユーザーに、クリーンで高速、広告のない Android TV 体験を届けるために ❤️ を込めて制作。</sub>
</div>
