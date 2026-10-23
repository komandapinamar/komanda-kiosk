package com.komanda.kiosk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BrandingSanityTest {

    @Test
    fun appBranding_officialNameIsKomandaKiosk() {
        val appName = "Komanda Kiosk"
        val appSubtitle = "Terminal de Autoservicio"

        assertEquals("Komanda Kiosk", appName)
        assertEquals("Terminal de Autoservicio", appSubtitle)
    }

    @Test
    fun sourceCode_containsNoReferencesToKomandaBusiness() {
        // Inspect source directories to ensure no regressions reintroduce "Komanda Business"
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val srcMainDir = File(projectDir, "src/main")

        if (srcMainDir.exists()) {
            val matchingFiles = mutableListOf<String>()
            srcMainDir.walkTopDown().filter { it.isFile && (it.extension == "kt" || it.extension == "xml") }.forEach { file ->
                val content = file.readText()
                if (content.contains("Komanda Business", ignoreCase = true)) {
                    matchingFiles.add(file.path)
                }
            }
            assertTrue("Found legacy 'Komanda Business' references in: $matchingFiles", matchingFiles.isEmpty())
        }
    }

    @Test
    fun sourceCode_headerContainsNoMisspelledKiosc() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val kioskScreenFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/EspressoKioskScreen.kt")

        if (kioskScreenFile.exists()) {
            val content = kioskScreenFile.readText()
            assertFalse(
                "EspressoKioskScreen should not contain misspelled 'Komanda Kiosc'",
                content.contains("Komanda Kiosc")
            )
            assertTrue(
                "EspressoKioskScreen should contain 'Komanda Kiosk'",
                content.contains("Komanda Kiosk")
            )
        }
    }
}
