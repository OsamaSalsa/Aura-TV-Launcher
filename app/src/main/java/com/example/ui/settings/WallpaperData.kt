package com.example.ui.settings

data class UnsplashWallpaper(
    val id: String,
    val title: String,
    val photoId: String
) {
    // Small preview thumbnail URL for fast loading in gallery/grid
    val thumbnailUrl: String
        get() = "https://images.unsplash.com/photo-$photoId?auto=format&fit=crop&w=360&q=70"

    // High resolution full wallpaper URL downloaded ONLY when selected
    val fullUrl: String
        get() = "https://images.unsplash.com/photo-$photoId?auto=format&fit=crop&w=1920&q=85"
}

data class WallpaperCategory(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val wallpapers: List<UnsplashWallpaper>
)

val onlineWallpaperCategories = listOf(
    WallpaperCategory(
        id = "animals",
        titleEn = "Animals",
        titleAr = "حيوانات",
        wallpapers = listOf(
            UnsplashWallpaper("anim_1", "Leopard in Wild", "1546182990-dffeafbe841d"),
            UnsplashWallpaper("anim_3", "Golden Retriever", "1552053831-71594a27632d"),
            UnsplashWallpaper("anim_4", "Playful Dog Portrait", "1543466835-00a7907e9de1"),
            UnsplashWallpaper("anim_5", "Majestic Bengal Tiger", "1574063413132-355dbfd83e0c"),
            UnsplashWallpaper("anim_6", "Giant Panda Bear", "1564349683136-77e08dba1ef9"),
            UnsplashWallpaper("anim_7", "Deep Sea Jellyfish", "1535591273668-578e31182c4f"),
            UnsplashWallpaper("anim_8", "African Lion Pride", "1534567153574-2b12153a87f0"),
            UnsplashWallpaper("anim_9", "Wild Elephant Sunset", "1557050543-4d5f4e07ef46"),
            UnsplashWallpaper("anim_10", "Red Fox in Forest", "1474511320723-9a56873867b5"),
            UnsplashWallpaper("anim_11", "Curious Black Cat", "1517849845537-4d257902454a"),
            UnsplashWallpaper("anim_12", "Little Puppy Face", "1507146426996-ef05306b995a"),
            UnsplashWallpaper("anim_13", "Flying Eagle", "1501705388883-4ed8a543392c"),
            UnsplashWallpaper("anim_14", "Sea Turtle Swimming", "1437622368342-7a3d73a34c8f"),
            UnsplashWallpaper("anim_15", "Cute Pug Dog", "1518020382113-a7e8fc38eac9")
        )
    ),
    WallpaperCategory(
        id = "architecture",
        titleEn = "Architecture & Interior",
        titleAr = "عمارة وتصميم",
        wallpapers = listOf(
            UnsplashWallpaper("arch_1", "Modern Interior Living", "1513694203232-719a280e022f"),
            UnsplashWallpaper("arch_2", "Glass Skyscraper", "1486406146926-c627a92ad1ab"),
            UnsplashWallpaper("arch_3", "Minimalist Luxury Villa", "1512917774080-9991f1c4c750"),
            UnsplashWallpaper("arch_4", "Modern House Design", "1600585154340-be6161a56a0c"),
            UnsplashWallpaper("arch_5", "Architectural Curves", "1600596542815-ffad4c1539a9"),
            UnsplashWallpaper("arch_6", "Concrete Geometric Art", "1503387762-592deb58ef4e"),
            UnsplashWallpaper("arch_7", "Illuminated Facade", "1513584684374-8bab748fbf90"),
            UnsplashWallpaper("arch_8", "Symmetrical Metal Waves", "1487958449943-2429e8be8625")
        )
    ),
    WallpaperCategory(
        id = "experimental",
        titleEn = "Experimental & Art",
        titleAr = "تجريبي وفني",
        wallpapers = listOf(
            UnsplashWallpaper("exp_1", "Neon Light Lines", "1518709268805-4e9042af9f23"),
            UnsplashWallpaper("exp_2", "Abstract Fluid Colors", "1541701494587-cb58502866ab"),
            UnsplashWallpaper("exp_3", "Dark Cyberpunk Glow", "1550684848-fac1c5b4e853"),
            UnsplashWallpaper("exp_4", "Prism Wave Spectrum", "1507525428034-b723cf961d3e"),
            UnsplashWallpaper("exp_5", "Purple Color Swirl", "1528618002914-72372c2621a2"),
            UnsplashWallpaper("exp_6", "Modern Graphic Artwork", "1579783902614-a3fb3927b675")
        )
    ),
    WallpaperCategory(
        id = "nature",
        titleEn = "Nature & Landscape",
        titleAr = "طبيعة",
        wallpapers = listOf(
            UnsplashWallpaper("nat_1", "Misty Mountain Forest", "1470071459604-3b5ec3a7fe05"),
            UnsplashWallpaper("nat_2", "Yosemite Valley Lake", "1426604966848-d7adac402bff"),
            UnsplashWallpaper("nat_3", "Green Mountain Valley", "1472214103451-9374bd1c798e"),
            UnsplashWallpaper("nat_4", "Tropical Sunset Beach", "1507525428034-b723cf961d3e"),
            UnsplashWallpaper("nat_5", "Snowy Alpine Peak", "1464822759023-fed622ff2c3b"),
            UnsplashWallpaper("nat_6", "Serene Sunrise Water", "1470240731273-7821a6eeb6bd")
        )
    ),
    WallpaperCategory(
        id = "travel",
        titleEn = "Travel & Places",
        titleAr = "سفر ومدن",
        wallpapers = listOf(
            UnsplashWallpaper("trv_1", "Scenic Mountain Pass", "1488646953014-85cb44e25828"),
            UnsplashWallpaper("trv_2", "Paris Eiffel Tower", "1502602898657-3e91760cbb34"),
            UnsplashWallpaper("trv_3", "European Historic City", "1499856871958-5b9627545d1a"),
            UnsplashWallpaper("trv_4", "Dubai Modern Skyline", "1512453979798-5ea266f8880c"),
            UnsplashWallpaper("trv_5", "Northern Lights Aurora", "1534447677768-be436bb09401"),
            UnsplashWallpaper("trv_6", "Sydney Harbor Lights", "1506973035872-a4ec16b8e8d9")
        )
    ),
    WallpaperCategory(
        id = "space",
        titleEn = "Space & Cosmos",
        titleAr = "فضاء وكون",
        wallpapers = listOf(
            UnsplashWallpaper("space_1", "Deep Space Nebula", "1451187580459-43490279c0fa"),
            UnsplashWallpaper("space_2", "Galaxy Stars Cluster", "1506703719100-a0f3a48c0f86"),
            UnsplashWallpaper("space_3", "Cosmic Dust Clouds", "1446776811953-b23d57bd21aa"),
            UnsplashWallpaper("space_4", "Earth Horizon from Orbit", "1614728894747-a83421e2b9c9"),
            UnsplashWallpaper("space_5", "Full Moon Night", "1532693322450-2cb5c511067d"),
            UnsplashWallpaper("space_6", "Solar Corona Eclipse", "1509198397868-475647b2a1e5")
        )
    ),
    WallpaperCategory(
        id = "oled",
        titleEn = "Dark & OLED",
        titleAr = "داكن و OLED",
        wallpapers = listOf(
            UnsplashWallpaper("oled_1", "OLED Dark Neon Glow", "1550684848-fac1c5b4e853"),
            UnsplashWallpaper("oled_2", "Midnight Waves Pattern", "1508739773434-c26b3d09e071"),
            UnsplashWallpaper("oled_3", "Neon Grid Lines", "1518709268805-4e9042af9f23"),
            UnsplashWallpaper("oled_4", "Black Polygon Geometric", "1550745165-9bc0b252726f"),
            UnsplashWallpaper("oled_5", "Smokey Dark Aura", "1517999144091-3d9dca6d1e43")
        )
    ),
    WallpaperCategory(
        id = "cars",
        titleEn = "Cars & Motion",
        titleAr = "سيارات وسرعة",
        wallpapers = listOf(
            UnsplashWallpaper("car_1", "Supercar Silhouette", "1503376780353-7e6692767b70"),
            UnsplashWallpaper("car_2", "Classic Porsche", "1580273916550-e323be2ae537"),
            UnsplashWallpaper("car_3", "Neon Night Racer", "1511919884226-fd3cad34687c"),
            UnsplashWallpaper("car_4", "Modern Luxury Coupe", "1552519507-da3b142c6e3d"),
            UnsplashWallpaper("car_5", "Highway Speed Lights", "1517524008697-84bbe3c3fd98")
        )
    ),
    WallpaperCategory(
        id = "ocean",
        titleEn = "Oceans & Water",
        titleAr = "محيطات وبحار",
        wallpapers = listOf(
            UnsplashWallpaper("ocn_1", "Turquoise Ocean Waves", "1507525428034-b723cf961d3e"),
            UnsplashWallpaper("ocn_2", "Coral Reef Ecosystem", "1544551763-46a013bb70d5"),
            UnsplashWallpaper("ocn_3", "Deep Ocean Trench", "1518837695005-2083093ee35b"),
            UnsplashWallpaper("ocn_4", "Golden Hour Coast", "1505118380757-91f5f5632de0")
        )
    ),
    WallpaperCategory(
        id = "cities",
        titleEn = "Cities at Night",
        titleAr = "مدن وليالي",
        wallpapers = listOf(
            UnsplashWallpaper("cit_1", "Tokyo Neon Alley", "1503899036084-c55cdd92da26"),
            UnsplashWallpaper("cit_2", "New York Skyline", "1496442226666-8d4d0e62e6e9"),
            UnsplashWallpaper("cit_3", "Cyberpunk Metropolis", "1519501025264-65ba15a82390"),
            UnsplashWallpaper("cit_4", "Golden Gate Bridge Fog", "1477959858617-67f30ac4fe70")
        )
    )
)
