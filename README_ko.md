[English](README.md) | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | **한국어** | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 📺 Aura TV Launcher

![홈 화면](docs/screenshots/home.png)

**Jetpack Compose로 제작된 현대적이고 사용자 정의가 자유로우며 초고속이고 100% 광고가 없는 Android TV 런처 및 미디어 센터입니다.**  
*기기에 설치된 수십 개의 개별 앱을 하나로 대체하는 올인원 TV 솔루션입니다.*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 올인원 TV 솔루션

오늘날 대부분의 스마트 TV 런처는 강제 광고, 무거운 동영상 미리보기, 원치 않는 추천 콘텐츠, 느린 UI로 가득 차 있습니다.

**Aura TV Launcher**는 그 정반대를 목표로 설계되었습니다. 단순한 홈 화면을 넘어, 여러 TV 앱을 하나의 깔끔하고 빠르며 완전히 광고 없는 허브로 통합하는 올인원 미디어 센터입니다.

### ✨ 핵심 통합 모듈
1. **🚀 초고속 경량 런처**: 1초 미만의 빠른 앱 실행, GPU 가속 포커스 이동, 병렬 아이콘 사전 로딩, CPU 부하 0%.
2. **📺 내장 IPTV 플레이어 및 즐겨찾기 행**: M3U 재생목록을 지원하며 홈 화면에서 클릭 한 번으로 즐겨찾는 채널을 즉시 재생합니다.
3. **🎥 MiniTube**: 빠른 게스트 재생과 완전한 TV 리모컨 제어를 지원하는 가벼운 YouTube TV Leanback 경험.
4. **📥 Mini Downloader**: 스마트 원클릭 패키지 설치 기능을 갖춘 고속 파일 및 APK 다운로더.
5. **🛒 Aura Store**: Android TV 도구와 유틸리티를 간편하게 탐색하고 설치할 수 있는 깔끔한 웹 앱 마켓.
6. **🕹️ 4가지 게임 포털**: 별도 다운로드 없이 즉시 즐기는 수천 개의 온라인 HTML5 게임 (**CrazyGames**, **Poki**, **Playhop**, **EasyGames**).
7. **🛡️ 시스템 전체 광고 및 추적 차단기**: 내장 AdGuard DNS 보호 기능으로 TV 전체의 팝업, 배너 및 데이터 추적을 차단합니다.

---

## 🌟 새로운 기능 및 최신 개선 사항

### ⚡ 극강의 성능과 즉각적인 앱 실행
* **아이콘 및 배너 병렬 프리로딩**: 앱 패키지, 아이콘 및 배너를 백그라운드 멀티코어 CPU에서 동시에 디코딩하여 실행 시 멈춤 없이 즉시 표시합니다.
* **초고속 110ms D-Pad 포커스 탐색**: 하드웨어 가속 `graphicsLayer` 렌더링과 110ms의 민첩한 전환 효과로 리모컨 조작 지연을 완전히 제거했습니다.
* **GPU 최적화 다이내믹 앰비언트 글로우**: 무거운 블러 및 노이즈 레이어를 부드러운 하드웨어 방사형 그라데이션으로 대체해 어떤 TV에서도 60fps를 유지합니다.
* **깔끔한 홈 화면 (하단 편집 버튼 제거)**: 시선을 앱과 채널에 집중시키고, 리모컨 `OK` 또는 `메뉴` 버튼을 길게 눌러 빠르게 정렬할 수 있습니다.

