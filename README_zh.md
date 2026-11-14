[English](README.md) | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | **中文** | [日本語](README_ja.md) | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 📺 Aura TV Launcher

![主屏幕](docs/screenshots/home.png)

**一款基于 Jetpack Compose 构建的现代、高度可定制、超快且 100% 无广告的 Android TV 启动器与媒体中心。**  
*一体化的电视解决方案，取代设备上数十个独立的应用程序。*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 一体化电视娱乐方案

如今大多数智能电视启动器充斥着强制广告、笨重的视频预览、不受欢迎的推荐内容以及卡顿的界面。

**Aura TV Launcher** 正是为此而生。它不仅是一个主屏幕，更是一个一体化的媒体中心，将各种独立的电视应用整合到一个干净、极速且完全无广告的中心中。

### ✨ 核心集成模块
1. **🚀 超快轻量级启动器**：亚秒级应用启动、GPU 加速焦点移动、多线程图标预加载与 0% 后台冗余负载。
2. **📺 内置 IPTV 播放器与主屏收藏栏**：支持 M3U 播放列表，直接从主屏幕一键即时播放喜爱的频道。
3. **🎥 MiniTube**：专为电视优化的轻量级 YouTube TV Leanback 体验，支持访客快速播放与完整遥控器控制。
4. **📥 Mini Downloader**：高速文件与 APK 下载器，支持智能一键安装包。
5. **🛒 Aura Store**：简洁的网页应用市场，轻松浏览和安装 Android TV 实用工具。
6. **🕹️ 4 大在线游戏门户**：数千款无需下载即点即玩的 HTML5 在线游戏（**CrazyGames**、**Poki**、**Playhop**、**EasyGames**）。
7. **🛡️ 系统级广告与追踪拦截器**：内置 AdGuard DNS 保护，为整台电视屏蔽弹窗、横幅广告和数据追踪。

---

## 🌟 新特性与最新优化

### ⚡ 极致性能与秒级响应
* **图标与横幅并行预加载**：后台利用多核 CPU 并发解码应用包、图标和横幅，启动时瞬间呈现在屏幕上。
* **极速 110ms 遥控器导航**：全新硬件加速 `graphicsLayer` 渲染与干脆的 110ms 过渡动画，彻底消除方向键移动延迟。
* **GPU 优化动态环境光晕**：以流畅的硬件径向渐变替代高负载模糊与噪点层，在任何电视硬件上实现稳定的 60fps 流畅滚动。
* **纯净主屏幕（移除底部编辑按钮）**：底部空间完全留给应用和频道，长按遥控器 `OK` 键或 `菜单` 键即可快速排序与管理。

### 🌙 智能屏保与系统屏保移交
* **智能移交外部屏保（`已停用` 模式）**：关闭内置屏保时自动清除屏幕常亮锁（`FLAG_KEEP_SCREEN_ON`），让 Android TV 系统屏保或第三方屏保自由启动。
* **内置环境休眠模式（`已启用`）**：启用后（2、5、10、15 或 30 分钟），保持电视唤醒并展示三种风格：
  * **时钟与日期模式**：带防烧屏漂移保护的明亮时钟与日期。
  * **4K 壁纸与信息模式**：左下角 45 度渐变遮罩，清晰呈现时间、日期与实时问候。
  * **沉浸式电影视频屏保**：全屏壁炉、冬日木屋或热带瀑布循环播放，无任何干扰横幅或登录提示。
  * **照片幻灯片屏保（`Slideshow`）**：从设备存储/USB 自定义文件夹、精选照片或内置 4K 图库播放幻灯片，支持自定义切换时间（`5秒`至`5分钟`）、**Ken Burns 平移缩放 / 轻柔缩放 / 静态展示**、6 种入场过渡动画、7 种唯美粒子特效（**飘雪 ❄️**、**萤火虫 ✨**、**细雨 🌧️**、**秋叶 🍂**、**樱花 🌸**、**繁星 ⭐**、**光斑 🔮**）、角落时钟日期问候以及内置环境白噪音或自定义 MP3 背景音乐。

### 🕒 全面的时钟、日期与问候语定制
* **21 种语言全面本地化**：完整支持 **中文**、**阿拉伯语（Cairo 字体）**、**英语**、**德语**、**法语**、**意大利语**、**西班牙语**、**俄语**、**葡萄牙语**、**土耳其语**、**波兰语**、**荷兰语**、**希腊语**、**印尼语**、**越南语**、**泰语**、**日语**、**韩语**、**印地语**、**乌尔都语** 和 **波斯语**。
* **时钟定制**：支持 12 小时 / 24 小时切换、可选实时秒数显示以及 3 种字体大小（紧凑、中等、大号）。
* **美式与国际日期格式**：支持美式数字（`MM/dd/yyyy`）、美式长日期（`MMMM d, yyyy`）、美式完整日期（`EEEE, MMMM d, yyyy`）、美式简短（`EEE, MMM d`）、国际格式（`dd/MM/yyyy`）、ISO 标准（`yyyy-MM-dd`）、欧洲点分（`dd.MM.yyyy`）、完整国际日期或隐藏。
* **问候语样式**：每小时诗意问候、温馨问候、简短问候、自定义个人文本或隐藏。

