[English](README.md) | [العربية](README_ar.md) | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | **Español**

<div align="center">

# 📺 Aura TV Launcher

![Pantalla Principal](docs/screenshots/home.png)

**Un lanzador y centro multimedia moderno, ultrarrápido, ligero, personalizable y 100% sin publicidad para Android TV creado con Jetpack Compose.**  
*La solución todo en uno que reemplaza docenas de aplicaciones independientes en tu televisor.*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Aura-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 La solución Todo en Uno

La mayoría de los lanzadores de Smart TV actuales están llenos de anuncios forzados, avances pesados de video y menús lentos.

**Aura TV Launcher** fue diseñado para ser todo lo contrario: un centro multimedia limpio, ligero, veloz y totalmente libre de publicidad y rastreo.

### ✨ 7 módulos esenciales integrados
1. **🚀 Lanzador ultrarrápido y ligero**: Arranque inmediato, precarga paralela de iconos en procesadores multinúcleo y navegación fluida a 60 FPS sin tirones.
2. **📺 Reproductor IPTV con favoritos en inicio**: Compatible con listas M3U y reproducción con 1 clic de tus canales preferidos.
3. **🎥 MiniTube**: Portal de YouTube Leanback optimizado para TV con reproducción fluida en modo invitado.
4. **📥 Mini Downloader**: Descargador de alta velocidad de archivos y paquetes APK con autoinstalador inteligente.
5. **🛒 Aura Store**: Tienda web integrada para descubrir e instalar aplicaciones y herramientas de Android TV con facilidad.
6. **🕹️ 4 portales de juegos online**: Miles de juegos HTML5 instantáneos sin descargas (**CrazyGames**, **Poki**, **Playhop**, **EasyGames**).
7. **🛡️ Bloqueador de anuncios global**: Motor AdGuard DNS integrado que protege todo el televisor de banners y ventanas emergentes.

---

## 🌟 Novedades y últimas mejoras destacadas

### ⚡ Rendimiento extremo y respuesta inmediata
* **Aparición instantánea de iconos y banners**: Todo el catálogo de apps se procesa en segundo plano en paralelo a través de los núcleos de la CPU, mostrándose de inmediato sin congelamientos.
* **Navegación con D-Pad ultrarrápida (110 ms)**: Aceleración por hardware completa mediante `graphicsLayer` con transiciones de 110 ms para eliminar cualquier latencia en el control remoto.
* **Efecto de halo ambiental optimizado para GPU**: Se reemplazaron los filtros de desenfoque pesados por suaves gradientes radiales por hardware, logrando 60 FPS estables.
* **Pantalla de inicio limpia (Botón Edit inferior eliminado)**: Se suprimió la barra inferior innecesaria para mayor claridad visual, manteniendo la reorganización mediante pulsación prolongada en `OK` o la tecla `Menú`.

### 🌙 Salvapantallas inteligente y cesión al sistema
* **Paso inteligente al salvapantallas externo (`Desactivado`)**: Al desactivar el salvapantallas interno, se libera automáticamente el bloqueo de pantalla (`FLAG_KEEP_SCREEN_ON`), permitiendo que el salvapantallas del sistema o una app externa funcione normalmente.
* **Salvapantallas integrado inteligente (`Activado`)**: Al seleccionar un tiempo de reposo (2, 5, 10, 15 o 30 min), el televisor se mantiene activo y muestra el estilo seleccionado:
  * **Reloj y fecha**: Hora brillante central con protección contra quemado de panel.
  * **Fondo 4K con información en esquina**: Gradiente a 45° que resalta la hora, fecha y saludo de forma elegante.
  * **Vídeo cinemático a pantalla completa**: Chimeneas cálidas, cabañas nevadas o cascadas en bucle continuo sin botones ni ventanas molestas.

### 🕒 Personalización completa de reloj, fecha y saludo
* **Soporte multilingüe (6 idiomas)**: Traducción completa al **Español**, **Árabe (Fuente Cairo)**, **Inglés**, **Francés**, **Italiano** y **Ruso**.
* **Opciones de reloj**: Formato 12h / 24h, visualización opcional de segundos y 3 tamaños (Compacto, Mediano, Grande).
* **Estilos de fecha**: Fecha completa con día, Fecha media, Fecha numérica (`DD/MM/AAAA`) o modo oculto.
* **Estilos de saludo**: Saludos poéticos por hora, cálidos, breves, texto personalizado propio o desactivado.

### 🎛️ Personalización del diseño y pantalla de inicio
* **Foco predeterminado en Ajustes**: Al iniciar, el cursor se ubica directamente sobre el icono de Ajustes para una configuración ágil.
* **Desplazamiento vertical de aplicaciones hacia abajo**: Ajuste para bajar las filas de apps (de 180dp a 380dp) para lucir los fondos 4K y la iluminación.
* **Densidad de cuadrícula**: Elección entre 4, 5, 6 o 7 aplicaciones por fila.
* **Control de opacidad de cristal**: Ajuste del nivel de transparencia de las tarjetas de 10% a 100%.

### 🔒 Privacidad, bloqueo con PIN y organización
* **Bloqueo con PIN de 4 dígitos**: Protege aplicaciones privadas de accesos no autorizados.
* **Organización de apps**: Mueve, clasifica (Apps vs Juegos) u oculta aplicaciones de sistema no deseadas.
* **Botón Atrás seguro**: Evita salidas accidentales al escritorio de Android TV al pulsar la tecla Volver en la pantalla principal.

---

## 📥 Descarga e instalación de APK

### Opción 1: Descarga directa de APK
Descarga el archivo APK directamente a tu televisor o smartphone:

👉 **[Descargar la última versión de Aura TV Launcher](https://github.com/OsamaSalsa/Aura-TV-Launcher/raw/refs/heads/main/aura-tv-launcher.apk)**  
👉 **[Código corto para app Downloader: aftv.news/7099231](https://aftv.news/7099231)**

### Opción 2: Instalación vía ADB
```bash
adb connect <IP_DE_TU_TV>:5555
adb install -r aura-tv-launcher.apk
```

---

## 🎮 Atajos del control remoto

| Tecla | Acción |
| :--- | :--- |
| **Flechas del D-Pad** | Navegación de alta velocidad fluida entre apps, canales y ajustes. |
| **Botón OK / Centro** | Abre la aplicación o reproduce el canal seleccionado. |
| **Pulsación larga en OK o tecla Menú** | Abre las opciones (Mover, Bloquear con PIN, Ocultar). |
| **Botón Atrás (Back)** | Vuelve a la pantalla anterior o a la principal de forma segura. |

---

## 📄 Licencia y derechos de autor

Diseñado y desarrollado por **© Osama Salsa**.  
Todos los derechos reservados.

<div align="center">
  <sub>Hecho con ❤️ para una experiencia Android TV veloz, limpia y sin anuncios en todo el mundo.</sub>
</div>