### 🌙 지능형 화면 보호기 및 외부 화면 보호기 전환
* **스마트 외부 화면 보호기 전환 (`비활성화` 모드)**: 앱 내 화면 보호기를 끄면 화면 켜짐 유지 잠금(`FLAG_KEEP_SCREEN_ON`)이 자동으로 해제되어 Android TV 시스템이나 외부 화면 보호기가 정상 작동합니다.
* **내장 앰비언트 절전 모드 (`활성화`)**: 대기 시간(2, 5, 10, 15 또는 30분)을 설정하면 TV를 깨운 상태로 내장 화면 보호기를 실행합니다:
  * **시계 및 날짜 모드**: 번인 방지(드리프트 보호) 기능이 포함된 선명한 시계와 날짜.
  * **4K 배경화면 및 정보 모드**: 45도 모서리 그라데이션 위에 시계, 날짜 및 실시간 인사말 표시.
  * **시네마틱 비디오 화면 보호기**: 불필요한 배너나 로그인 창 없이 전체 화면으로 벽난로, 겨울 오두막, 열대 폭포 영상을 감상하세요.
  * **사진 슬라이드쇼 화면 보호기 (`Slideshow`)**: 기기 저장소/USB의 특정 폴더, 선택한 사진 또는 4K 갤러리를 슬라이드쇼로 재생합니다. 사진별 표시 시간(`5초`~`5분`), 모션(**Ken Burns** / **부드러운 줌** / **정지**), 6가지 전환 효과, 7가지 분위기 효과(**눈 내림 ❄️**, **반딧불이 ✨**, **잔잔한 비 🌧️**, **단풍잎 🍂**, **벚꽃잎 🌸**, **반짝이는 별 ⭐**, **보케 조명 🔮**), 모서리 시계 및 배경 음악(내장 사운드/MP3)을 지원합니다.

### 🕒 완벽한 시계, 날짜 및 인사말 맞춤 설정
* **21개 언어 다국어 타이포그래피**: **한국어**, **아랍어(Cairo 글꼴)**, **영어**, **독일어**, **프랑스어**, **이탈리아어**, **스페인어**, **러시아어**, **포르투갈어**, **튀르키예어**, **폴란드어**, **네덜란드어**, **그리스어**, **인도네시아어**, **베트남어**, **태국어**, **중국어**, **일본어**, **힌디어**, **우르두어**, **페르시아어**를 완벽 지원합니다.
* **시계 설정**: 12시간/24시간 전환, 실시간 초 표시 선택, 3가지 크기(작게, 중간, 크게).
* **미국식 및 국제 날짜 형식**: 미국 숫자(`MM/dd/yyyy`), 미국 긴 형식(`MMMM d, yyyy`), 미국 요일 포함(`EEEE, MMMM d, yyyy`), 미국 요약(`EEE, MMM d`), 국제(`dd/MM/yyyy`), ISO(`yyyy-MM-dd`), 유럽식(`dd.MM.yyyy`), 전체 날짜 또는 숨김.
* **인사말 스타일**: 시간대별 시적인 인사말, 따뜻한 인사말, 간단한 인사말, 사용자 지정 문구 또는 숨김.

### 🎛️ 런처 및 홈 화면 레이아웃 설정
* **상단 바 컨트롤 센터 및 설정 바로가기 모드**: 홈 화면 우측 상단에 **빠른 컨트롤 센터 아이콘**을 표시하거나, 컨트롤 센터를 건너뛰고 **설정 아이콘을 바로 표시**하거나, **두 아이콘을 함께 표시**할 수 있습니다.
* **설정 아이콘 기본 포커스**: 런처 부팅 시 커서가 오른쪽 상단 설정 아이콘에 맞춰져 빠르게 구성을 변경할 수 있습니다.
* **앱 행 상단 여백 조절**: 앱 행을 아래로 내려(180dp~380dp) 고해상도 4K 배경화면과 오로라 배경 효과를 더욱 돋보이게 합니다.
* **맞춤형 그리드 밀도**: 화면 크기와 시청 거리에 맞게 한 줄당 4, 5, 6 또는 7개의 앱을 선택하세요.
* **글래스모피즘 투명도 제어**: 반투명 카드 투명도를 10%에서 100%까지 조절할 수 있습니다.