### 🎛️ 启动器与主屏布局定制
* **顶栏控制中心与直接设置图标模式**：可在设置中选择主屏幕右上角显示 **快捷控制中心图标**、**跳过控制中心直接显示设置图标**，或 **同时显示两个图标（控制中心 + 设置）**。
* **默认聚焦设置图标**：启动时光标默认定位在右上角设置图标上，方便快速调整配置。
* **应用行垂直下移偏移**：可将应用图标行向下偏移（180dp 至 380dp），充分展示高清 4K 壁纸与极光背景效果。
* **自定义网格密度**：每行可选 4、5、6 或 7 个应用，匹配不同屏幕尺寸与观看距离。
* **毛玻璃透明度调节**：卡片背景透明度可在 10% 至 100% 间自由调节。

### 🔒 隐私保护、应用锁与整理
* **PIN 码应用锁**：使用 4 位 PIN 码锁定敏感应用。
* **应用排序与隐藏**：自由移动、分类（应用与游戏）或隐藏不需要的系统预装应用。
* **防误触返回键**：在主屏幕按下返回键时防止意外退出到系统桌面。

---

## 📱 屏幕截图

<div align="center">

| 主屏幕与环境光效 | 4K 壁纸与自定义照片 |
| :---: | :---: |
| *![主屏幕](docs/screenshots/home_screen.png)* | *![壁纸](docs/screenshots/wallpaper_grid.png)* |

| 内置 IPTV 播放器 | Aura Store |
| :---: | :---: |
| *![IPTV 播放器](docs/screenshots/iptv_player.png)* | *![Aura Store](docs/screenshots/aura_store.png)* |

| 游戏门户 | Mini Downloader |
| :---: | :---: |
| *![游戏门户](docs/screenshots/gaming_portals.png)* | *![Mini Downloader](docs/screenshots/mini_downloader.png)* |

</div>

---

## 📥 APK 下载与安装

### 方式 1：直接下载 APK
直接下载最新编译发布的 APK 到您的电视或手机：

👉 **[下载最新版 Aura TV Launcher APK](https://github.com/OsamaSalsa/Aura-TV-Launcher/raw/refs/heads/main/aura-tv-launcher.apk)**  
👉 **[Downloader 短代码 / 直链：aftv.news/7099231](https://aftv.news/7099231)**

### 方式 2：通过 ADB 安装
```bash
# 连接到您的 Android TV IP 地址
adb connect <您的电视_IP_地址>:5555

# 安装 APK
adb install -r aura-tv-launcher.apk
```

### 方式 3：设为默认桌面启动器
1. 打开 Aura TV Launcher 内的 **设置**。
2. 选择 **设为默认启动器**。
3. 选择 **Aura TV Launcher** 并点击 **始终**。

---

## 🎮 遥控器快捷键

| 遥控器按键 | 功能 |
| :--- | :--- |
| **方向键 (D-Pad)** | 在应用、频道与设置之间流畅高速导航。 |
| **OK / 确认键** | 启动所选应用或播放 IPTV 频道。 |
| **长按 OK / 菜单键 (`☰` 三横线)** | 在应用或频道上打开管理菜单（排序、PIN 锁定、隐藏、游戏分类），或在顶栏时直接跳转至应用列表。 |
| **语音 / Google 助理键 (`🎤`)** | 打开内置语音搜索，快速查找应用、IPTV 频道或网页。 |
| **返回键 (Back)** | 返回上一页，或在主屏安全回到顶部而不退出应用。 |

---

## 🛠️ 技术栈

* **开发语言**：[Kotlin](https://kotlinlang.org/)
* **UI 框架**：[Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
* **架构**：MVVM + Coroutines & StateFlow
* **图片引擎**：[Coil](https://coil-kt.github.io/coil/)
* **媒体播放**：[Media3 / ExoPlayer](https://developer.android.com/media/media3)

---

## 📄 许可与版权

由 **© Osama Salsa** 设计与开发。  
保留所有权利。

<div align="center">
  <sub>用 ❤️ 打造，为全球用户带来纯净、极速、无广告的 Android TV 体验。</sub>
</div>
