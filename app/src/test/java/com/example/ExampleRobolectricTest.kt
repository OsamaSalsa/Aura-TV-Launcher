package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.IptvParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aura TV", appName)
    }

    @Test
    fun `m3u parser parses valid channels accurately`() {
        val m3uSample = """
            #EXTM3U
            #EXTINF:-1 tvg-id="news1" tvg-name="World News" tvg-logo="https://example.com/logo.png" group-title="News",World News HD
            https://example.com/stream.m3u8
        """.trimIndent()

        val parsed = IptvParser.parseFromContent(m3uSample)
        assertEquals(1, parsed.size)
        assertEquals("World News HD", parsed[0].name)
        assertEquals("News", parsed[0].groupTitle)
        assertEquals("https://example.com/stream.m3u8", parsed[0].streamUrl)
        assertEquals(false, parsed[0].isFavorite)
    }
}
