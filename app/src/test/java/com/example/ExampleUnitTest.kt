package com.example

import com.example.data.update.AppUpdateManager
import com.example.service.DnsBlockerService
import com.example.ui.youtube.YOUTUBE_TV_URL
import com.example.ui.youtube.YOUTUBE_TV_USER_AGENT
import com.example.util.LocaleHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `youtube tv url is targeting leanback on tv`() {
        assertEquals("https://www.youtube.com/tv", YOUTUBE_TV_URL)
    }

    @Test
    fun `youtube tv user agent targets smart tv browser`() {
        assertTrue(YOUTUBE_TV_USER_AGENT.contains("SMART-TV"))
    }

    @Test
    fun `app update manager compares semantic versions accurately`() {
        assertTrue(AppUpdateManager.compareVersions("1.4", "1.3") > 0)
        assertTrue(AppUpdateManager.compareVersions("v1.10", "v1.4") > 0)
        assertEquals(0, AppUpdateManager.compareVersions("v1.4", "1.4"))
        assertTrue(AppUpdateManager.compareVersions("1.3.9", "1.4") < 0)
    }

    @Test
    fun `app update manager converts github blob urls to raw`() {
        val raw = AppUpdateManager.normalizeDownloadUrl(
            "https://github.com/OsamaSalsa/Aura-TV-Launcher/blob/main/aura-tv-launcher.apk"
        )
        assertEquals(
            "https://raw.githubusercontent.com/OsamaSalsa/Aura-TV-Launcher/main/aura-tv-launcher.apk",
            raw
        )
    }

    @Test
    fun `dns blocker configures distinct ipv4 and ipv6 adguard profiles`() {
        assertEquals(4, DnsBlockerService.ADS_DNS_SERVERS.size)
        assertEquals(4, DnsBlockerService.FAMILY_DNS_SERVERS.size)
        assertTrue(DnsBlockerService.ADS_DNS_SERVERS.contains("94.140.14.14"))
        assertTrue(DnsBlockerService.FAMILY_DNS_SERVERS.contains("94.140.14.15"))
    }

    @Test
    fun `locale helper identifies rtl and sanitizes invisible marks for cairo font`() {
        assertTrue(LocaleHelper.isRtlLanguage("ar_rtl"))
        assertTrue(LocaleHelper.isRtlLanguage("fa"))
        assertTrue(LocaleHelper.isRtlLanguage("ur"))
        assertFalse(LocaleHelper.isRtlLanguage("en"))
        assertEquals("ar", LocaleHelper.getEffectiveLanguage("ar_rtl"))
        assertEquals("id", LocaleHelper.getEffectiveLanguage("in"))

        val dirty = "\u061Cالأحد\u202F2026\u200F"
        assertEquals("الأحد 2026", LocaleHelper.sanitizeForCairoFont(dirty))
    }

    @Test
    fun `extract youtube video id handles standard, short, embed, live, and raw ids`() {
        assertEquals("EG_g7BFaL5s", com.example.ui.home.extractYouTubeVideoId("https://www.youtube.com/watch?v=EG_g7BFaL5s&t=12s"))
        assertEquals("TtLeec_DR-Y", com.example.ui.home.extractYouTubeVideoId("https://youtu.be/TtLeec_DR-Y?si=abc"))
        assertEquals("8xXHeRnLwNQ", com.example.ui.home.extractYouTubeVideoId("https://www.youtube.com/embed/8xXHeRnLwNQ?autoplay=1"))
        assertEquals("EG_g7BFaL5s", com.example.ui.home.extractYouTubeVideoId("EG_g7BFaL5s"))
        assertEquals("EG_g7BFaL5s", com.example.ui.home.extractYouTubeVideoId(""))
    }
}
