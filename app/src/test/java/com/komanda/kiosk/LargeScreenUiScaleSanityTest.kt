package com.komanda.kiosk

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LargeScreenUiScaleSanityTest {

    @Test
    fun kioskScreen_declaresLargeScreenDimensionsAndTypography() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val screenFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/EspressoKioskScreen.kt")

        assertTrue("EspressoKioskScreen.kt should exist", screenFile.exists())
        val content = screenFile.readText()

        assertTrue(
            "Product price should be styled in 22.sp",
            content.contains("fontSize = 22.sp")
        )

        assertTrue(
            "Primary action button should read 'Pagar'",
            content.contains("text = \"Pagar\"")
        )

        assertTrue(
            "Checkout button height should be 64.dp for totem touch scale",
            content.contains(".height(64.dp)")
        )

        assertTrue(
            "Checkout button typography should be 24.sp",
            content.contains("fontSize = 24.sp")
        )

        assertTrue(
            "Total amount text should be styled in 32.sp",
            content.contains("fontSize = 32.sp")
        )

        assertTrue(
            "Filter chips should have at least 48.dp height",
            content.contains(".height(48.dp)")
        )
    }
}
