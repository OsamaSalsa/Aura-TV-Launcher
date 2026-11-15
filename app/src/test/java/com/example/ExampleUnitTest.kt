package com.example

import com.example.ui.youtube.YOUTUBE_TV_URL
import com.example.ui.youtube.YOUTUBE_TV_USER_AGENT
import org.junit.Assert.assertEquals
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
}