### 🔒 개인정보 보호, 앱 잠금 및 정리
* **PIN 보호 앱 잠금**: 4자리 PIN 코드로 민감한 앱을 안전하게 잠급니다.
* **앱 재정렬 및 숨기기**: 앱 이동, 분류(앱 / 게임) 또는 불필요한 기본 시스템 앱 숨기기.
* **안전한 뒤로 가기 탐색**: 홈 화면에서 뒤로 가기 버튼을 눌렀을 때 실수로 시스템 바탕화면으로 나가는 것을 방지합니다.

---

## 📱 스크린샷

<div align="center">

| 홈 화면 및 앰비언트 글로우 | 4K 배경화면 및 사용자 사진 |
| :---: | :---: |
| *![홈 화면](docs/screenshots/home_screen.png)* | *![배경화면](docs/screenshots/wallpaper_grid.png)* |

| 통합 IPTV 플레이어 | Aura Store |
| :---: | :---: |
| *![IPTV 플레이어](docs/screenshots/iptv_player.png)* | *![Aura Store](docs/screenshots/aura_store.png)* |

| 게임 포털 | Mini Downloader |
| :---: | :---: |
| *![게임 포털](docs/screenshots/gaming_portals.png)* | *![Mini Downloader](docs/screenshots/mini_downloader.png)* |

</div>

---

## 📥 APK 다운로드 및 설치

### 방법 1: 직접 APK 다운로드
최신 빌드된 APK 파일을 TV 또는 스마트폰으로 직접 다운로드하세요:

👉 **[최신 Aura TV Launcher APK 다운로드](https://github.com/OsamaSalsa/Aura-TV-Launcher/raw/refs/heads/main/aura-tv-launcher.apk)**  
👉 **[Downloader 단축 코드 / 직링크: aftv.news/7099231](https://aftv.news/7099231)**

### 방법 2: ADB를 통한 설치
```bash
# Android TV IP 주소에 연결
adb connect <YOUR_TV_IP_ADDRESS>:5555

# APK 설치
adb install -r aura-tv-launcher.apk
```

### 방법 3: 기본 홈 런처로 설정
1. Aura TV Launcher 내의 **설정**을 엽니다.
2. **기본 런처로 설정**을 선택합니다.
3. **Aura TV Launcher**를 선택하고 **항상**을 클릭합니다.

---

## 🎮 리모컨 단축키

| 리모컨 버튼 | 동작 |
| :--- | :--- |
| **방향키 (D-Pad)** | 앱, 채널 및 설정 간 부드럽고 빠른 이동. |
| **OK / 중앙 확인 버튼** | 선택한 앱 실행 또는 IPTV 채널 재생. |
| **OK 길게 누르기 / 메뉴 (`☰` 세 줄 버튼)** | 앱/채널 관리 메뉴 열기(순서 변경, PIN 잠금, 숨기기, 게임 분류) 또는 상단 바에서 앱 그리드로 즉시 이동. |
| **음성 / Google 어시스턴트 버튼 (`🎤`)** | 앱 내 음성 검색을 열어 앱, IPTV 채널 또는 웹 검색 실행. |
| **뒤로 가기 버튼** | 이전 화면으로 돌아가거나 앱을 종료하지 않고 안전하게 홈으로 복귀. |

---

## 🛠️ 기술 스택

* **언어**: [Kotlin](https://kotlinlang.org/)
* **UI 프레임워크**: [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
* **아키텍처**: MVVM + Coroutines & StateFlow
* **이미지 엔진**: [Coil](https://coil-kt.github.io/coil/)
* **미디어 재생**: [Media3 / ExoPlayer](https://developer.android.com/media/media3)

---

## 📄 라이선스 및 저작권

디자인 및 개발: **© Osama Salsa**.  
All rights reserved.

<div align="center">
  <sub>전 세계에 깔끔하고 빠르며 광고 없는 Android TV 경험을 제공하기 위해 ❤️로 제작되었습니다.</sub>
</div>
