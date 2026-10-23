package com.komanda.kiosk

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LoginScopeCardSanityTest {

    @Test
    fun loginScreen_declaresPlatformScopeInfoCardWithWebGuidance() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val loginFile = File(projectDir, "src/main/java/com/komanda/kiosk/core/auth/ui/LoginScreen.kt")

        assertTrue("LoginScreen.kt file should exist", loginFile.exists())
        val content = loginFile.readText()

        assertTrue(
            "LoginScreen should reference the web backoffice domain app.komanda.com.ar",
            content.contains("app.komanda.com.ar")
        )
        assertTrue(
            "LoginScreen should clarify that stock/inventory is managed on web",
            content.contains("control de stock")
        )
        assertTrue(
            "LoginScreen should clarify that analytics are managed on web",
            content.contains("analíticas de ventas")
        )
        assertTrue(
            "LoginScreen should clarify that full catalog management is managed on web",
            content.contains("administración completa del catálogo")
        )
    }
}
