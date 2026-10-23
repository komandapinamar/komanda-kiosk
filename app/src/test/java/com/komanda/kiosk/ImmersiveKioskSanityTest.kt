package com.komanda.kiosk

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ImmersiveKioskSanityTest {

    @Test
    fun kioskActivity_declaresWindowInsetsAndImmersiveStickyBehavior() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val activityFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/KioskActivity.kt")

        assertTrue("KioskActivity.kt should exist", activityFile.exists())
        val content = activityFile.readText()

        assertTrue(
            "KioskActivity should configure BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE",
            content.contains("BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE")
        )
        assertTrue(
            "KioskActivity should hide systemBars",
            content.contains("WindowInsetsCompat.Type.systemBars()")
        )
        assertTrue(
            "KioskActivity should re-enforce immersive mode onWindowFocusChanged",
            content.contains("onWindowFocusChanged")
        )
        assertTrue(
            "KioskActivity should call WindowCompat.setDecorFitsSystemWindows(window, false)",
            content.contains("WindowCompat.setDecorFitsSystemWindows")
        )
    }
}
