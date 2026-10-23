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
            "Product price should be styled in 26.sp",
            content.contains("fontSize = 26.sp")
        )

        assertTrue(
            "Primary action button should read 'Pagar'",
            content.contains("text = \"Pagar\"")
        )

        assertTrue(
            "Checkout button height should be 72.dp for totem touch scale",
            content.contains(".height(72.dp)")
        )

        assertTrue(
            "Checkout button typography should be 26.sp",
            content.contains("fontSize = 26.sp")
        )

        assertTrue(
            "Total amount text should be styled in 38.sp",
            content.contains("fontSize = 38.sp")
        )

        assertTrue(
            "Filter chips should have at least 56.dp height",
            content.contains(".height(56.dp)")
        )
    }
}
