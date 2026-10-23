package com.komanda.kiosk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CatalogLandingSanityTest {

    @Test
    fun kioskScreen_defaultsToCatalogListMode() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val screenFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/EspressoKioskScreen.kt")

        assertTrue("EspressoKioskScreen.kt should exist", screenFile.exists())
        val content = screenFile.readText()

        assertTrue(
            "viewMode should default to KioskViewMode.LIST",
            content.contains("var viewMode by remember { mutableStateOf(KioskViewMode.LIST) }")
        )

        assertFalse(
            "viewMode should not default to KioskViewMode.HUB",
            content.contains("var viewMode by remember { mutableStateOf(KioskViewMode.HUB) }")
        )

        assertTrue(
            "dismissApprovedPayment should return to KioskViewMode.LIST",
            content.contains("viewMode = KioskViewMode.LIST")
        )
    }
}
